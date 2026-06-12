package com.transactionriskengine.riskengine.risk.domain;

public record TriggeredRule(
        String ruleCode,
        String ruleName,
        int scoreImpact,
        String reason
) {
}
