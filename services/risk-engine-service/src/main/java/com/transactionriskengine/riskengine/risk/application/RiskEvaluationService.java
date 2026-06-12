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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class RiskEvaluationService {

    private final RiskRuleEvaluator riskRuleEvaluator;
    private final RiskScoreCalculator riskScoreCalculator;
    private final RiskDecisionRepository riskDecisionRepository;
    private final RiskDecisionReasonBuilder riskDecisionReasonBuilder;
    private final RiskOutboxEventRepository riskOutboxEventRepository;
    private final ObjectMapper objectMapper;
    private final String riskEvaluatedTopic;

    public RiskEvaluationService(
            RiskRuleEvaluator riskRuleEvaluator,
            RiskScoreCalculator riskScoreCalculator,
            RiskDecisionRepository riskDecisionRepository,
            RiskDecisionReasonBuilder riskDecisionReasonBuilder,
            RiskOutboxEventRepository riskOutboxEventRepository,
            ObjectMapper objectMapper,
            @Value("${transaction-risk-engine.kafka.topics.risk-evaluated}")
            String riskEvaluatedTopic
    ) {
        this.riskRuleEvaluator = riskRuleEvaluator;
        this.riskScoreCalculator = riskScoreCalculator;
        this.riskDecisionRepository = riskDecisionRepository;
        this.riskDecisionReasonBuilder = riskDecisionReasonBuilder;
        this.riskOutboxEventRepository = riskOutboxEventRepository;
        this.objectMapper = objectMapper;
        this.riskEvaluatedTopic = riskEvaluatedTopic;
    }

    @Transactional
    public void evaluate(TransactionCreatedEvent event) {
        if (riskDecisionRepository.existsByTransactionId(event.transactionId())) {
            log.info("Skipping duplicate risk evaluation for transactionId={}", event.transactionId());
            return;
        }

        List<TriggeredRule> triggeredRules = riskRuleEvaluator.evaluate(event);
        int riskScore = riskScoreCalculator.calculate(triggeredRules);
        RiskLevel riskLevel = riskScoreCalculator.determineRiskLevel(riskScore);
        DecisionStatus decisionStatus = riskScoreCalculator.determineDecisionStatus(riskLevel);
        String decisionReason = riskDecisionReasonBuilder.build(triggeredRules);

        RiskDecision decision = new RiskDecision(
                event.transactionId(),
                event.transactionReference(),
                event.customerId(),
                event.accountId(),
                riskScore,
                riskLevel,
                decisionStatus,
                decisionReason,
                triggeredRules
        );

        riskDecisionRepository.save(decision);
        UUID eventId = UUID.randomUUID();
        RiskEvaluatedEvent riskEvaluatedEvent = new RiskEvaluatedEvent(
                eventId,
                decision.getId(),
                decision.getTransactionId(),
                decision.getTransactionReference(),
                decision.getCustomerId(),
                decision.getAccountId(),
                decision.getRiskScore(),
                decision.getRiskLevel(),
                decision.getDecisionStatus(),
                decision.getDecisionReason(),
                decision.getTriggeredRules(),
                decision.getEvaluatedAt()
        );
        riskOutboxEventRepository.save(new RiskOutboxEvent(
                eventId,
                decision.getTransactionId(),
                "RISK_EVALUATED",
                riskEvaluatedTopic,
                objectMapper.writeValueAsString(riskEvaluatedEvent)
        ));

        log.info(
                "Saved risk decision for transactionId={} with score={}, level={}, status={}, triggeredRules={}",
                event.transactionId(),
                riskScore,
                riskLevel,
                decisionStatus,
                triggeredRules.size()
        );
    }
}
