package com.transactionriskengine.alertservice.alert.application;

import com.transactionriskengine.alertservice.alert.api.response.AlertResponse;
import com.transactionriskengine.alertservice.alert.domain.Alert;
import com.transactionriskengine.alertservice.alert.domain.AlertStatus;
import com.transactionriskengine.alertservice.alert.mapper.AlertMapper;
import com.transactionriskengine.alertservice.alert.repository.AlertRepository;
import com.transactionriskengine.alertservice.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlertStatusUpdateService {

    private final AlertRepository alertRepository;
    private final AlertMapper alertMapper;

    @Transactional
    public AlertResponse updateStatus(
            UUID alertId,
            AlertStatus status,
            String assignedTo
    ) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Alert not found for id=" + alertId));

        alert.updateStatus(status, assignedTo);
        return alertMapper.toResponse(alert);
    }
}
