package com.transactionriskengine.transactioningestion.transaction.api;

import java.time.Instant;
import java.util.UUID;

public record LatestTransactionResponse(
        UUID transactionId,
        String transactionReference,
        String status,
        Instant createdAt
) {
}
