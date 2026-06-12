package com.transactionriskengine.riskengine.risk.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "risk_decisions")
public class RiskDecision {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID transactionId;

    @Column(nullable = false)
    private String transactionReference;

    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private UUID accountId;

    @Column(nullable = false)
    private int riskScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskLevel riskLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DecisionStatus decisionStatus;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String decisionReason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "JSONB")
    private List<TriggeredRule> triggeredRules;

    @Column(nullable = false)
    private Instant evaluatedAt;

    @Column(nullable = false)
    private Instant createdAt;

    protected RiskDecision() {
    }

    public RiskDecision(
            UUID transactionId,
            String transactionReference,
            UUID customerId,
            UUID accountId,
            int riskScore,
            RiskLevel riskLevel,
            DecisionStatus decisionStatus,
            String decisionReason,
            List<TriggeredRule> triggeredRules
    ) {
        Instant now = Instant.now();
        this.id = UUID.randomUUID();
        this.transactionId = transactionId;
        this.transactionReference = transactionReference;
        this.customerId = customerId;
        this.accountId = accountId;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.decisionStatus = decisionStatus;
        this.decisionReason = decisionReason;
        this.triggeredRules = List.copyOf(triggeredRules);
        this.evaluatedAt = now;
        this.createdAt = now;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public DecisionStatus getDecisionStatus() {
        return decisionStatus;
    }

    public String getDecisionReason() {
        return decisionReason;
    }

    public List<TriggeredRule> getTriggeredRules() {
        return triggeredRules;
    }

    public Instant getEvaluatedAt() {
        return evaluatedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
