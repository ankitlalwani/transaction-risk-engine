package com.transactionriskengine.transactioningestion.messaging.event;

import java.util.UUID;

public record TransactionEventPayload(
        UUID eventId,
        UUID transactionId,
        String transactionReference,
        String eventType,
        String status
) {
}
