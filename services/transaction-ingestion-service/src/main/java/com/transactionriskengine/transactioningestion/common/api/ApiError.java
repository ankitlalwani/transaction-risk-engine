package com.transactionriskengine.transactioningestion.common.api;

import java.time.Instant;

public record ApiError(
        String message,
        Instant timestamp
) {

    public static ApiError of(String message) {
        return new ApiError(message, Instant.now());
    }
}
