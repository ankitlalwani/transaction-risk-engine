package com.transactionriskengine.riskengine.messaging.kafka;

import com.transactionriskengine.riskengine.messaging.event.TransactionCreatedEvent;
import com.transactionriskengine.riskengine.risk.application.RiskEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionCreatedEventConsumer {

    private final RiskEvaluationService riskEvaluationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${transaction-risk-engine.kafka.topics.transaction-created}")
    public void consume(String payload) {
        TransactionCreatedEvent event =
                objectMapper.readValue(payload, TransactionCreatedEvent.class);

        log.info("Consumed TransactionCreatedEvent: {}", event);
        validate(event);
        riskEvaluationService.evaluate(event);
    }

    private void validate(TransactionCreatedEvent event) {
        if (event.transactionId() == null) {
            throw new IllegalArgumentException("transactionId is required");
        }

        if (event.amount() == null) {
            throw new IllegalArgumentException("amount is required");
        }

        if (event.amount().signum() <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }

        if (event.paymentChannel() == null || event.paymentChannel().isBlank()) {
            throw new IllegalArgumentException("paymentChannel is required");
        }

        if (event.transactionTime() == null) {
            throw new IllegalArgumentException("transactionTime is required");
        }
    }
}
