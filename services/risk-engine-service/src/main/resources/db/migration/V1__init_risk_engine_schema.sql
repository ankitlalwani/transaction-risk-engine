CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE risk_rules (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    rule_code VARCHAR(100) NOT NULL UNIQUE,
    rule_name VARCHAR(200) NOT NULL,
    rule_description TEXT NOT NULL,

    rule_type VARCHAR(50) NOT NULL,
    score_impact INT NOT NULL,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_risk_rule_type
        CHECK (rule_type IN (
            'AMOUNT',
            'PAYMENT_CHANNEL',
            'LOCATION',
            'MERCHANT_CATEGORY',
            'DEVICE',
            'COMPOSITE'
        )),

    CONSTRAINT chk_risk_rule_score_impact
        CHECK (score_impact >= 0 AND score_impact <= 100)
);

CREATE TABLE risk_decisions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    transaction_id UUID NOT NULL,
    transaction_reference VARCHAR(100) NOT NULL,

    customer_id UUID NOT NULL,
    account_id UUID NOT NULL,

    risk_score INT NOT NULL,
    risk_level VARCHAR(30) NOT NULL,
    decision_status VARCHAR(50) NOT NULL,

    decision_reason TEXT NOT NULL,

    triggered_rules JSONB NOT NULL DEFAULT '[]'::jsonb,

    evaluated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_risk_decisions_transaction_id
        UNIQUE (transaction_id),

    CONSTRAINT chk_risk_score
        CHECK (risk_score >= 0 AND risk_score <= 100),

    CONSTRAINT chk_risk_level
        CHECK (risk_level IN (
            'LOW',
            'MEDIUM',
            'HIGH',
            'CRITICAL'
        )),

    CONSTRAINT chk_decision_status
        CHECK (decision_status IN (
            'APPROVED',
            'MONITOR',
            'REVIEW_REQUIRED',
            'BLOCK_RECOMMENDED'
        ))
);

CREATE INDEX idx_risk_rules_active
    ON risk_rules(active);

CREATE INDEX idx_risk_rules_rule_type
    ON risk_rules(rule_type);

CREATE INDEX idx_risk_decisions_transaction_reference
    ON risk_decisions(transaction_reference);

CREATE INDEX idx_risk_decisions_customer_id
    ON risk_decisions(customer_id);

CREATE INDEX idx_risk_decisions_account_id
    ON risk_decisions(account_id);

CREATE INDEX idx_risk_decisions_risk_level
    ON risk_decisions(risk_level);

CREATE INDEX idx_risk_decisions_decision_status
    ON risk_decisions(decision_status);

CREATE INDEX idx_risk_decisions_evaluated_at
    ON risk_decisions(evaluated_at DESC);

CREATE INDEX idx_risk_decisions_customer_evaluated_at
    ON risk_decisions(customer_id, evaluated_at DESC);
