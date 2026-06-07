package com.riskpulse.transactioningestion.messaging.transactionEventAudit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transaction_event_audit")
public class TransactionEvent {

    @Id
    private UUID id;

    private UUID eventId;

    @Column(nullable = false)
    private UUID transactionId;

    @Column(nullable = false)
    private String topicName;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String consumerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionEventProcessingStatus processingStatus;

    private String errorMessage;

    @Column(nullable = false)
    private Instant receivedAt;

    private Instant processedAt;

    protected TransactionEvent() {
    }

    public TransactionEvent(
            UUID eventId,
            UUID transactionId,
            String topicName,
            String eventType,
            String consumerName
    ) {
        this.id = UUID.randomUUID();
        this.eventId = eventId;
        this.transactionId = transactionId;
        this.topicName = topicName;
        this.eventType = eventType;
        this.consumerName = consumerName;
        this.processingStatus = TransactionEventProcessingStatus.RECEIVED;
        this.receivedAt = Instant.now();
    }

    public void markProcessed() {
        this.processingStatus = TransactionEventProcessingStatus.PROCESSED;
        this.processedAt = Instant.now();
        this.errorMessage = null;
    }

    public UUID getId() {
        return id;
    }

    public UUID getEventId() {
        return eventId;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public String getTopicName() {
        return topicName;
    }

    public String getEventType() {
        return eventType;
    }

    public String getConsumerName() {
        return consumerName;
    }

    public TransactionEventProcessingStatus getProcessingStatus() {
        return processingStatus;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}
