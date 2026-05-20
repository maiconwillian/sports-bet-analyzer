CREATE TABLE IF NOT EXISTS match_analysis_snapshots (
    id UUID PRIMARY KEY,
    match_id UUID NOT NULL,
    strategy_version VARCHAR(100) NOT NULL,
    features JSONB NOT NULL,
    confidence DOUBLE PRECISION NOT NULL,
    reasoning TEXT,
    generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_analysis_snapshot_match FOREIGN KEY (match_id) REFERENCES matches(id) ON DELETE CASCADE
);

CREATE INDEX idx_analysis_snapshot_match_id ON match_analysis_snapshots(match_id);
CREATE INDEX idx_analysis_snapshot_strategy ON match_analysis_snapshots(strategy_version);
CREATE INDEX idx_analysis_snapshot_generated_at ON match_analysis_snapshots(generated_at);
