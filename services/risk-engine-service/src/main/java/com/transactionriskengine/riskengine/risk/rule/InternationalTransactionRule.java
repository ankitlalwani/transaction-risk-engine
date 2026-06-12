package com.transactionriskengine.riskengine.risk.rule;

import com.transactionriskengine.riskengine.messaging.event.TransactionCreatedEvent;
import com.transactionriskengine.riskengine.risk.domain.RiskRule;
import com.transactionriskengine.riskengine.risk.domain.TriggeredRule;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class InternationalTransactionRule implements RiskRuleCondition {

    @Override
    public String ruleCode() {
        return "INTERNATIONAL_TRANSACTION";
    }

    @Override
    public Optional<TriggeredRule> evaluate(RiskRule rule, TransactionCreatedEvent event) {
        boolean matched = RuleEvaluationSupport.hasText(event.merchantCountry())
                && !RuleEvaluationSupport.equalsIgnoreCase(event.merchantCountry(), "USA");

        return RuleEvaluationSupport.triggeredWhen(
                rule,
                matched,
                "Merchant country %s is outside USA".formatted(event.merchantCountry())
        );
    }
}
