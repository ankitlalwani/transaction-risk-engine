package com.transactionriskengine.alertservice.alert.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "alerts")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Alert {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "alert_reference", nullable = false, unique = true)
    private String alertReference;

    @Column(name = "transaction_id", nullable = false, unique = true)
    private UUID transactionId;

    @Column(name = "transaction_reference", nullable = false)
    private String transactionReference;

    @Column(name = "risk_decision_id")
    private UUID riskDecisionId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "risk_score", nullable = false)
    private Integer riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private RiskLevel riskLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision_status", nullable = false)
    private DecisionStatus decisionStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_status", nullable = false)
    private AlertStatus alertStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_priority", nullable = false)
    private AlertPriority alertPriority;

    @Column(name = "alert_reason", nullable = false, columnDefinition = "TEXT")
    private String alertReason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "triggered_rules", nullable = false, columnDefinition = "JSONB")
    private String triggeredRules;

    @Column(name = "assigned_to")
    private String assignedTo;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    public void updateStatus(AlertStatus status, String assignedTo) {
        Instant now = Instant.now();
        this.alertStatus = status;
        this.assignedTo = assignedTo;
        this.updatedAt = now;
        this.closedAt = isClosedStatus(status) ? now : null;
    }

    private boolean isClosedStatus(AlertStatus status) {
        return status == AlertStatus.CLOSED
                || status == AlertStatus.FALSE_POSITIVE;
    }
}
