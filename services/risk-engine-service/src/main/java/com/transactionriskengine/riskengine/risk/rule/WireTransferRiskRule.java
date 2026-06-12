package com.transactionriskengine.riskengine.risk.rule;

import com.transactionriskengine.riskengine.messaging.event.TransactionCreatedEvent;
import com.transactionriskengine.riskengine.risk.domain.RiskRule;
import com.transactionriskengine.riskengine.risk.domain.TriggeredRule;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class WireTransferRiskRule implements RiskRuleCondition {

    @Override
    public String ruleCode() {
        return "WIRE_TRANSFER_RISK";
    }

    @Override
    public Optional<TriggeredRule> evaluate(RiskRule rule, TransactionCreatedEvent event) {
        return RuleEvaluationSupport.triggeredWhen(
                rule,
                RuleEvaluationSupport.equalsIgnoreCase(event.paymentChannel(), "WIRE"),
                "Payment channel is WIRE"
        );
    }
}
