package com.transactionriskengine.riskengine.risk.repository;

import com.transactionriskengine.riskengine.risk.domain.RiskRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RiskRuleRepository extends JpaRepository<RiskRule, UUID> {
    List<RiskRule> findByActiveTrue();
}
