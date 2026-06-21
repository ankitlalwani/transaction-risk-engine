package com.transactionriskengine.aiexplanation.explanation.application;

import com.transactionriskengine.aiexplanation.messaging.event.RiskEvaluatedEvent;
import com.transactionriskengine.aiexplanation.messaging.event.TriggeredRule;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RuleBasedExplanationGenerator implements ExplanationGenerator {

    @Override
    public GeneratedExplanation generate(RiskEvaluatedEvent event) {
        String ruleSummary = buildRuleSummary(event.triggeredRules());

        String explanationText = buildExplanationText(event, ruleSummary);
        String recommendedAction = buildRecommendedAction(event);
        String analystSummary = buildAnalystSummary(event);

        return new GeneratedExplanation(
                explanationText,
                recommendedAction,
                analystSummary,
                "RULE_BASED",
                "deterministic-template-v1",
                "template-v1"
        );
    }

    private String buildRuleSummary(List<TriggeredRule> triggeredRules) {
        if (triggeredRules == null || triggeredRules.isEmpty()) {
            return "no configured risk rules were triggered";
        }

        return triggeredRules.stream()
                .map(TriggeredRule::ruleCode)
                .collect(Collectors.joining(", "));
    }

    private String buildExplanationText(RiskEvaluatedEvent event, String ruleSummary) {
        if ("LOW".equalsIgnoreCase(event.riskLevel())) {
            return "This transaction was classified as LOW risk because "
                    + "it did not match any high-risk rule combination. "
                    + "The risk engine assigned a score of " + event.riskScore() + ".";
        }

        return "This transaction was classified as " + event.riskLevel()
                + " risk with a score of " + event.riskScore()
                + ". The decision was based on the following triggered rule(s): "
                + ruleSummary
                + ". " + event.decisionReason();
    }

    private String buildRecommendedAction(RiskEvaluatedEvent event) {
        return switch (event.riskLevel().toUpperCase()) {
            case "CRITICAL" -> "Route this transaction for immediate analyst review. "
                    + "Verify customer intent, beneficiary or merchant details, device context, "
                    + "and recent account activity before allowing further action.";

            case "HIGH" -> "Send this transaction to analyst review. "
                    + "Validate the triggered risk indicators and compare against recent customer behavior.";

            case "MEDIUM" -> "Monitor this transaction and review if similar activity repeats "
                    + "within a short time window.";

            default -> "No manual review is required based on the current risk decision.";
        };
    }

    private String buildAnalystSummary(RiskEvaluatedEvent event) {
        return "Transaction " + event.transactionReference()
                + " received a " + event.riskLevel()
                + " risk classification with score " + event.riskScore()
                + ". Decision status: " + event.decisionStatus() + ".";
    }
}