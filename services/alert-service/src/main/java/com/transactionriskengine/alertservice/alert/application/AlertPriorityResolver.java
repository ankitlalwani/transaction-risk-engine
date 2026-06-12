package com.transactionriskengine.alertservice.alert.application;

import com.transactionriskengine.alertservice.alert.domain.AlertPriority;
import com.transactionriskengine.alertservice.alert.domain.RiskLevel;
import com.transactionriskengine.alertservice.messaging.event.RiskEvaluatedEvent;
import org.springframework.stereotype.Component;

@Component
public class AlertPriorityResolver {

    public AlertPriority resolve(RiskEvaluatedEvent event) {
        String riskLevel = event.riskLevel();

        if ("CRITICAL".equalsIgnoreCase(riskLevel)) {
            return AlertPriority.CRITICAL;
        }

        if ("HIGH".equalsIgnoreCase(riskLevel)) {
            return AlertPriority.HIGH;
        }

        if ("MEDIUM".equalsIgnoreCase(riskLevel)) {
            return AlertPriority.MEDIUM;
        }

        return AlertPriority.LOW;
    }
}
