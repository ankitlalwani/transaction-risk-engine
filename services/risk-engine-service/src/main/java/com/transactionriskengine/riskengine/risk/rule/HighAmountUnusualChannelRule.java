package com.transactionriskengine.riskengine.risk.rule;

import com.transactionriskengine.riskengine.messaging.event.TransactionCreatedEvent;
import com.transactionriskengine.riskengine.risk.domain.RiskRule;
import com.transactionriskengine.riskengine.risk.domain.TriggeredRule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class HighAmountUnusualChannelRule implements RiskRuleCondition {

    private static final BigDecimal HIGH_AMOUNT_THRESHOLD = BigDecimal.valueOf(5_000);

    @Override
    public String ruleCode() {
        return "HIGH_AMOUNT_UNUSUAL_CHANNEL";
    }

    @Override
    public Optional<TriggeredRule> evaluate(RiskRule rule, TransactionCreatedEvent event) {
        boolean highAmount = event.amount() != null
                && event.amount().compareTo(HIGH_AMOUNT_THRESHOLD) >= 0;
        boolean unusualChannel = RuleEvaluationSupport.equalsIgnoreCase(event.paymentChannel(), "ATM")
                || RuleEvaluationSupport.equalsIgnoreCase(event.paymentChannel(), "MOBILE_APP");

        return RuleEvaluationSupport.triggeredWhen(
                rule,
                highAmount && unusualChannel,
                "High-value transaction %s used channel %s".formatted(
                        event.amount(),
                        event.paymentChannel()
                )
        );
    }
}
