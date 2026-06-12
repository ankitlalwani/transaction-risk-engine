package com.transactionriskengine.alertservice.alert.api.request;

import com.transactionriskengine.alertservice.alert.domain.AlertStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateAlertStatusRequest(
        @NotNull AlertStatus status,
        @NotBlank String assignedTo
) {
}
