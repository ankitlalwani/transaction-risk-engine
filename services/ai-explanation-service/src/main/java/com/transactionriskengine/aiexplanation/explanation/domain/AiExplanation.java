package com.transactionriskengine.aiexplanation.explanation.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ai_explanations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiExplanation {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "risk_decision_id")
    private UUID riskDecisionId;

    @Column(name = "transaction_id", nullable = false, unique = true)
    private UUID transactionId;

    @Column(name = "transaction_reference", nullable = false)
    private String transactionReference;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "risk_score", nullable = false)
    private Integer riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private RiskLevel riskLevel;

    @Column(name = "decision_status", nullable = false)
    private String decisionStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "explanation_status", nullable = false)
    private ExplanationStatus explanationStatus;

    @Column(name = "explanation_text", nullable = false, columnDefinition = "TEXT")
    private String explanationText;

    @Column(name = "recommended_action", nullable = false, columnDefinition = "TEXT")
    private String recommendedAction;

    @Column(name = "analyst_summary", nullable = false, columnDefinition = "TEXT")
    private String analystSummary;

    @Column(name = "source_event_id")
    private UUID sourceEventId;

    @Column(name = "prompt_version")
    private String promptVersion;

    @Column(name = "model_provider")
    private String modelProvider;

    @Column(name = "model_name")
    private String modelName;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}