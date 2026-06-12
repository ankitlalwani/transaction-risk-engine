package com.transactionriskengine.alertservice.alert.application;

import com.transactionriskengine.alertservice.alert.api.response.AlertResponse;
import com.transactionriskengine.alertservice.alert.domain.Alert;
import com.transactionriskengine.alertservice.alert.domain.AlertStatus;
import com.transactionriskengine.alertservice.alert.mapper.AlertMapper;
import com.transactionriskengine.alertservice.alert.repository.AlertRepository;
import com.transactionriskengine.alertservice.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlertStatusUpdateServiceTest {

    private final AlertRepository alertRepository = mock(AlertRepository.class);
    private final AlertMapper alertMapper = mock(AlertMapper.class);
    private final AlertStatusUpdateService service =
            new AlertStatusUpdateService(alertRepository, alertMapper);

    @Test
    void updateStatusAssignsAlertAndUpdatesTimestamp() {
        UUID alertId = UUID.randomUUID();
        Alert alert = Alert.builder()
                .id(alertId)
                .alertStatus(AlertStatus.OPEN)
                .createdAt(Instant.parse("2026-06-12T12:00:00Z"))
                .updatedAt(Instant.parse("2026-06-12T12:00:00Z"))
                .build();
        AlertResponse response = mock(AlertResponse.class);
        when(alertRepository.findById(alertId)).thenReturn(Optional.of(alert));
        when(alertMapper.toResponse(alert)).thenReturn(response);

        AlertResponse result =
                service.updateStatus(alertId, AlertStatus.IN_REVIEW, "analyst-1");

        assertThat(result).isSameAs(response);
        assertThat(alert.getAlertStatus()).isEqualTo(AlertStatus.IN_REVIEW);
        assertThat(alert.getAssignedTo()).isEqualTo("analyst-1");
        assertThat(alert.getUpdatedAt()).isAfter(Instant.parse("2026-06-12T12:00:00Z"));
        assertThat(alert.getClosedAt()).isNull();
        verify(alertMapper).toResponse(alert);
    }

    @Test
    void updateStatusThrowsWhenAlertDoesNotExist() {
        UUID alertId = UUID.randomUUID();
        when(alertRepository.findById(alertId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.updateStatus(alertId, AlertStatus.IN_REVIEW, "analyst-1"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Alert not found for id=" + alertId);
    }
}
