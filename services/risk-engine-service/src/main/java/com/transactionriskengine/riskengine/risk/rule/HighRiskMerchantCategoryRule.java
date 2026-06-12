package com.transactionriskengine.riskengine.risk.rule;

import com.transactionriskengine.riskengine.messaging.event.TransactionCreatedEvent;
import com.transactionriskengine.riskengine.risk.domain.HighRiskMerchantCategory;
import com.transactionriskengine.riskengine.risk.domain.RiskRule;
import com.transactionriskengine.riskengine.risk.domain.TriggeredRule;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class HighRiskMerchantCategoryRule implements RiskRuleCondition {

    @Override
    public String ruleCode() {
        return "HIGH_RISK_MERCHANT_CATEGORY";
    }

    @Override
    public Optional<TriggeredRule> evaluate(RiskRule rule, TransactionCreatedEvent event) {
        return RuleEvaluationSupport.triggeredWhen(
                rule,
                HighRiskMerchantCategory.contains(event.merchantCategory()),
                "Merchant category %s is high risk".formatted(event.merchantCategory())
        );
    }
}
