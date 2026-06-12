package com.transactionriskengine.riskengine.risk.application;

import com.transactionriskengine.riskengine.messaging.event.TransactionCreatedEvent;
import com.transactionriskengine.riskengine.risk.domain.RiskRule;
import com.transactionriskengine.riskengine.risk.domain.TriggeredRule;
import com.transactionriskengine.riskengine.risk.repository.RiskRuleRepository;
import com.transactionriskengine.riskengine.risk.rule.HighAmountTransactionRule;
import com.transactionriskengine.riskengine.risk.rule.HighRiskMerchantCategoryRule;
import com.transactionriskengine.riskengine.risk.rule.InternationalTransactionRule;
import com.transactionriskengine.riskengine.risk.rule.MissingDeviceIdRule;
import com.transactionriskengine.riskengine.risk.rule.MissingSourceIpRule;
import com.transactionriskengine.riskengine.risk.rule.WireTransferRiskRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiskRuleEvaluatorTest {

    @Mock
    private RiskRuleRepository riskRuleRepository;

    private RiskRuleEvaluator riskRuleEvaluator;

    @BeforeEach
    void setUp() {
        riskRuleEvaluator = new RiskRuleEvaluator(
                riskRuleRepository,
                List.of(
                        new HighAmountTransactionRule(),
                        new WireTransferRiskRule(),
                        new InternationalTransactionRule(),
                        new MissingDeviceIdRule(),
                        new HighRiskMerchantCategoryRule(),
                        new MissingSourceIpRule()
                )
        );
    }

    @Test
    void evaluateReturnsTriggeredRulesForMatchingConditions() {
        when(riskRuleRepository.findByActiveTrue()).thenReturn(List.of(
                rule("HIGH_AMOUNT_TRANSACTION", "High amount", 30),
                rule("WIRE_TRANSFER_RISK", "Wire transfer", 20),
                rule("INTERNATIONAL_TRANSACTION", "International transaction", 25),
                rule("MISSING_DEVICE_ID", "Missing device", 10)
        ));

        TransactionCreatedEvent event = event(
                new BigDecimal("15000.00"),
                "WIRE",
                "CAN",
                "WIRE_TRANSFER",
                null,
                "10.10.20.30"
        );

        List<TriggeredRule> triggeredRules = riskRuleEvaluator.evaluate(event);

        assertThat(triggeredRules)
                .extracting(TriggeredRule::ruleCode)
                .containsExactly(
                        "HIGH_AMOUNT_TRANSACTION",
                        "WIRE_TRANSFER_RISK",
                        "INTERNATIONAL_TRANSACTION",
                        "MISSING_DEVICE_ID"
                );
        assertThat(triggeredRules)
                .extracting(TriggeredRule::scoreImpact)
                .containsExactly(30, 20, 25, 10);
    }

    @Test
    void evaluateReturnsEmptyListWhenNoActiveRuleMatches() {
        when(riskRuleRepository.findByActiveTrue()).thenReturn(List.of(
                rule("HIGH_AMOUNT_TRANSACTION", "High amount", 30),
                rule("WIRE_TRANSFER_RISK", "Wire transfer", 20),
                rule("HIGH_RISK_MERCHANT_CATEGORY", "High-risk merchant", 25),
                rule("MISSING_SOURCE_IP", "Missing source IP", 10)
        ));

        TransactionCreatedEvent event = event(
                new BigDecimal("189.99"),
                "CARD",
                "USA",
                "ECOMMERCE",
                "web-browser-device-002",
                "172.16.10.45"
        );

        assertThat(riskRuleEvaluator.evaluate(event)).isEmpty();
    }

    @Test
    void evaluateTriggersForHighRiskMerchantCategory() {
        when(riskRuleRepository.findByActiveTrue()).thenReturn(List.of(
                rule("HIGH_RISK_MERCHANT_CATEGORY", "High-risk merchant", 25)
        ));

        TransactionCreatedEvent event = event(
                new BigDecimal("500.00"),
                "CARD",
                "USA",
                "crypto",
                "mobile-device-001",
                "172.16.10.45"
        );

        assertThat(riskRuleEvaluator.evaluate(event))
                .extracting(TriggeredRule::ruleCode)
                .containsExactly("HIGH_RISK_MERCHANT_CATEGORY");
    }

    private RiskRule rule(String code, String name, int scoreImpact) {
        return new RiskRule(code, name, name, "COMPOSITE", scoreImpact);
    }

    private TransactionCreatedEvent event(
            BigDecimal amount,
            String paymentChannel,
            String merchantCountry,
            String merchantCategory,
            String deviceId,
            String sourceIp
    ) {
        return new TransactionCreatedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "TXN-123",
                "TransactionReceived",
                "RECEIVED",
                UUID.randomUUID(),
                UUID.randomUUID(),
                amount,
                "USD",
                "TRANSFER",
                paymentChannel,
                null,
                merchantCategory,
                merchantCountry,
                sourceIp,
                deviceId,
                Instant.parse("2026-06-03T15:30:00Z")
        );
    }
}
