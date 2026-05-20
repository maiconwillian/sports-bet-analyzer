package com.betanalyzer.application.mapper;

import com.betanalyzer.domain.enums.MatchStatus;
import com.betanalyzer.domain.model.League;
import com.betanalyzer.domain.model.Match;
import com.betanalyzer.domain.model.Team;
import com.betanalyzer.infrastructure.client.dto.FixtureDTO;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class MatchMapperTest {

    private final MatchMapper mapper = Mappers.getMapper(MatchMapper.class);

    @Test
    void testMapApiDtoToEntity() {
        FixtureDTO dto = createFixtureDTO();
        League league = League.builder().name("Premier League").build();
        Team home = Team.builder().name("Arsenal").build();
        Team away = Team.builder().name("Chelsea").build();

        Match match = mapper.mapApiDtoToEntity(dto, league, home, away);

        assertNotNull(match);
        assertEquals(12345L, match.getApiId());
        assertEquals(league, match.getLeague());
        assertEquals(home, match.getHomeTeam());
        assertEquals(away, match.getAwayTeam());
        assertEquals(2, match.getHomeGoals());
        assertEquals(1, match.getAwayGoals());
        assertEquals(MatchStatus.FT, match.getStatus());
        assertNotNull(match.getMatchDate());
    }

    private FixtureDTO createFixtureDTO() {
        return new FixtureDTO(
            new FixtureDTO.FixtureInfo(12345L, "2024-05-20T20:00:00Z", new FixtureDTO.StatusInfo("Finished", "FT", "90")),
            new FixtureDTO.TeamsInfo(
                new FixtureDTO.TeamInfo(1L, "Arsenal", "logo1"),
                new FixtureDTO.TeamInfo(2L, "Chelsea", "logo2")
            ),
            new FixtureDTO.LeagueInfo(39L, "Premier League", "England", 2023),
            new FixtureDTO.GoalsInfo(2, 1)
        );
    }
}