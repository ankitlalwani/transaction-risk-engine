CREATE TABLE risk_outbox_events (
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

    CONSTRAINT chk_risk_outbox_event_status
        CHECK (event_status IN ('PENDING', 'PUBLISHED', 'FAILED'))
);

CREATE INDEX idx_risk_outbox_events_status_created_at
    ON risk_outbox_events(event_status, created_at);
