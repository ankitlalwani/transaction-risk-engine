CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    external_customer_id VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(200) NOT NULL,
    email VARCHAR(200),
    phone_number VARCHAR(50),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_customer_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'BLOCKED'))
);

CREATE TABLE accounts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id UUID NOT NULL,
    external_account_id VARCHAR(100) NOT NULL UNIQUE,
    account_type VARCHAR(30) NOT NULL,
    account_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    current_balance NUMERIC(19, 4) NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_accounts_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(id),

    CONSTRAINT chk_account_type
        CHECK (account_type IN ('CHECKING', 'SAVINGS', 'CREDIT_CARD', 'LOAN')),

    CONSTRAINT chk_account_status
        CHECK (account_status IN ('ACTIVE', 'INACTIVE', 'FROZEN', 'CLOSED'))
);

CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    transaction_reference VARCHAR(100) NOT NULL UNIQUE,
    idempotency_key VARCHAR(150) NOT NULL UNIQUE,

    customer_id UUID NOT NULL,
    account_id UUID NOT NULL,

    transaction_type VARCHAR(40) NOT NULL,
    payment_channel VARCHAR(40) NOT NULL,

    amount NUMERIC(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',

    merchant_name VARCHAR(200),
    merchant_category VARCHAR(100),
    merchant_country VARCHAR(3),

    source_ip VARCHAR(100),
    device_id VARCHAR(150),

    transaction_status VARCHAR(40) NOT NULL DEFAULT 'RECEIVED',

    transaction_time TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_transactions_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(id),

    CONSTRAINT fk_transactions_account
        FOREIGN KEY (account_id)
        REFERENCES accounts(id),

    CONSTRAINT chk_transaction_type
        CHECK (transaction_type IN ('PURCHASE', 'TRANSFER', 'WITHDRAWAL', 'DEPOSIT', 'PAYMENT')),

    CONSTRAINT chk_payment_channel
        CHECK (payment_channel IN ('CARD', 'ACH', 'WIRE', 'ATM', 'ONLINE_BANKING', 'MOBILE_APP')),

    CONSTRAINT chk_transaction_status
        CHECK (transaction_status IN ('RECEIVED', 'PUBLISHED', 'PROCESSING', 'COMPLETED', 'FAILED')),

    CONSTRAINT chk_transaction_amount_positive
        CHECK (amount > 0)
);

CREATE TABLE outbox_events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id UUID NOT NULL,

    event_type VARCHAR(100) NOT NULL,
    topic_name VARCHAR(150) NOT NULL,

    payload JSONB NOT NULL,

    event_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    last_error TEXT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    published_at TIMESTAMPTZ,

    CONSTRAINT chk_outbox_event_status
        CHECK (event_status IN ('PENDING', 'PUBLISHED', 'FAILED'))
);

CREATE TABLE transaction_event_audit (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    event_id UUID,
    transaction_id UUID NOT NULL,
    topic_name VARCHAR(150) NOT NULL,
    event_type VARCHAR(100) NOT NULL,

    consumer_name VARCHAR(100) NOT NULL,
    processing_status VARCHAR(30) NOT NULL,

    error_message TEXT,

    received_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    processed_at TIMESTAMPTZ,

    CONSTRAINT fk_transaction_event_audit_transaction
        FOREIGN KEY (transaction_id)
        REFERENCES transactions(id),

    CONSTRAINT chk_event_audit_status
        CHECK (processing_status IN ('RECEIVED', 'PROCESSED', 'FAILED'))
);

CREATE INDEX idx_customers_external_customer_id
    ON customers(external_customer_id);

CREATE INDEX idx_accounts_customer_id
    ON accounts(customer_id);

CREATE INDEX idx_accounts_external_account_id
    ON accounts(external_account_id);

CREATE INDEX idx_transactions_customer_id
    ON transactions(customer_id);

CREATE INDEX idx_transactions_account_id
    ON transactions(account_id);

CREATE INDEX idx_transactions_transaction_time
    ON transactions(transaction_time);

CREATE INDEX idx_transactions_status
    ON transactions(transaction_status);

CREATE INDEX idx_transactions_customer_time
    ON transactions(customer_id, transaction_time DESC);

CREATE INDEX idx_outbox_events_status_created_at
    ON outbox_events(event_status, created_at);

CREATE INDEX idx_transaction_event_audit_transaction_id
    ON transaction_event_audit(transaction_id);
