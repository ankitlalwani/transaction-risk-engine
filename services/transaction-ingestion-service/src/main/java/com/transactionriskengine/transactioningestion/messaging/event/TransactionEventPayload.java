package com.transactionriskengine.transactioningestion.messaging.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionEventPayload(
        UUID eventId,
        UUID transactionId,
        String transactionReference,
        String eventType,
        String status,
        UUID customerId,
        UUID accountId,
        BigDecimal amount,
        String currency,
        String transactionType,
        String paymentChannel,
        String merchantName,
        String merchantCategory,
        String merchantCountry,
        String sourceIp,
        String deviceId,
        Instant transactionTime
) {
}
