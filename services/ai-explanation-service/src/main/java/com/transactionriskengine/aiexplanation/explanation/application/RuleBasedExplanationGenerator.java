package com.transactionriskengine.aiexplanation.explanation.application;

import com.transactionriskengine.aiexplanation.messaging.event.RiskEvaluatedEvent;
import com.transactionriskengine.aiexplanation.messaging.event.TriggeredRule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@ConditionalOnProperty(
        prefix = "ai.explanation",
        name = "mode",
        havingValue = "RULE_BASED",
        matchIfMissing = true
)
public class RuleBasedExplanationGenerator implements ExplanationGenerator {

    private static final Pattern PLACEHOLDER_PATTERN =
            Pattern.compile("\\$\\{([a-zA-Z][a-zA-Z0-9]*)}");
    private static final String MODEL_PROVIDER = "RULE_BASED";
    private static final String MODEL_NAME = "deterministic-template-v2";
    private static final String PROMPT_VERSION = "template-v2";

    private final String analystSummaryTemplate;
    private final Map<TemplateType, String> explanationTemplates;
    private final Map<TemplateType, String> recommendedActionTemplates;

    public RuleBasedExplanationGenerator(
            @Value("classpath:templates/ai-explanation/analyst-summary.txt")
            Resource analystSummaryTemplate,
            @Value("classpath:templates/ai-explanation/explanation-low.txt")
            Resource lowRiskExplanationTemplate,
            @Value("classpath:templates/ai-explanation/explanation-elevated.txt")
            Resource elevatedRiskExplanationTemplate,
            @Value("classpath:templates/ai-explanation/action-low.txt")
            Resource lowRiskActionTemplate,
            @Value("classpath:templates/ai-explanation/action-medium.txt")
            Resource mediumRiskActionTemplate,
            @Value("classpath:templates/ai-explanation/action-high.txt")
            Resource highRiskActionTemplate,
            @Value("classpath:templates/ai-explanation/action-critical.txt")
            Resource criticalRiskActionTemplate
    ) {
        this.analystSummaryTemplate = readTemplate(analystSummaryTemplate);

        this.explanationTemplates = new EnumMap<>(TemplateType.class);
        this.explanationTemplates.put(TemplateType.LOW, readTemplate(lowRiskExplanationTemplate));
        this.explanationTemplates.put(TemplateType.ELEVATED, readTemplate(elevatedRiskExplanationTemplate));

        this.recommendedActionTemplates = new EnumMap<>(TemplateType.class);
        this.recommendedActionTemplates.put(TemplateType.LOW, readTemplate(lowRiskActionTemplate));
        this.recommendedActionTemplates.put(TemplateType.MEDIUM, readTemplate(mediumRiskActionTemplate));
        this.recommendedActionTemplates.put(TemplateType.HIGH, readTemplate(highRiskActionTemplate));
        this.recommendedActionTemplates.put(TemplateType.CRITICAL, readTemplate(criticalRiskActionTemplate));
    }

    @Override
    public GeneratedExplanation generate(RiskEvaluatedEvent event) {
        TemplateType riskLevel = parseRiskLevel(event.riskLevel());
        Map<String, String> values = templateValues(event);

        String explanationText = render(
                explanationTemplates.get(riskLevel == TemplateType.LOW
                        ? TemplateType.LOW
                        : TemplateType.ELEVATED),
                values
        );
        String recommendedAction = render(recommendedActionTemplates.get(riskLevel), values);
        String analystSummary = render(analystSummaryTemplate, values);

        return new GeneratedExplanation(
                explanationText,
                recommendedAction,
                analystSummary,
                MODEL_PROVIDER,
                MODEL_NAME,
                PROMPT_VERSION
        );
    }

    private Map<String, String> templateValues(RiskEvaluatedEvent event) {
        return Map.of(
                "transactionReference", valueOrDefault(event.transactionReference(), "unknown"),
                "riskLevel", valueOrDefault(event.riskLevel(), "UNKNOWN").toUpperCase(Locale.ROOT),
                "riskScore", String.valueOf(event.riskScore()),
                "decisionStatus", valueOrDefault(event.decisionStatus(), "UNKNOWN"),
                "decisionReason", valueOrDefault(event.decisionReason(), "No decision reason was provided."),
                "ruleSummary", buildRuleSummary(event.triggeredRules())
        );
    }

    private String buildRuleSummary(List<TriggeredRule> triggeredRules) {
        if (triggeredRules == null || triggeredRules.isEmpty()) {
            return "no configured risk rules were triggered";
        }

        return triggeredRules.stream()
                .map(this::describeRule)
                .reduce((left, right) -> left + "; " + right)
                .orElse("no configured risk rules were triggered");
    }

    private String describeRule(TriggeredRule rule) {
        String code = valueOrDefault(rule.ruleCode(), "UNKNOWN_RULE");
        String name = valueOrDefault(rule.ruleName(), code);
        String reason = valueOrDefault(rule.reason(), "No rule reason was provided");
        String scoreImpact = rule.scoreImpact() == null ? "unknown" : rule.scoreImpact().toString();

        return "%s (%s, score impact %s): %s".formatted(code, name, scoreImpact, reason);
    }

    private TemplateType parseRiskLevel(String riskLevel) {
        if (riskLevel == null || riskLevel.isBlank()) {
            throw new IllegalArgumentException("Risk level is required to generate an explanation");
        }

        try {
            return TemplateType.valueOf(riskLevel.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Unsupported risk level for explanation template: " + riskLevel,
                    exception
            );
        }
    }

    private String render(String template, Map<String, String> values) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuilder rendered = new StringBuilder();

        while (matcher.find()) {
            String placeholder = matcher.group(1);
            String value = values.get(placeholder);
            if (value == null) {
                throw new IllegalStateException("No value supplied for template placeholder: " + placeholder);
            }
            matcher.appendReplacement(rendered, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(rendered);

        return rendered.toString().trim();
    }

    private static String readTemplate(Resource resource) {
        try (var inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8).trim();
        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "Failed to load AI explanation template: " + resource.getDescription(),
                    exception
            );
        }
    }

    private static String valueOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private enum TemplateType {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL,
        ELEVATED
    }
}
