package com.transactionriskengine.aiexplanation.explanation.application;

import com.transactionriskengine.aiexplanation.messaging.event.RiskEvaluatedEvent;
import com.transactionriskengine.aiexplanation.messaging.event.TriggeredRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RuleBasedExplanationGeneratorTest {

    private RuleBasedExplanationGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new RuleBasedExplanationGenerator(
                template("analyst-summary.txt"),
                template("explanation-low.txt"),
                template("explanation-elevated.txt"),
                template("action-low.txt"),
                template("action-medium.txt"),
                template("action-high.txt"),
                template("action-critical.txt")
        );
    }

    @Test
    void generatesLowRiskExplanationFromTemplates() {
        RiskEvaluatedEvent event = event(
                "LOW",
                12,
                "APPROVED",
                "No material risk indicators were detected.",
                List.of()
        );

        GeneratedExplanation explanation = generator.generate(event);

        assertThat(explanation.explanationText())
                .contains("classified as LOW risk with a score of 12")
                .contains("no configured risk rules were triggered");
        assertThat(explanation.recommendedAction())
                .isEqualTo("No manual review is required based on the current risk decision.");
        assertThat(explanation.analystSummary())
                .contains("Transaction TXN-1001")
                .contains("Decision status: APPROVED");
        assertThat(explanation.modelName()).isEqualTo("deterministic-template-v2");
        assertThat(explanation.promptVersion()).isEqualTo("template-v2");
    }

    @Test
    void generatesCriticalRiskExplanationWithTriggeredRuleDetails() {
        RiskEvaluatedEvent event = event(
                "CRITICAL",
                95,
                "REVIEW_REQUIRED",
                "Multiple high-risk indicators were detected.",
                List.of(new TriggeredRule(
                        "LARGE_WIRE",
                        "Large wire transfer",
                        60,
                        "Wire amount exceeded the configured threshold"
                ))
        );

        GeneratedExplanation explanation = generator.generate(event);

        assertThat(explanation.explanationText())
                .contains("classified as CRITICAL risk with a score of 95")
                .contains("LARGE_WIRE (Large wire transfer, score impact 60)")
                .contains("Wire amount exceeded the configured threshold");
        assertThat(explanation.recommendedAction())
                .startsWith("Route this transaction for immediate analyst review.");
    }

    private ClassPathResource template(String filename) {
        return new ClassPathResource("templates/ai-explanation/" + filename);
    }

    private RiskEvaluatedEvent event(
            String riskLevel,
            int riskScore,
            String decisionStatus,
            String decisionReason,
            List<TriggeredRule> triggeredRules
    ) {
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
                riskScore,
                riskLevel,
                decisionStatus,
                decisionReason,
                triggeredRules,
                Instant.parse("2026-06-23T12:00:00Z")
        );
    }
}
