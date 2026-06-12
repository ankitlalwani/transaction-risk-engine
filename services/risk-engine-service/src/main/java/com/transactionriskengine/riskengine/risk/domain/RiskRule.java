package com.transactionriskengine.riskengine.risk.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "risk_rules")
public class RiskRule {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String ruleCode;

    @Column(nullable = false)
    private String ruleName;

    @Column(nullable = false)
    private String ruleDescription;

    @Column(nullable = false)
    private String ruleType;

    @Column(nullable = false)
    private int scoreImpact;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected RiskRule() {
    }

    public RiskRule(String ruleCode, String ruleName, String ruleDescription, String ruleType, int scoreImpact) {
        Instant now = Instant.now();
        this.id = UUID.randomUUID();
        this.ruleCode = ruleCode;
        this.ruleName = ruleName;
        this.ruleDescription = ruleDescription;
        this.ruleType = ruleType;
        this.scoreImpact = scoreImpact;
        this.active = true;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public String getRuleName() {
        return ruleName;
    }

    public int getScoreImpact() {
        return scoreImpact;
    }
}
