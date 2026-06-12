CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE alerts (
                        id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

                        alert_reference VARCHAR(100) NOT NULL UNIQUE,

                        transaction_id UUID NOT NULL,
                        transaction_reference VARCHAR(100) NOT NULL,

                        risk_decision_id UUID,
                        customer_id UUID NOT NULL,
                        account_id UUID NOT NULL,

                        risk_score INT NOT NULL,
                        risk_level VARCHAR(30) NOT NULL,
                        decision_status VARCHAR(50) NOT NULL,

                        alert_status VARCHAR(40) NOT NULL DEFAULT 'OPEN',
                        alert_priority VARCHAR(40) NOT NULL,

                        alert_reason TEXT NOT NULL,
                        triggered_rules JSONB NOT NULL DEFAULT '[]'::jsonb,

                        assigned_to VARCHAR(150),

                        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                        closed_at TIMESTAMPTZ,

                        CONSTRAINT uq_alert_transaction_id
                            UNIQUE (transaction_id),

                        CONSTRAINT chk_alert_risk_score
                            CHECK (risk_score >= 0 AND risk_score <= 100),

                        CONSTRAINT chk_alert_risk_level
                            CHECK (risk_level IN (
                                                  'LOW',
                                                  'MEDIUM',
                                                  'HIGH',
                                                  'CRITICAL'
                                )),

                        CONSTRAINT chk_alert_decision_status
                            CHECK (decision_status IN (
                                                       'APPROVED',
                                                       'MONITOR',
                                                       'REVIEW_REQUIRED',
                                                       'BLOCK_RECOMMENDED'
                                )),

                        CONSTRAINT chk_alert_status
                            CHECK (alert_status IN (
                                                    'OPEN',
                                                    'IN_REVIEW',
                                                    'ESCALATED',
                                                    'CLOSED',
                                                    'FALSE_POSITIVE'
                                )),

                        CONSTRAINT chk_alert_priority
                            CHECK (alert_priority IN (
                                                      'LOW',
                                                      'MEDIUM',
                                                      'HIGH',
                                                      'CRITICAL'
                                ))
);

CREATE INDEX idx_alerts_transaction_id
    ON alerts(transaction_id);

CREATE INDEX idx_alerts_risk_decision_id
    ON alerts(risk_decision_id);

CREATE INDEX idx_alerts_customer_id
    ON alerts(customer_id);

CREATE INDEX idx_alerts_account_id
    ON alerts(account_id);

CREATE INDEX idx_alerts_risk_level
    ON alerts(risk_level);

CREATE INDEX idx_alerts_decision_status
    ON alerts(decision_status);

CREATE INDEX idx_alerts_alert_status
    ON alerts(alert_status);

CREATE INDEX idx_alerts_alert_priority
    ON alerts(alert_priority);

CREATE INDEX idx_alerts_created_at
    ON alerts(created_at DESC);

CREATE INDEX idx_alerts_status_priority_created
    ON alerts(alert_status, alert_priority, created_at DESC);