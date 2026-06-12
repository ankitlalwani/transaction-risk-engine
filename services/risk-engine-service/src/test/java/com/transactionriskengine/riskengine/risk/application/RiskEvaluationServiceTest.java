package com.transactionriskengine.riskengine.risk.application;

import com.transactionriskengine.riskengine.messaging.event.TransactionCreatedEvent;
import com.transactionriskengine.riskengine.messaging.event.RiskEvaluatedEvent;
import com.transactionriskengine.riskengine.messaging.outbox.RiskOutboxEvent;
import com.transactionriskengine.riskengine.messaging.outbox.RiskOutboxEventRepository;
import com.transactionriskengine.riskengine.risk.domain.DecisionStatus;
import com.transactionriskengine.riskengine.risk.domain.RiskDecision;
import com.transactionriskengine.riskengine.risk.domain.RiskLevel;
import com.transactionriskengine.riskengine.risk.domain.TriggeredRule;
import com.transactionriskengine.riskengine.risk.repository.RiskDecisionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

class RiskEvaluationServiceTest {

    private final RiskRuleEvaluator riskRuleEvaluator = mock(RiskRuleEvaluator.class);
    private final RiskScoreCalculator riskScoreCalculator = mock(RiskScoreCalculator.class);
    private final RiskDecisionRepository riskDecisionRepository = mock(RiskDecisionRepository.class);
    private final RiskDecisionReasonBuilder riskDecisionReasonBuilder = mock(RiskDecisionReasonBuilder.class);
    private final RiskOutboxEventRepository riskOutboxEventRepository =
            mock(RiskOutboxEventRepository.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RiskEvaluationService riskEvaluationService =
            new RiskEvaluationService(
                    riskRuleEvaluator,
                    riskScoreCalculator,
                    riskDecisionRepository,
                    riskDecisionReasonBuilder,
                    riskOutboxEventRepository,
                    objectMapper,
                    "risk.evaluated.v1"
            );

    @Test
    void evaluateCreatesAndSavesRiskDecision() {
        TransactionCreatedEvent event = event();
        List<TriggeredRule> triggeredRules = List.of(
                new TriggeredRule("HIGH_AMOUNT_TRANSACTION", "High amount", 60, "High amount"),
                new TriggeredRule("WIRE_TRANSFER_RISK", "Wire transfer", 50, "Wire transfer")
        );

        when(riskRuleEvaluator.evaluate(event)).thenReturn(triggeredRules);
        when(riskScoreCalculator.calculate(triggeredRules)).thenReturn(100);
        when(riskScoreCalculator.determineRiskLevel(100)).thenReturn(RiskLevel.CRITICAL);
        when(riskScoreCalculator.determineDecisionStatus(RiskLevel.CRITICAL))
                .thenReturn(DecisionStatus.REVIEW_REQUIRED);
        when(riskDecisionReasonBuilder.build(triggeredRules))
                .thenReturn("Transaction matched 2 risk rules.");
        when(riskDecisionRepository.existsByTransactionId(any())).thenReturn(false);

        riskEvaluationService.evaluate(event);

        ArgumentCaptor<RiskDecision> decisionCaptor = ArgumentCaptor.forClass(RiskDecision.class);
        verify(riskDecisionRepository).save(decisionCaptor.capture());

        RiskDecision decision = decisionCaptor.getValue();
        assertThat(decision.getTransactionId()).isEqualTo(event.transactionId());
        assertThat(decision.getRiskScore()).isEqualTo(100);
        assertThat(decision.getRiskLevel()).isEqualTo(RiskLevel.CRITICAL);
        assertThat(decision.getDecisionStatus()).isEqualTo(DecisionStatus.REVIEW_REQUIRED);
        assertThat(decision.getTriggeredRules()).isEqualTo(triggeredRules);

        ArgumentCaptor<RiskOutboxEvent> outboxCaptor =
                ArgumentCaptor.forClass(RiskOutboxEvent.class);
        verify(riskOutboxEventRepository).save(outboxCaptor.capture());

        RiskOutboxEvent outboxEvent = outboxCaptor.getValue();
        RiskEvaluatedEvent payload =
                objectMapper.readValue(outboxEvent.getPayload(), RiskEvaluatedEvent.class);
        assertThat(outboxEvent.getAggregateId()).isEqualTo(event.transactionId());
        assertThat(outboxEvent.getTopicName()).isEqualTo("risk.evaluated.v1");
        assertThat(payload.eventId()).isEqualTo(outboxEvent.getId());
        assertThat(payload.riskDecisionId()).isEqualTo(decision.getId());
        assertThat(payload.transactionId()).isEqualTo(event.transactionId());
        assertThat(payload.riskScore()).isEqualTo(100);
    }

    @Test
    void evaluateSkipsDuplicateTransaction() {
        TransactionCreatedEvent event = event();
        when(riskDecisionRepository.existsByTransactionId(event.transactionId())).thenReturn(true);

        riskEvaluationService.evaluate(event);

        verify(riskDecisionRepository, never()).save(any(RiskDecision.class));
        verify(riskOutboxEventRepository, never()).save(any(RiskOutboxEvent.class));
        verifyNoInteractions(riskRuleEvaluator, riskScoreCalculator, riskDecisionReasonBuilder);
    }

    private TransactionCreatedEvent event() {
        return new TransactionCreatedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "TXN-123",
                "TransactionReceived",
                "RECEIVED",
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("15000.00"),
                "USD",
                "TRANSFER",
                "WIRE",
                null,
                "WIRE_TRANSFER",
                "USA",
                "10.10.20.30",
                "desktop-device-004",
                Instant.parse("2026-06-03T15:30:00Z")
        );
    }
}
