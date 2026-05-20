CREATE TABLE leagues (
    id UUID PRIMARY KEY,
    api_id BIGINT UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    country VARCHAR(100),
    logo VARCHAR(500),
    season INT,
    created_at TIMESTAMP NOT NULL
);