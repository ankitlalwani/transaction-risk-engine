CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE ai_explanations (
                                 id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

                                 risk_decision_id UUID,
                                 transaction_id UUID NOT NULL,
                                 transaction_reference VARCHAR(100) NOT NULL,

                                 customer_id UUID NOT NULL,
                                 account_id UUID NOT NULL,

                                 risk_score INT NOT NULL,
                                 risk_level VARCHAR(30) NOT NULL,
                                 decision_status VARCHAR(50) NOT NULL,

                                 explanation_status VARCHAR(40) NOT NULL DEFAULT 'GENERATED',

                                 explanation_text TEXT NOT NULL,
                                 recommended_action TEXT NOT NULL,
                                 analyst_summary TEXT NOT NULL,

                                 source_event_id UUID,
                                 prompt_version VARCHAR(50),
                                 model_provider VARCHAR(50),
                                 model_name VARCHAR(100),

                                 created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                 updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                 CONSTRAINT uq_ai_explanations_transaction_id
                                     UNIQUE (transaction_id),

                                 CONSTRAINT chk_ai_explanation_risk_score
                                     CHECK (risk_score >= 0 AND risk_score <= 100),

                                 CONSTRAINT chk_ai_explanation_risk_level
                                     CHECK (risk_level IN (
                                                           'LOW',
                                                           'MEDIUM',
                                                           'HIGH',
                                                           'CRITICAL'
                                         )),

                                 CONSTRAINT chk_ai_explanation_decision_status
                                     CHECK (decision_status IN (
                                                                'APPROVED',
                                                                'MONITOR',
                                                                'REVIEW_REQUIRED',
                                                                'BLOCK_RECOMMENDED'
                                         )),

                                 CONSTRAINT chk_ai_explanation_status
                                     CHECK (explanation_status IN (
                                                                   'GENERATED',
                                                                   'FAILED',
                                                                   'SKIPPED'
                                         ))
);

CREATE INDEX idx_ai_explanations_transaction_id
    ON ai_explanations(transaction_id);

CREATE INDEX idx_ai_explanations_risk_decision_id
    ON ai_explanations(risk_decision_id);

CREATE INDEX idx_ai_explanations_risk_level
    ON ai_explanations(risk_level);

CREATE INDEX idx_ai_explanations_created_at
    ON ai_explanations(created_at DESC);