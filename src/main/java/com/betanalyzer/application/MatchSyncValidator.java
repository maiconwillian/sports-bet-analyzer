package com.betanalyzer.application;

import com.betanalyzer.infrastructure.client.dto.FixtureDTO;
import com.betanalyzer.shared.exception.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class MatchSyncValidator {

    public void validateFixtureDTO(FixtureDTO dto) {
        if (dto == null) {
            throw new ValidationException("FixtureDTO cannot be null");
        }
        if (dto.league() == null || dto.league().id() == null) {
            throw new ValidationException("League details or ID cannot be null");
        }
        if (dto.teams() == null || dto.teams().home() == null || dto.teams().away() == null) {
            throw new ValidationException("Teams details cannot be null");
        }
        if (dto.fixture() == null || dto.fixture().id() == null || dto.fixture().date() == null) {
            throw new ValidationException("Fixture basic info or date cannot be null");
        }
        if (dto.fixture().status() == null || dto.fixture().status().shortStatus() == null) {
            throw new ValidationException("Fixture status cannot be null");
        }
    }
}