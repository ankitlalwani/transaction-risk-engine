package com.transactionriskengine.alertservice.messaging.kafka;

import com.transactionriskengine.alertservice.alert.application.AlertCreationService;
import com.transactionriskengine.alertservice.messaging.event.RiskEvaluatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class RiskEvaluatedConsumer {

    private final AlertCreationService alertCreationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${transaction-risk-engine.kafka.topics.risk-evaluated}")
    public void consume(String payload) {
        try {
            RiskEvaluatedEvent event =
                    objectMapper.readValue(payload, RiskEvaluatedEvent.class);

            log.info(
                    "Consumed RiskEvaluatedEvent eventId={}, transactionId={}, riskLevel={}, decisionStatus={}",
                    event.eventId(),
                    event.transactionId(),
                    event.riskLevel(),
                    event.decisionStatus()
            );
            alertCreationService.createAlertIfRequired(event);
        } catch (Exception ex) {
            log.error("Failed to process RiskEvaluatedEvent. payload={}", payload, ex);
            throw new RuntimeException(ex);
        }
    }
}
