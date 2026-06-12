package com.transactionriskengine.alertservice.alert.mapper;

import com.transactionriskengine.alertservice.alert.api.response.AlertResponse;
import com.transactionriskengine.alertservice.alert.domain.Alert;
import org.springframework.stereotype.Component;

@Component
public class AlertMapper {

    public AlertResponse toResponse(Alert alert) {
        return new AlertResponse(
                alert.getId(),
                alert.getAlertReference(),
                alert.getTransactionId(),
                alert.getTransactionReference(),
                alert.getRiskDecisionId(),
                alert.getCustomerId(),
                alert.getAccountId(),
                alert.getRiskScore(),
                alert.getRiskLevel(),
                alert.getDecisionStatus(),
                alert.getAlertStatus(),
                alert.getAlertPriority(),
                alert.getAlertReason(),
                alert.getTriggeredRules(),
                alert.getAssignedTo(),
                alert.getCreatedAt(),
                alert.getUpdatedAt(),
                alert.getClosedAt()
        );
    }
}
