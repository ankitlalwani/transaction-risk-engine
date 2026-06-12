package com.transactionriskengine.riskengine.risk.application;

import com.transactionriskengine.riskengine.messaging.event.TransactionCreatedEvent;
import com.transactionriskengine.riskengine.risk.domain.RiskRule;
import com.transactionriskengine.riskengine.risk.domain.TriggeredRule;
import com.transactionriskengine.riskengine.risk.repository.RiskRuleRepository;
import com.transactionriskengine.riskengine.risk.rule.RiskRuleCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RiskRuleEvaluator {

    private final RiskRuleRepository riskRuleRepository;
    private final Map<String, RiskRuleCondition> conditionsByRuleCode;

    public RiskRuleEvaluator(
            RiskRuleRepository riskRuleRepository,
            List<RiskRuleCondition> ruleConditions
    ) {
        this.riskRuleRepository = riskRuleRepository;
        this.conditionsByRuleCode = ruleConditions.stream()
                .collect(Collectors.toUnmodifiableMap(
                        RiskRuleCondition::ruleCode,
                        Function.identity()
                ));
    }

    public List<TriggeredRule> evaluate(TransactionCreatedEvent event) {
        return riskRuleRepository.findByActiveTrue()
                .stream()
                .flatMap(rule -> evaluate(rule, event).stream())
                .toList();
    }

    private java.util.Optional<TriggeredRule> evaluate(
            RiskRule rule,
            TransactionCreatedEvent event
    ) {
        RiskRuleCondition condition = conditionsByRuleCode.get(rule.getRuleCode());

        if (condition == null) {
            log.warn("No RiskRuleCondition implementation found for rule code {}", rule.getRuleCode());
            return java.util.Optional.empty();
        }

        return condition.evaluate(rule, event);
    }
}
