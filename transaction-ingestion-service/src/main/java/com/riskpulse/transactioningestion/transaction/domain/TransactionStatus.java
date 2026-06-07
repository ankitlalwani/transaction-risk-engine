package com.riskpulse.transactioningestion.transaction.domain;

public enum TransactionStatus {
    RECEIVED,
    PUBLISHED,
    PROCESSING,
    COMPLETED,
    FAILED
}
