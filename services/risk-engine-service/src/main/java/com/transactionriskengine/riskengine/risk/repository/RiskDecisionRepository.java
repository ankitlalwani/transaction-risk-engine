package com.transactionriskengine.riskengine.risk.repository;

import com.transactionriskengine.riskengine.risk.domain.RiskDecision;
import com.transactionriskengine.riskengine.risk.domain.DecisionStatus;
import com.transactionriskengine.riskengine.risk.domain.RiskLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RiskDecisionRepository extends JpaRepository<RiskDecision, UUID> {

    boolean existsByTransactionId(UUID transactionId);

    Optional<RiskDecision> findByTransactionId(UUID transactionId);

    List<RiskDecision> findAllByOrderByEvaluatedAtDesc();

    List<RiskDecision> findByRiskLevelOrderByEvaluatedAtDesc(RiskLevel riskLevel);

    List<RiskDecision> findByDecisionStatusOrderByEvaluatedAtDesc(
            DecisionStatus decisionStatus
    );

    List<RiskDecision> findByRiskLevelAndDecisionStatusOrderByEvaluatedAtDesc(
            RiskLevel riskLevel,
            DecisionStatus decisionStatus
    );
}
