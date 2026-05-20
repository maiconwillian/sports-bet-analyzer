CREATE TABLE matches (
    id UUID PRIMARY KEY,
    api_id BIGINT UNIQUE NOT NULL,
    league_id UUID NOT NULL REFERENCES leagues(id),
    home_team_id UUID NOT NULL REFERENCES teams(id),
    away_team_id UUID NOT NULL REFERENCES teams(id),
    home_goals INT,
    away_goals INT,
    match_date TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    referee VARCHAR(255),
    venue VARCHAR(255),
    venue_city VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);