package com.transactionriskengine.riskengine.risk.application;

import com.transactionriskengine.riskengine.common.exception.RiskDecisionNotFoundException;
import com.transactionriskengine.riskengine.risk.api.RiskDecisionResponse;
import com.transactionriskengine.riskengine.risk.domain.DecisionStatus;
import com.transactionriskengine.riskengine.risk.domain.RiskDecision;
import com.transactionriskengine.riskengine.risk.domain.RiskLevel;
import com.transactionriskengine.riskengine.risk.repository.RiskDecisionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskDecisionQueryService {

    private final RiskDecisionRepository riskDecisionRepository;

    @Transactional(readOnly = true)
    public RiskDecisionResponse getByTransactionId(UUID transactionId) {
        return riskDecisionRepository.findByTransactionId(transactionId)
                .map(RiskDecisionResponse::from)
                .orElseThrow(() -> new RiskDecisionNotFoundException(transactionId));
    }

    @Transactional(readOnly = true)
    public List<RiskDecisionResponse> find(
            RiskLevel riskLevel,
            DecisionStatus decisionStatus
    ) {
        List<RiskDecision> decisions;

        if (riskLevel != null && decisionStatus != null) {
            decisions = riskDecisionRepository
                    .findByRiskLevelAndDecisionStatusOrderByEvaluatedAtDesc(
                            riskLevel,
                            decisionStatus
                    );
        } else if (riskLevel != null) {
            decisions = riskDecisionRepository
                    .findByRiskLevelOrderByEvaluatedAtDesc(riskLevel);
        } else if (decisionStatus != null) {
            decisions = riskDecisionRepository
                    .findByDecisionStatusOrderByEvaluatedAtDesc(decisionStatus);
        } else {
            decisions = riskDecisionRepository.findAllByOrderByEvaluatedAtDesc();
        }

        return decisions.stream()
                .map(RiskDecisionResponse::from)
                .toList();
    }
}
