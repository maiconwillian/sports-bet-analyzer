-- V6__Create_Odds_Table.sql
CREATE TABLE IF NOT EXISTS odds (
    id UUID PRIMARY KEY,
    match_id UUID NOT NULL,
    bookmaker VARCHAR(100) NOT NULL,
    home_win_odd NUMERIC(10, 2) NOT NULL,
    draw_odd NUMERIC(10, 2) NOT NULL,
    away_win_odd NUMERIC(10, 2) NOT NULL,
    captured_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_odds_match FOREIGN KEY (match_id) REFERENCES matches(id) ON DELETE CASCADE
);

CREATE INDEX idx_odds_match_id ON odds(match_id);
CREATE INDEX idx_odds_bookmaker ON odds(bookmaker);
CREATE INDEX idx_odds_captured_at ON odds(captured_at);
