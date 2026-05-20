CREATE TABLE IF NOT EXISTS bet_suggestions (
                                               id UUID PRIMARY KEY,
                                               match_id UUID NOT NULL,
                                               market VARCHAR(100) NOT NULL,
    picked_odd NUMERIC(10, 2) NOT NULL,
    picked_bookmaker VARCHAR(100) NOT NULL,
    confidence DOUBLE PRECISION NOT NULL,
    expected_value DOUBLE PRECISION NOT NULL,
    stake NUMERIC(15, 2),
    status VARCHAR(50) NOT NULL,
    actual_result VARCHAR(255),
    roi DOUBLE PRECISION,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_bet_suggestions_match FOREIGN KEY (match_id) REFERENCES matches(id) ON DELETE CASCADE
    );

CREATE INDEX idx_bet_suggestions_match_id ON bet_suggestions(match_id);
CREATE INDEX idx_bet_suggestions_status ON bet_suggestions(status);
CREATE INDEX idx_bet_suggestions_created_at ON bet_suggestions(created_at);