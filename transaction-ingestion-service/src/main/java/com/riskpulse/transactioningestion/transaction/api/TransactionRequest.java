package com.riskpulse.transactioningestion.transaction.api;

import com.riskpulse.transactioningestion.transaction.domain.PaymentChannel;
import com.riskpulse.transactioningestion.transaction.domain.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionRequest(
        @NotBlank
        String idempotencyKey,

        @NotBlank
        String externalCustomerId,

        @NotBlank
        String externalAccountId,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal amount,

        @NotBlank
        String currency,

        @NotNull
        TransactionType transactionType,

        @NotNull
        PaymentChannel paymentChannel,

        String merchantName,

        String merchantCategory,

        String merchantCountry,

        String sourceIp,

        String deviceId,

        @NotNull
        Instant transactionTime
) {
}
