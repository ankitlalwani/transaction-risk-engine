package com.transactionriskengine.alertservice.alert.application;

import com.transactionriskengine.alertservice.alert.api.response.AlertResponse;
import com.transactionriskengine.alertservice.alert.domain.Alert;
import com.transactionriskengine.alertservice.alert.domain.AlertPriority;
import com.transactionriskengine.alertservice.alert.domain.AlertStatus;
import com.transactionriskengine.alertservice.alert.domain.RiskLevel;
import com.transactionriskengine.alertservice.alert.mapper.AlertMapper;
import com.transactionriskengine.alertservice.alert.repository.AlertRepository;
import com.transactionriskengine.alertservice.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlertQueryService {

    private final AlertRepository alertRepository;

    public List<Alert> getRecentAlerts() {
        return alertRepository.findTop50ByOrderByCreatedAtDesc();
    }

    public Alert getAlertById(UUID alertId) {
        return alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found: " + alertId));
    }

    public Alert getAlertByTransactionId(UUID transactionId) {
        return alertRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found for transactionId: " + transactionId));
    }

    public List<Alert> getAlertsByStatus(AlertStatus status) {
        return alertRepository.findByAlertStatusOrderByCreatedAtDesc(status);
    }

    public List<Alert> getAlertsByPriority(AlertPriority priority) {
        return alertRepository.findByAlertPriorityOrderByCreatedAtDesc(priority);
    }

    public List<Alert> getAlertsByRiskLevel(RiskLevel riskLevel) {
        return alertRepository.findByRiskLevelOrderByCreatedAtDesc(riskLevel);
    }
}
