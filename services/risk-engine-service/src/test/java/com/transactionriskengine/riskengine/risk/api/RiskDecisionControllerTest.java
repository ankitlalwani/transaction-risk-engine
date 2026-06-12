package com.transactionriskengine.riskengine.risk.api;

import com.transactionriskengine.riskengine.common.api.GlobalExceptionHandler;
import com.transactionriskengine.riskengine.common.exception.RiskDecisionNotFoundException;
import com.transactionriskengine.riskengine.risk.application.RiskDecisionQueryService;
import com.transactionriskengine.riskengine.risk.domain.DecisionStatus;
import com.transactionriskengine.riskengine.risk.domain.RiskLevel;
import com.transactionriskengine.riskengine.risk.domain.TriggeredRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class RiskDecisionControllerTest {

    private final RiskDecisionQueryService queryService =
            mock(RiskDecisionQueryService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new RiskDecisionController(queryService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getRiskDecisionReturnsDecision() throws Exception {
        UUID transactionId = UUID.fromString("7ff1d2ef-58f4-4dc1-9ff5-82f332392111");
        RiskDecisionResponse response = new RiskDecisionResponse(
                transactionId,
                "TXN-20260609-000001",
                100,
                RiskLevel.CRITICAL,
                DecisionStatus.REVIEW_REQUIRED,
                "Transaction matched 1 risk rule(s): HIGH_AMOUNT_TRANSACTION.",
                List.of(new TriggeredRule(
                        "HIGH_AMOUNT_TRANSACTION",
                        "High Amount Transaction",
                        40,
                        "Transaction amount 15000.00 exceeded threshold 10000."
                )),
                Instant.parse("2026-06-10T14:30:00Z")
        );
        when(queryService.getByTransactionId(transactionId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/risk-decisions/{transactionId}", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(transactionId.toString()))
                .andExpect(jsonPath("$.transactionReference").value("TXN-20260609-000001"))
                .andExpect(jsonPath("$.riskScore").value(100))
                .andExpect(jsonPath("$.riskLevel").value("CRITICAL"))
                .andExpect(jsonPath("$.decisionStatus").value("REVIEW_REQUIRED"))
                .andExpect(jsonPath("$.triggeredRules[0].ruleCode")
                        .value("HIGH_AMOUNT_TRANSACTION"))
                .andExpect(jsonPath("$.evaluatedAt").value("2026-06-10T14:30:00Z"));
    }

    @Test
    void getRiskDecisionReturnsNotFoundWhenDecisionDoesNotExist() throws Exception {
        UUID transactionId = UUID.randomUUID();
        when(queryService.getByTransactionId(transactionId))
                .thenThrow(new RiskDecisionNotFoundException(transactionId));

        mockMvc.perform(get("/api/v1/risk-decisions/{transactionId}", transactionId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Risk decision not found for transactionId=" + transactionId));
    }

    @Test
    void getRiskDecisionReturnsBadRequestForInvalidTransactionId() throws Exception {
        mockMvc.perform(get("/api/v1/risk-decisions/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("transactionId has an invalid value"));
    }

    @Test
    void findRiskDecisionsFiltersByRiskLevel() throws Exception {
        RiskDecisionResponse response = response(RiskLevel.HIGH, DecisionStatus.REVIEW_REQUIRED);
        when(queryService.find(RiskLevel.HIGH, null)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/risk-decisions")
                        .queryParam("riskLevel", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].riskLevel").value("HIGH"))
                .andExpect(jsonPath("$[0].decisionStatus").value("REVIEW_REQUIRED"));
    }

    @Test
    void findRiskDecisionsFiltersByDecisionStatus() throws Exception {
        RiskDecisionResponse response = response(RiskLevel.CRITICAL, DecisionStatus.REVIEW_REQUIRED);
        when(queryService.find(null, DecisionStatus.REVIEW_REQUIRED))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/risk-decisions")
                        .queryParam("decisionStatus", "REVIEW_REQUIRED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].riskLevel").value("CRITICAL"))
                .andExpect(jsonPath("$[0].decisionStatus").value("REVIEW_REQUIRED"));
    }

    @Test
    void findRiskDecisionsReturnsBadRequestForInvalidRiskLevel() throws Exception {
        mockMvc.perform(get("/api/v1/risk-decisions")
                        .queryParam("riskLevel", "VERY_HIGH"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("riskLevel has an invalid value: VERY_HIGH"));
    }

    private RiskDecisionResponse response(
            RiskLevel riskLevel,
            DecisionStatus decisionStatus
    ) {
        return new RiskDecisionResponse(
                UUID.randomUUID(),
                "TXN-20260609-000001",
                80,
                riskLevel,
                decisionStatus,
                "Transaction matched risk rules.",
                List.of(),
                Instant.parse("2026-06-10T14:30:00Z")
        );
    }
}
