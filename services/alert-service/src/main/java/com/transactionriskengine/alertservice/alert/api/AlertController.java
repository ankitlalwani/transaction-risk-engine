package com.transactionriskengine.alertservice.alert.api;

import com.transactionriskengine.alertservice.alert.api.request.UpdateAlertStatusRequest;
import com.transactionriskengine.alertservice.alert.api.response.AlertResponse;
import com.transactionriskengine.alertservice.alert.application.AlertQueryService;
import com.transactionriskengine.alertservice.alert.application.AlertStatusUpdateService;
import com.transactionriskengine.alertservice.alert.domain.AlertPriority;
import com.transactionriskengine.alertservice.alert.domain.AlertStatus;
import com.transactionriskengine.alertservice.alert.domain.RiskLevel;
import com.transactionriskengine.alertservice.alert.mapper.AlertMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertQueryService alertQueryService;
    private final AlertStatusUpdateService alertStatusUpdateService;
    private final AlertMapper alertMapper;

    @GetMapping
    public List<AlertResponse> getAlerts(
            @RequestParam(required = false) AlertStatus status,
            @RequestParam(required = false) AlertPriority priority,
            @RequestParam(required = false) RiskLevel riskLevel
    ) {
        if (status != null) {
            return alertQueryService.getAlertsByStatus(status)
                    .stream()
                    .map(alertMapper::toResponse)
                    .toList();
        }

        if (priority != null) {
            return alertQueryService.getAlertsByPriority(priority)
                    .stream()
                    .map(alertMapper::toResponse)
                    .toList();
        }

        if (riskLevel != null) {
            return alertQueryService.getAlertsByRiskLevel(riskLevel)
                    .stream()
                    .map(alertMapper::toResponse)
                    .toList();
        }

        return alertQueryService.getRecentAlerts()
                .stream()
                .map(alertMapper::toResponse)
                .toList();
    }

    @GetMapping("/{alertId}")
    public AlertResponse getAlertById(@PathVariable UUID alertId) {
        return alertMapper.toResponse(alertQueryService.getAlertById(alertId));
    }

    @GetMapping("/transaction/{transactionId}")
    public AlertResponse getAlertByTransactionId(@PathVariable UUID transactionId) {
        return alertMapper.toResponse(alertQueryService.getAlertByTransactionId(transactionId));
    }

    @PatchMapping("/{alertId}/status")
    public AlertResponse updateAlertStatus(
            @PathVariable UUID alertId,
            @Valid @RequestBody UpdateAlertStatusRequest request
    ) {
        return updateAlert(alertId, request);
    }

    @PatchMapping("/{alertId}")
    public AlertResponse updateAlert(
            @PathVariable UUID alertId,
            @Valid @RequestBody UpdateAlertStatusRequest request
    ) {
        return alertStatusUpdateService.updateStatus(
                alertId,
                request.status(),
                request.assignedTo()
        );
    }
}
