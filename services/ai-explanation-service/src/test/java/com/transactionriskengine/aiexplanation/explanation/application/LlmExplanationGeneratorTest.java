package com.transactionriskengine.aiexplanation.explanation.application;

import com.transactionriskengine.aiexplanation.messaging.event.RiskEvaluatedEvent;
import com.transactionriskengine.aiexplanation.messaging.event.TriggeredRule;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class LlmExplanationGeneratorTest {

    @Test
    void generatesExplanationFromOpenAiStructuredOutput() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        LlmExplanationGenerator generator = new LlmExplanationGenerator(
                builder,
                new ObjectMapper(),
                "test-key",
                "https://api.openai.test/v1",
                "gpt-5-mini",
                "llm-prompt-v1",
                new ClassPathResource(
                        "templates/ai-explanation/llm-system-prompt.txt"
                )
        );

        server.expect(once(), requestTo("https://api.openai.test/v1/responses"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer test-key"))
                .andExpect(jsonPath("$.model").value("gpt-5-mini"))
                .andExpect(jsonPath("$.store").value(false))
                .andExpect(jsonPath("$.text.format.type").value("json_schema"))
                .andExpect(jsonPath("$.text.format.strict").value(true))
                .andRespond(withSuccess("""
                        {
                          "output": [
                            {
                              "type": "message",
                              "content": [
                                {
                                  "type": "output_text",
                                  "text": "{\\"analystSummary\\":\\"Critical wire transfer requires review.\\",\\"explanationText\\":\\"The score was driven by the large wire rule.\\",\\"recommendedAction\\":\\"Verify customer intent before proceeding.\\"}"
                                }
                              ]
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        GeneratedExplanation explanation = generator.generate(event());

        assertThat(explanation.analystSummary())
                .isEqualTo("Critical wire transfer requires review.");
        assertThat(explanation.explanationText())
                .isEqualTo("The score was driven by the large wire rule.");
        assertThat(explanation.recommendedAction())
                .isEqualTo("Verify customer intent before proceeding.");
        assertThat(explanation.modelProvider()).isEqualTo("OPENAI");
        assertThat(explanation.modelName()).isEqualTo("gpt-5-mini");
        assertThat(explanation.promptVersion()).isEqualTo("llm-prompt-v1");

        server.verify();
    }

    private RiskEvaluatedEvent event() {
        return new RiskEvaluatedEvent(
                UUID.randomUUID(),
                "RISK_EVALUATED",
                "1",
                Instant.parse("2026-06-23T12:00:00Z"),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "TXN-1001",
                UUID.randomUUID(),
                UUID.randomUUID(),
                95,
                "CRITICAL",
                "REVIEW_REQUIRED",
                "Large wire activity was detected.",
                List.of(new TriggeredRule(
                        "LARGE_WIRE",
                        "Large wire transfer",
                        60,
                        "Wire amount exceeded the configured threshold"
                )),
                Instant.parse("2026-06-23T12:00:00Z")
        );
    }
}
