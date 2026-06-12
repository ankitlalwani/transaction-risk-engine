package com.transactionriskengine.alertservice.alert.application;

import com.transactionriskengine.alertservice.alert.domain.*;
import com.transactionriskengine.alertservice.alert.repository.AlertRepository;
import com.transactionriskengine.alertservice.messaging.event.RiskEvaluatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertCreationService {

    private final AlertRepository alertRepository;
    private final AlertPriorityResolver alertPriorityResolver;
    private final AlertReferenceGenerator alertReferenceGenerator;
    private final ObjectMapper objectMapper;

    @Transactional
    public void createAlertIfRequired(RiskEvaluatedEvent event) {
        if (alertRepository.existsByTransactionId(event.transactionId())) {
            log.info("Alert already exists for transactionId={}", event.transactionId());
            return;
        }

        if (!requiresAlert(event)) {
            log.info(
                    "No alert required for transactionId={}, riskLevel={}, decisionStatus={}",
                    event.transactionId(),
                    event.riskLevel(),
                    event.decisionStatus()
            );
            return;
        }

        AlertPriority priority = alertPriorityResolver.resolve(event);

        Alert alert = Alert.builder()
                .alertReference(alertReferenceGenerator.generate())
                .transactionId(event.transactionId())
                .transactionReference(event.transactionReference())
                .riskDecisionId(event.riskDecisionId())
                .customerId(event.customerId())
                .accountId(event.accountId())
                .riskScore(event.riskScore())
                .riskLevel(RiskLevel.valueOf(event.riskLevel()))
                .decisionStatus(DecisionStatus.valueOf(event.decisionStatus()))
                .alertStatus(AlertStatus.OPEN)
                .alertPriority(priority)
                .alertReason(event.decisionReason())
                .triggeredRules(toJson(event))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        alertRepository.save(alert);

        log.info(
                "Created alert. alertReference={}, transactionId={}, priority={}",
                alert.getAlertReference(),
                alert.getTransactionId(),
                alert.getAlertPriority()
        );
    }

    private boolean requiresAlert(RiskEvaluatedEvent event) {
        String riskLevel = event.riskLevel();
        String decisionStatus = event.decisionStatus();

        return "HIGH".equalsIgnoreCase(riskLevel)
                || "CRITICAL".equalsIgnoreCase(riskLevel)
                || "REVIEW_REQUIRED".equalsIgnoreCase(decisionStatus)
                || "BLOCK_RECOMMENDED".equalsIgnoreCase(decisionStatus);
    }

    private String toJson(RiskEvaluatedEvent event) {
        return objectMapper.writeValueAsString(event.triggeredRules());
    }
}
