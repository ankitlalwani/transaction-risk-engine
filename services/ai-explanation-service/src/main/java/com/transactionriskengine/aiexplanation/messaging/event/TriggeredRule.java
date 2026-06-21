package com.transactionriskengine.aiexplanation.messaging.event;

public record TriggeredRule(
        String ruleCode,
        String ruleName,
        Integer scoreImpact,
        String reason
) {
}