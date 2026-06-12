package com.transactionriskengine.riskengine.common.exception;

import java.util.UUID;

public class RiskDecisionNotFoundException extends RuntimeException {

    public RiskDecisionNotFoundException(UUID transactionId) {
        super("Risk decision not found for transactionId=" + transactionId);
    }
}
