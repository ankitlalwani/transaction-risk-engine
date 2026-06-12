package com.transactionriskengine.riskengine.risk.rule;

import com.transactionriskengine.riskengine.risk.domain.RiskRule;
import com.transactionriskengine.riskengine.risk.domain.TriggeredRule;

import java.util.Optional;

final class RuleEvaluationSupport {

    private RuleEvaluationSupport() {
    }

    static Optional<TriggeredRule> triggeredWhen(RiskRule rule, boolean matched, String reason) {
        if (!matched) {
            return Optional.empty();
        }

        return Optional.of(new TriggeredRule(
                rule.getRuleCode(),
                rule.getRuleName(),
                rule.getScoreImpact(),
                reason
        ));
    }

    static boolean equalsIgnoreCase(String value, String expected) {
        return value != null && value.equalsIgnoreCase(expected);
    }

    static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
