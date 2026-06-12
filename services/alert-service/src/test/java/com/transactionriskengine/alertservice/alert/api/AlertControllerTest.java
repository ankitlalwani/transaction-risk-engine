package com.transactionriskengine.alertservice.alert.api;

import com.transactionriskengine.alertservice.alert.api.response.AlertResponse;
import com.transactionriskengine.alertservice.alert.application.AlertQueryService;
import com.transactionriskengine.alertservice.alert.application.AlertStatusUpdateService;
import com.transactionriskengine.alertservice.alert.domain.AlertStatus;
import com.transactionriskengine.alertservice.alert.mapper.AlertMapper;
import com.transactionriskengine.alertservice.common.api.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class AlertControllerTest {

    private final AlertQueryService alertQueryService = mock(AlertQueryService.class);
    private final AlertStatusUpdateService alertStatusUpdateService =
            mock(AlertStatusUpdateService.class);
    private final AlertMapper alertMapper = mock(AlertMapper.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new AlertController(
                alertQueryService,
                alertStatusUpdateService,
                alertMapper
        )).setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void updateAlertStatusReturnsUpdatedAlert() throws Exception {
        UUID alertId = UUID.randomUUID();
        AlertResponse response = mock(AlertResponse.class);
        when(response.id()).thenReturn(alertId);
        when(response.alertStatus()).thenReturn(AlertStatus.IN_REVIEW);
        when(response.assignedTo()).thenReturn("analyst-1");
        when(alertStatusUpdateService.updateStatus(
                alertId,
                AlertStatus.IN_REVIEW,
                "analyst-1"
        )).thenReturn(response);

        mockMvc.perform(patch("/api/v1/alerts/{alertId}/status", alertId)
                        .contentType("application/json")
                        .content("""
                                {
                                  "status": "IN_REVIEW",
                                  "assignedTo": "analyst-1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(alertId.toString()))
                .andExpect(jsonPath("$.alertStatus").value("IN_REVIEW"))
                .andExpect(jsonPath("$.assignedTo").value("analyst-1"));
    }

    @Test
    void updateAlertStatusRejectsMissingAssignee() throws Exception {
        mockMvc.perform(patch("/api/v1/alerts/{alertId}/status", UUID.randomUUID())
                        .contentType("application/json")
                        .content("""
                                {
                                  "status": "IN_REVIEW"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("assignedTo must not be blank"));
    }
}
