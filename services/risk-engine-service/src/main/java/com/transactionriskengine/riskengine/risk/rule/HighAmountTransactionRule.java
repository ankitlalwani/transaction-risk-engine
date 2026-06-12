package com.transactionriskengine.riskengine.risk.rule;

import com.transactionriskengine.riskengine.messaging.event.TransactionCreatedEvent;
import com.transactionriskengine.riskengine.risk.domain.RiskRule;
import com.transactionriskengine.riskengine.risk.domain.TriggeredRule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class HighAmountTransactionRule implements RiskRuleCondition {

    private static final BigDecimal THRESHOLD = BigDecimal.valueOf(10_000);

    @Override
    public String ruleCode() {
        return "HIGH_AMOUNT_TRANSACTION";
    }

    @Override
    public Optional<TriggeredRule> evaluate(RiskRule rule, TransactionCreatedEvent event) {
        boolean matched = event.amount() != null && event.amount().compareTo(THRESHOLD) >= 0;

        return RuleEvaluationSupport.triggeredWhen(
                rule,
                matched,
                "Transaction amount %s is at or above %s".formatted(event.amount(), THRESHOLD)
        );
    }
}
