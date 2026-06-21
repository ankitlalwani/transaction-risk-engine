package com.transactionriskengine.aiexplanation.messaging.kafka;

import com.transactionriskengine.aiexplanation.explanation.application.AiExplanationService;
import com.transactionriskengine.aiexplanation.messaging.event.RiskEvaluatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class RiskEvaluatedConsumer {

    private final ObjectMapper objectMapper;
    private final AiExplanationService aiExplanationService;

    @KafkaListener(
            topics = "${ai-explanation-service.kafka.topics.risk-evaluated}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(String payload) {
        try {
            RiskEvaluatedEvent event =
                    objectMapper.readValue(payload, RiskEvaluatedEvent.class);

            log.info(
                    "Received RiskEvaluatedEvent for explanation. transactionId={}, riskLevel={}",
                    event.transactionId(),
                    event.riskLevel()
            );

            aiExplanationService.generateExplanation(event);

        } catch (Exception ex) {
            log.error("Failed to process RiskEvaluatedEvent for AI explanation. payload={}", payload, ex);
            throw new RuntimeException(ex);
        }
    }
}