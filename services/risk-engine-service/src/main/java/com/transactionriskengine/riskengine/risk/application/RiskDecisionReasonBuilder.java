package com.transactionriskengine.riskengine.risk.application;

import com.transactionriskengine.riskengine.risk.domain.TriggeredRule;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RiskDecisionReasonBuilder {

    public String build(List<TriggeredRule> triggeredRules) {
        if (triggeredRules == null || triggeredRules.isEmpty()) {
            return "No risk rules were triggered.";
        }

        String ruleCodes = triggeredRules.stream()
                .map(TriggeredRule::ruleCode)
                .collect(Collectors.joining(", "));

        return "Transaction matched " + triggeredRules.size()
                + " risk rule(s): " + ruleCodes + ".";
    }
}