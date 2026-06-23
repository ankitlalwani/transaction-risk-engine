package com.transactionriskengine.aiexplanation.explanation.application;

import com.transactionriskengine.aiexplanation.messaging.event.RiskEvaluatedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@ConditionalOnProperty(
        prefix = "ai.explanation",
        name = "mode",
        havingValue = "LLM"
)
public class LlmExplanationGenerator implements ExplanationGenerator {

    private static final String MODEL_PROVIDER = "OPENAI";
    private static final String RESPONSE_SCHEMA_NAME = "risk_explanation";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String model;
    private final String promptVersion;
    private final String instructions;

    public LlmExplanationGenerator(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${ai.explanation.openai.api-key:}") String apiKey,
            @Value("${ai.explanation.openai.base-url:https://api.openai.com/v1}") String baseUrl,
            @Value("${ai.explanation.openai.model:gpt-5-mini}") String model,
            @Value("${ai.explanation.openai.prompt-version:llm-prompt-v1}") String promptVersion,
            @Value("classpath:templates/ai-explanation/llm-system-prompt.txt") Resource promptTemplate
    ) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "OPENAI_API_KEY is required when ai.explanation.mode=LLM"
            );
        }

        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
        this.objectMapper = objectMapper;
        this.model = model;
        this.promptVersion = promptVersion;
        this.instructions = readTemplate(promptTemplate);
    }

    @Override
    public GeneratedExplanation generate(RiskEvaluatedEvent event) {
        log.info(
                "Calling OpenAI for AI explanation. transactionId={}, model={}, promptVersion={}",
                event.transactionId(),
                model,
                promptVersion
        );

        Map<String, Object> request = Map.of(
                "model", model,
                "instructions", instructions,
                "input", serializeEvent(event),
                "store", false,
                "text", structuredOutputFormat()
        );

        try {
            String responseBody = restClient.post()
                    .uri("/responses")
                    .body(request)
                    .retrieve()
                    .body(String.class);

            LlmExplanationContent content = parseResponse(responseBody);
            log.info(
                    "OpenAI explanation generated successfully. transactionId={}, model={}",
                    event.transactionId(),
                    model
            );

            return new GeneratedExplanation(
                    content.explanationText(),
                    content.recommendedAction(),
                    content.analystSummary(),
                    MODEL_PROVIDER,
                    model,
                    promptVersion
            );
        } catch (RestClientResponseException exception) {
            log.error(
                    "OpenAI explanation request failed. transactionId={}, status={}, response={}",
                    event.transactionId(),
                    exception.getStatusCode(),
                    exception.getResponseBodyAsString()
            );
            throw new IllegalStateException(
                    "OpenAI explanation request failed with status "
                            + exception.getStatusCode(),
                    exception
            );
        } catch (RestClientException exception) {
            log.error(
                    "OpenAI explanation request failed. transactionId={}",
                    event.transactionId(),
                    exception
            );
            throw new IllegalStateException("OpenAI explanation request failed", exception);
        }
    }

    private String serializeEvent(RiskEvaluatedEvent event) {
        try {
            Map<String, Object> promptInput = Map.of(
                    "transactionReference", valueOrEmpty(event.transactionReference()),
                    "riskScore", event.riskScore(),
                    "riskLevel", valueOrEmpty(event.riskLevel()),
                    "decisionStatus", valueOrEmpty(event.decisionStatus()),
                    "decisionReason", valueOrEmpty(event.decisionReason()),
                    "triggeredRules", event.triggeredRules() == null
                            ? List.of()
                            : event.triggeredRules()
            );
            return objectMapper.writeValueAsString(promptInput);
        } catch (JacksonException exception) {
            throw new IllegalArgumentException(
                    "Failed to serialize risk event for OpenAI",
                    exception
            );
        }
    }

    private Map<String, Object> structuredOutputFormat() {
        Map<String, Object> stringProperty = Map.of("type", "string");
        Map<String, Object> schema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "analystSummary", stringProperty,
                        "explanationText", stringProperty,
                        "recommendedAction", stringProperty
                ),
                "required", List.of(
                        "analystSummary",
                        "explanationText",
                        "recommendedAction"
                ),
                "additionalProperties", false
        );

        return Map.of(
                "format", Map.of(
                        "type", "json_schema",
                        "name", RESPONSE_SCHEMA_NAME,
                        "strict", true,
                        "schema", schema
                )
        );
    }

    private LlmExplanationContent parseResponse(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            throw new IllegalStateException("OpenAI returned an empty response");
        }

        JsonNode response;
        try {
            response = objectMapper.readTree(responseBody);
        } catch (JacksonException exception) {
            throw new IllegalStateException("OpenAI returned invalid response JSON", exception);
        }

        JsonNode output = response.path("output");
        for (JsonNode outputItem : output) {
            if (!"message".equals(outputItem.path("type").asString())) {
                continue;
            }

            for (JsonNode contentItem : outputItem.path("content")) {
                if ("refusal".equals(contentItem.path("type").asString())) {
                    throw new IllegalStateException(
                            "OpenAI refused to generate the explanation: "
                                    + contentItem.path("refusal").asString()
                    );
                }

                if ("output_text".equals(contentItem.path("type").asString())) {
                    return deserializeContent(contentItem.path("text").asString());
                }
            }
        }

        throw new IllegalStateException("OpenAI response did not contain explanation output");
    }

    private LlmExplanationContent deserializeContent(String content) {
        LlmExplanationContent explanation;
        try {
            explanation = objectMapper.readValue(content, LlmExplanationContent.class);
        } catch (JacksonException exception) {
            throw new IllegalStateException(
                    "OpenAI returned invalid explanation JSON",
                    exception
            );
        }

        if (isBlank(explanation.analystSummary())
                || isBlank(explanation.explanationText())
                || isBlank(explanation.recommendedAction())) {
            throw new IllegalStateException(
                    "OpenAI returned an incomplete explanation"
            );
        }

        return explanation;
    }

    private static String readTemplate(Resource resource) {
        try (var inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8).trim();
        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "Failed to load LLM explanation prompt: " + resource.getDescription(),
                    exception
            );
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private record LlmExplanationContent(
            String analystSummary,
            String explanationText,
            String recommendedAction
    ) {
    }
}
