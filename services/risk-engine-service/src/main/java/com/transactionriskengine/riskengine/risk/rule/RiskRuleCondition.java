package com.transactionriskengine.riskengine.risk.rule;

import com.transactionriskengine.riskengine.messaging.event.TransactionCreatedEvent;
import com.transactionriskengine.riskengine.risk.domain.RiskRule;
import com.transactionriskengine.riskengine.risk.domain.TriggeredRule;

import java.util.Optional;

public interface RiskRuleCondition {

    String ruleCode();

    Optional<TriggeredRule> evaluate(RiskRule rule, TransactionCreatedEvent event);
}
