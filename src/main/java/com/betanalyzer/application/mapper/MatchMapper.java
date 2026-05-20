package com.betanalyzer.application.mapper;

import com.betanalyzer.application.dto.MatchResponseDTO;
import com.betanalyzer.domain.enums.MatchStatus;
import com.betanalyzer.domain.model.League;
import com.betanalyzer.domain.model.Match;
import com.betanalyzer.domain.model.Team;
import com.betanalyzer.application.service.DataQualityValidator;
import com.betanalyzer.domain.enums.SupportedLeague;
import com.betanalyzer.infrastructure.client.dto.FixtureDTO;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Optional;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MatchMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "apiId", source = "fixtureDTO.fixture.id")
    @Mapping(target = "league", source = "league")
    @Mapping(target = "homeTeam", source = "homeTeam")
    @Mapping(target = "awayTeam", source = "awayTeam")
    @Mapping(target = "homeGoals", source = "fixtureDTO.goals.home")
    @Mapping(target = "awayGoals", source = "fixtureDTO.goals.away")
    @Mapping(target = "matchDate", source = "fixtureDTO.fixture.date", qualifiedByName = "mapDate")
    @Mapping(target = "status", source = "fixtureDTO.fixture.status.shortStatus", qualifiedByName = "mapStatus")
    @Mapping(target = "venue", source = "fixtureDTO.fixture.id", ignore = true) // Not in FixtureDTO
    @Mapping(target = "venueCity", source = "fixtureDTO.fixture.id", ignore = true) // Not in FixtureDTO
    @Mapping(target = "referee", source = "fixtureDTO.fixture.id", ignore = true) // Not in FixtureDTO
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Match mapApiDtoToEntity(FixtureDTO fixtureDTO, League league, Team homeTeam, Team awayTeam);

    default Optional<Match> mapWithQualityCheck(FixtureDTO fixtureDTO, League league, Team homeTeam, Team awayTeam, SupportedLeague supportedLeague, DataQualityValidator validator) {
        if (validator.isQualityFixture(fixtureDTO, supportedLeague)) {
            return Optional.of(mapApiDtoToEntity(fixtureDTO, league, homeTeam, awayTeam));
        }
        return Optional.empty();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "apiId", source = "fixtureDTO.fixture.id")
    @Mapping(target = "league", source = "league")
    @Mapping(target = "homeTeam", source = "homeTeam")
    @Mapping(target = "awayTeam", source = "awayTeam")
    @Mapping(target = "homeGoals", source = "fixtureDTO.goals.home")
    @Mapping(target = "awayGoals", source = "fixtureDTO.goals.away")
    @Mapping(target = "matchDate", source = "fixtureDTO.fixture.date", qualifiedByName = "mapDate")
    @Mapping(target = "status", source = "fixtureDTO.fixture.status.shortStatus", qualifiedByName = "mapStatus")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(FixtureDTO fixtureDTO, League league, Team homeTeam, Team awayTeam, @MappingTarget Match match);

    @Mapping(target = "leagueName", source = "league.name")
    @Mapping(target = "homeTeamName", source = "homeTeam.name")
    @Mapping(target = "awayTeamName", source = "awayTeam.name")
    MatchResponseDTO mapToResponseDTO(Match match);

    // Legacy methods for MatchService compatibility
    default MatchResponseDTO toResponseDTO(Match match) {
        return mapToResponseDTO(match);
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "apiId", ignore = true)
    @Mapping(target = "league", ignore = true) // Needs proper mapping in service if used
    @Mapping(target = "homeTeam", ignore = true) // Needs proper mapping in service if used
    @Mapping(target = "awayTeam", ignore = true) // Needs proper mapping in service if used
    @Mapping(target = "homeGoals", ignore = true)
    @Mapping(target = "awayGoals", ignore = true)
    @Mapping(target = "matchDate", source = "matchDate")
    @Mapping(target = "status", constant = "NS")
    @Mapping(target = "referee", ignore = true)
    @Mapping(target = "venue", ignore = true)
    @Mapping(target = "venueCity", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Match toEntity(com.betanalyzer.application.dto.request.CreateMatchRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "apiId", ignore = true)
    @Mapping(target = "league", ignore = true)
    @Mapping(target = "homeTeam", ignore = true)
    @Mapping(target = "awayTeam", ignore = true)
    @Mapping(target = "homeGoals", ignore = true)
    @Mapping(target = "awayGoals", ignore = true)
    @Mapping(target = "matchDate", ignore = true)
    @Mapping(target = "status", source = "status")
    @Mapping(target = "referee", ignore = true)
    @Mapping(target = "venue", ignore = true)
    @Mapping(target = "venueCity", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(com.betanalyzer.application.dto.request.UpdateMatchRequest request, @MappingTarget Match match);

    @Named("mapDate")
    default LocalDateTime mapDate(String date) {
        if (date == null) return null;
        return ZonedDateTime.parse(date).toLocalDateTime();
    }

    @Named("mapStatus")
    default MatchStatus mapStatus(String status) {
        return MatchStatus.fromCode(status);
    }
}