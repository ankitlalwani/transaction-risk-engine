package com.transactionriskengine.alertservice.messaging.event;

public record TriggeredRule(
        String ruleCode,
        String ruleName,
        int scoreImpact,
        String reason
) {
}
