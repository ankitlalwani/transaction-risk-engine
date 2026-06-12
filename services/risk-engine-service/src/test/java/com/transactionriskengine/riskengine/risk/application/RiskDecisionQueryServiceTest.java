package com.transactionriskengine.riskengine.risk.application;

import com.transactionriskengine.riskengine.common.exception.RiskDecisionNotFoundException;
import com.transactionriskengine.riskengine.risk.api.RiskDecisionResponse;
import com.transactionriskengine.riskengine.risk.domain.DecisionStatus;
import com.transactionriskengine.riskengine.risk.domain.RiskDecision;
import com.transactionriskengine.riskengine.risk.domain.RiskLevel;
import com.transactionriskengine.riskengine.risk.domain.TriggeredRule;
import com.transactionriskengine.riskengine.risk.repository.RiskDecisionRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RiskDecisionQueryServiceTest {

    private final RiskDecisionRepository riskDecisionRepository =
            mock(RiskDecisionRepository.class);
    private final RiskDecisionQueryService queryService =
            new RiskDecisionQueryService(riskDecisionRepository);

    @Test
    void getByTransactionIdReturnsRiskDecisionResponse() {
        UUID transactionId = UUID.randomUUID();
        TriggeredRule triggeredRule = new TriggeredRule(
                "HIGH_AMOUNT_TRANSACTION",
                "High Amount Transaction",
                40,
                "Transaction amount 15000.00 exceeded threshold 10000."
        );
        RiskDecision decision = new RiskDecision(
                transactionId,
                "TXN-20260609-000001",
                UUID.randomUUID(),
                UUID.randomUUID(),
                100,
                RiskLevel.CRITICAL,
                DecisionStatus.REVIEW_REQUIRED,
                "Transaction matched 1 risk rule(s): HIGH_AMOUNT_TRANSACTION.",
                List.of(triggeredRule)
        );
        when(riskDecisionRepository.findByTransactionId(transactionId))
                .thenReturn(Optional.of(decision));

        RiskDecisionResponse response = queryService.getByTransactionId(transactionId);

        assertThat(response.transactionId()).isEqualTo(transactionId);
        assertThat(response.transactionReference()).isEqualTo("TXN-20260609-000001");
        assertThat(response.riskScore()).isEqualTo(100);
        assertThat(response.riskLevel()).isEqualTo(RiskLevel.CRITICAL);
        assertThat(response.decisionStatus()).isEqualTo(DecisionStatus.REVIEW_REQUIRED);
        assertThat(response.triggeredRules()).containsExactly(triggeredRule);
        assertThat(response.evaluatedAt()).isNotNull();
    }

    @Test
    void getByTransactionIdThrowsWhenDecisionDoesNotExist() {
        UUID transactionId = UUID.randomUUID();
        when(riskDecisionRepository.findByTransactionId(transactionId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> queryService.getByTransactionId(transactionId))
                .isInstanceOf(RiskDecisionNotFoundException.class)
                .hasMessage("Risk decision not found for transactionId=" + transactionId);
    }

    @Test
    void findFiltersByRiskLevel() {
        RiskDecision decision = decision(RiskLevel.HIGH, DecisionStatus.REVIEW_REQUIRED);
        when(riskDecisionRepository.findByRiskLevelOrderByEvaluatedAtDesc(RiskLevel.HIGH))
                .thenReturn(List.of(decision));

        List<RiskDecisionResponse> responses = queryService.find(RiskLevel.HIGH, null);

        assertThat(responses)
                .extracting(RiskDecisionResponse::riskLevel)
                .containsExactly(RiskLevel.HIGH);
        verify(riskDecisionRepository)
                .findByRiskLevelOrderByEvaluatedAtDesc(RiskLevel.HIGH);
    }

    @Test
    void findFiltersByDecisionStatus() {
        RiskDecision decision = decision(RiskLevel.CRITICAL, DecisionStatus.REVIEW_REQUIRED);
        when(riskDecisionRepository.findByDecisionStatusOrderByEvaluatedAtDesc(
                DecisionStatus.REVIEW_REQUIRED
        )).thenReturn(List.of(decision));

        List<RiskDecisionResponse> responses =
                queryService.find(null, DecisionStatus.REVIEW_REQUIRED);

        assertThat(responses)
                .extracting(RiskDecisionResponse::decisionStatus)
                .containsExactly(DecisionStatus.REVIEW_REQUIRED);
        verify(riskDecisionRepository)
                .findByDecisionStatusOrderByEvaluatedAtDesc(DecisionStatus.REVIEW_REQUIRED);
    }

    private RiskDecision decision(RiskLevel riskLevel, DecisionStatus decisionStatus) {
        return new RiskDecision(
                UUID.randomUUID(),
                "TXN-20260609-000001",
                UUID.randomUUID(),
                UUID.randomUUID(),
                80,
                riskLevel,
                decisionStatus,
                "Transaction matched risk rules.",
                List.of()
        );
    }
}
