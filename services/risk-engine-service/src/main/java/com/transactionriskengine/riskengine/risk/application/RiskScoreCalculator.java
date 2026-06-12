package com.transactionriskengine.riskengine.risk.application;

import com.transactionriskengine.riskengine.risk.domain.DecisionStatus;
import com.transactionriskengine.riskengine.risk.domain.RiskLevel;
import com.transactionriskengine.riskengine.risk.domain.TriggeredRule;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RiskScoreCalculator {

    public int calculate(List<TriggeredRule> triggeredRules) {
        int totalScore = triggeredRules.stream()
                .mapToInt(TriggeredRule::scoreImpact)
                .sum();

        return Math.min(totalScore, 100);
    }

    public RiskLevel determineRiskLevel(int riskScore) {
        if (riskScore >= 75) {
            return RiskLevel.CRITICAL;
        }

        if (riskScore >= 50) {
            return RiskLevel.HIGH;
        }

        if (riskScore >= 25) {
            return RiskLevel.MEDIUM;
        }

        return RiskLevel.LOW;
    }

    public DecisionStatus determineDecisionStatus(RiskLevel riskLevel) {
        return switch (riskLevel) {
            case LOW -> DecisionStatus.APPROVED;
            case MEDIUM -> DecisionStatus.MONITOR;
            case HIGH, CRITICAL -> DecisionStatus.REVIEW_REQUIRED;
        };
    }
}
