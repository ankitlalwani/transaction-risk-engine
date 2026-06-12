package com.transactionriskengine.riskengine.messaging.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "risk_outbox_events")
public class RiskOutboxEvent {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String aggregateType;

    @Column(nullable = false)
    private UUID aggregateId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String topicName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "JSONB")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskOutboxEventStatus eventStatus;

    @Column(nullable = false)
    private int retryCount;

    private String lastError;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant publishedAt;

    protected RiskOutboxEvent() {
    }

    public RiskOutboxEvent(
            UUID id,
            UUID transactionId,
            String eventType,
            String topicName,
            String payload
    ) {
        this.id = id;
        this.aggregateType = "RISK_DECISION";
        this.aggregateId = transactionId;
        this.eventType = eventType;
        this.topicName = topicName;
        this.payload = payload;
        this.eventStatus = RiskOutboxEventStatus.PENDING;
        this.retryCount = 0;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    public String getTopicName() {
        return topicName;
    }

    public String getPayload() {
        return payload;
    }

    public RiskOutboxEventStatus getEventStatus() {
        return eventStatus;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public String getLastError() {
        return lastError;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void markPublished(Instant publishedAt) {
        this.eventStatus = RiskOutboxEventStatus.PUBLISHED;
        this.publishedAt = publishedAt;
        this.lastError = null;
    }

    public void recordFailure(String errorMessage, int maximumRetries) {
        this.retryCount++;
        this.lastError = errorMessage;

        if (retryCount >= maximumRetries) {
            this.eventStatus = RiskOutboxEventStatus.FAILED;
        }
    }
}
