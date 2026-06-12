package com.transactionriskengine.riskengine.risk.rule;

import com.transactionriskengine.riskengine.messaging.event.TransactionCreatedEvent;
import com.transactionriskengine.riskengine.risk.domain.RiskRule;
import com.transactionriskengine.riskengine.risk.domain.TriggeredRule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class MediumAmountTransactionRule implements RiskRuleCondition {

    private static final BigDecimal LOWER_THRESHOLD = BigDecimal.valueOf(5_000);
    private static final BigDecimal UPPER_THRESHOLD = BigDecimal.valueOf(10_000);

    @Override
    public String ruleCode() {
        return "MEDIUM_AMOUNT_TRANSACTION";
    }

    @Override
    public Optional<TriggeredRule> evaluate(RiskRule rule, TransactionCreatedEvent event) {
        boolean matched = event.amount() != null
                && event.amount().compareTo(LOWER_THRESHOLD) >= 0
                && event.amount().compareTo(UPPER_THRESHOLD) < 0;

        return RuleEvaluationSupport.triggeredWhen(
                rule,
                matched,
                "Transaction amount %s is between %s and %s".formatted(
                        event.amount(),
                        LOWER_THRESHOLD,
                        UPPER_THRESHOLD
                )
        );
    }
}
