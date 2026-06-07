package com.transactionriskengine.transactioningestion.transaction.api;

import java.util.UUID;

public record TransactionResponse(
        UUID transactionId,
        String transactionReference,
        String status,
        String message
) {
}
