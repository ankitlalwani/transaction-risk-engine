package com.riskpulse.transactioningestion.transaction.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String transactionReference;

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private UUID accountId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentChannel paymentChannel;

    private String merchantName;
    private String merchantCategory;
    private String merchantCountry;
    private String sourceIp;
    private String deviceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus transactionStatus;

    @Column(nullable = false)
    private Instant transactionTime;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Transaction() {
    }

    public Transaction(
            String idempotencyKey,
            UUID customerId,
            UUID accountId,
            BigDecimal amount,
            String currency,
            TransactionType transactionType,
            PaymentChannel paymentChannel,
            String merchantName,
            String merchantCategory,
            String merchantCountry,
            String sourceIp,
            String deviceId,
            Instant transactionTime
    ) {
        Instant now = Instant.now();

        this.id = UUID.randomUUID();
        this.transactionReference = "TXN-" + this.id;
        this.idempotencyKey = idempotencyKey;
        this.customerId = customerId;
        this.accountId = accountId;
        this.amount = amount;
        this.currency = currency;
        this.transactionType = transactionType;
        this.paymentChannel = paymentChannel;
        this.merchantName = merchantName;
        this.merchantCategory = merchantCategory;
        this.merchantCountry = merchantCountry;
        this.sourceIp = sourceIp;
        this.deviceId = deviceId;
        this.transactionStatus = TransactionStatus.RECEIVED;
        this.transactionTime = transactionTime;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public PaymentChannel getPaymentChannel() {
        return paymentChannel;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public String getMerchantCategory() {
        return merchantCategory;
    }

    public String getMerchantCountry() {
        return merchantCountry;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public Instant getTransactionTime() {
        return transactionTime;
    }
}
