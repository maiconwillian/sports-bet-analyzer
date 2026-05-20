package com.betanalyzer.application;

import com.betanalyzer.application.dto.SyncResult;
import com.betanalyzer.domain.model.Match;
import com.betanalyzer.infrastructure.client.ApiFootballClient;
import com.betanalyzer.infrastructure.client.dto.FixtureDTO;
import com.betanalyzer.infrastructure.persistence.LeagueRepository;
import com.betanalyzer.infrastructure.persistence.MatchRepository;
import com.betanalyzer.infrastructure.persistence.TeamRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
class FixtureSyncServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    private FixtureSyncService fixtureSyncService;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private TeamRepository teamRepository;

    @MockBean
    private ApiFootballClient apiFootballClient;

    @Test
    void testSyncRealFixtures_DatabasePersistence() {
        LocalDate date = LocalDate.of(2024, 5, 20);
        FixtureDTO fixtureDTO = createFixtureDTO();
        when(apiFootballClient.getFixturesByDate(date)).thenReturn(List.of(fixtureDTO));

        SyncResult result = fixtureSyncService.syncFixturesByDate(date);

        assertEquals(1, result.getCreated());
        
        Optional<Match> savedMatch = matchRepository.findByApiId(12345L);
        assertTrue(savedMatch.isPresent());
        assertEquals("Premier League", savedMatch.get().getLeague().getName());
        assertEquals("Arsenal", savedMatch.get().getHomeTeam().getName());
        assertEquals("Chelsea", savedMatch.get().getAwayTeam().getName());

        // Test Upsert
        SyncResult updateResult = fixtureSyncService.syncFixturesByDate(date);
        assertEquals(0, updateResult.getCreated());
        assertEquals(1, updateResult.getUpdated());
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