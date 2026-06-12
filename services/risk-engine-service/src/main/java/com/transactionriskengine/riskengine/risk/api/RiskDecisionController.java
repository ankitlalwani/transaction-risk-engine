package com.transactionriskengine.riskengine.risk.api;

import com.transactionriskengine.riskengine.risk.application.RiskDecisionQueryService;
import com.transactionriskengine.riskengine.risk.domain.DecisionStatus;
import com.transactionriskengine.riskengine.risk.domain.RiskLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/risk-decisions")
@RequiredArgsConstructor
@Slf4j
public class RiskDecisionController {

    private final RiskDecisionQueryService riskDecisionQueryService;

    @GetMapping
    public List<RiskDecisionResponse> findRiskDecisions(
            @RequestParam(required = false) RiskLevel riskLevel,
            @RequestParam(required = false) DecisionStatus decisionStatus
    ) {
        log.info(
                "Querying risk decisions with riskLevel={} and decisionStatus={}",
                riskLevel,
                decisionStatus
        );
        return riskDecisionQueryService.find(riskLevel, decisionStatus);
    }

    @GetMapping("/{transactionId}")
    public RiskDecisionResponse getRiskDecision(@PathVariable UUID transactionId) {
        log.info("In RiskDecisionController.getRiskDecision({})", transactionId);
        return riskDecisionQueryService.getByTransactionId(transactionId);
    }
}
