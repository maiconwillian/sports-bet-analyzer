CREATE TABLE teams (
    id UUID PRIMARY KEY,
    api_id BIGINT UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    logo VARCHAR(500),
    country VARCHAR(100),
    created_at TIMESTAMP NOT NULL
);