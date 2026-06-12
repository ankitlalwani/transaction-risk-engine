package com.transactionriskengine.riskengine.messaging.kafka;

import com.transactionriskengine.riskengine.messaging.event.TransactionCreatedEvent;
import com.transactionriskengine.riskengine.risk.application.RiskEvaluationService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import tools.jackson.databind.ObjectMapper;

class TransactionCreatedEventConsumerTest {

    private final RiskEvaluationService riskEvaluationService = mock(RiskEvaluationService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TransactionCreatedEventConsumer consumer =
            new TransactionCreatedEventConsumer(riskEvaluationService, objectMapper);

    @Test
    void consumeEvaluatesValidTransactionEvent() {
        TransactionCreatedEvent event = validEvent();

        consumer.consume(objectMapper.writeValueAsString(event));

        verify(riskEvaluationService).evaluate(event);
    }

    @Test
    void consumeRejectsInvalidEventBeforeRiskEvaluation() {
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                UUID.randomUUID(),
                null,
                "TXN-123",
                "TransactionReceived",
                "RECEIVED",
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                "USD",
                "PURCHASE",
                "CARD",
                null,
                "ECOMMERCE",
                "USA",
                "172.16.10.45",
                "web-device-1",
                Instant.parse("2026-06-03T15:10:00Z")
        );

        assertThatThrownBy(() -> consumer.consume(objectMapper.writeValueAsString(event)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("transactionId is required");

        verify(riskEvaluationService, never()).evaluate(event);
    }

    private TransactionCreatedEvent validEvent() {
        return new TransactionCreatedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "TXN-123",
                "TransactionReceived",
                "RECEIVED",
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("15000.00"),
                "USD",
                "TRANSFER",
                "WIRE",
                null,
                "WIRE_TRANSFER",
                "USA",
                "10.10.20.30",
                "desktop-device-004",
                Instant.parse("2026-06-03T15:30:00Z")
        );
    }
}
