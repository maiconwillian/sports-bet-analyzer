CREATE TABLE IF NOT EXISTS match_stats (
                                           id UUID PRIMARY KEY,
                                           match_id UUID NOT NULL UNIQUE,
                                           home_team_form VARCHAR(255),
    away_team_form VARCHAR(255),
    home_team_goals_avg DOUBLE PRECISION,
    away_team_goals_avg DOUBLE PRECISION,
    head_to_head TEXT,
    last_update TIMESTAMP,
    CONSTRAINT fk_match_stats_match FOREIGN KEY (match_id) REFERENCES matches(id) ON DELETE CASCADE
    );

CREATE INDEX idx_match_stats_match_id ON match_stats(match_id);