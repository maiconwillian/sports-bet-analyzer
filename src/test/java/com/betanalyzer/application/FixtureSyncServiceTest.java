package com.betanalyzer.application;

import com.betanalyzer.application.dto.SettlePendingResultDTO;
import com.betanalyzer.application.dto.SyncResult;
import com.betanalyzer.application.mapper.LeagueMapper;
import com.betanalyzer.application.mapper.MatchMapper;
import com.betanalyzer.application.mapper.TeamMapper;
import com.betanalyzer.application.service.DataQualityValidator;
import com.betanalyzer.domain.model.League;
import com.betanalyzer.domain.model.Match;
import com.betanalyzer.domain.model.Team;
import com.betanalyzer.infrastructure.client.ApiFootballClient;
import com.betanalyzer.infrastructure.client.dto.FixtureDTO;
import com.betanalyzer.infrastructure.persistence.LeagueRepository;
import com.betanalyzer.infrastructure.persistence.MatchRepository;
import com.betanalyzer.infrastructure.persistence.MatchStatsRepository;
import com.betanalyzer.infrastructure.persistence.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FixtureSyncServiceTest {

    @Mock
    private ApiFootballClient apiFootballClient;
    @Mock
    private LeagueRepository leagueRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private MatchRepository matchRepository;
    @Mock
    private MatchStatsRepository matchStatsRepository;
    @Mock
    private LeagueMapper leagueMapper;
    @Mock
    private TeamMapper teamMapper;
    @Mock
    private MatchMapper matchMapper;
    @Mock
    private MatchSyncValidator validator;
    @Mock
    private SuggestionSettlementService suggestionSettlementService;
    @Mock
    private DataQualityValidator dataQualityValidator;

    @InjectMocks
    private FixtureSyncService service;

    private FixtureDTO premierEngland;
    private FixtureDTO premierEgypt;

    @BeforeEach
    void setUp() {
        premierEngland = fixture("Premier League", "England", 100L);
        premierEgypt = fixture("Premier League", "Egypt", 200L);

        when(suggestionSettlementService.settlePendingSuggestions()).thenReturn(
                SettlePendingResultDTO.builder().settled(0).won(0).lost(0).voided(0).skipped(0).errors(List.of()).build()
        );
    }

    @Test
    void syncFixturesByDate_shouldImportPremierLeagueEngland() {
        LocalDate date = LocalDate.now();
        when(apiFootballClient.getFixturesByDate(date)).thenReturn(List.of(premierEngland));
        when(dataQualityValidator.isQualityFixture(eq(premierEngland), any())).thenReturn(true);
        stubPersistNewMatch();

        SyncResult result = service.syncFixturesByDate(date);

        assertEquals(1, result.getCreated());
        assertEquals(0, result.getSkippedUnsupported());
        verify(matchRepository).save(any(Match.class));
    }

    @Test
    void syncFixturesByDate_shouldSkipPremierLeagueEgypt() {
        LocalDate date = LocalDate.now();
        when(apiFootballClient.getFixturesByDate(date)).thenReturn(List.of(premierEgypt));

        SyncResult result = service.syncFixturesByDate(date);

        assertEquals(0, result.getCreated());
        assertEquals(1, result.getSkippedUnsupported());
        verify(matchRepository, never()).save(any());
    }

    @Test
    void syncFixturesByDate_shouldSkipUnsupportedLeague() {
        LocalDate date = LocalDate.now();
        FixtureDTO randomLeague = fixture("Random League", "Nowhere", 300L);
        when(apiFootballClient.getFixturesByDate(date)).thenReturn(List.of(randomLeague));

        SyncResult result = service.syncFixturesByDate(date);

        assertEquals(1, result.getSkippedUnsupported());
        verify(matchRepository, never()).save(any());
    }

    @Test
    void syncFixturesByDate_shouldSkipLowQualityFixture() {
        LocalDate date = LocalDate.now();
        when(apiFootballClient.getFixturesByDate(date)).thenReturn(List.of(premierEngland));
        when(dataQualityValidator.isQualityFixture(eq(premierEngland), any())).thenReturn(false);

        SyncResult result = service.syncFixturesByDate(date);

        assertEquals(0, result.getCreated());
        assertEquals(1, result.getSkippedQuality());
        verify(matchRepository, never()).save(any());
    }

    private void stubPersistNewMatch() {
        when(leagueRepository.findByApiId(any())).thenReturn(Optional.empty());
        when(teamRepository.findByApiId(any())).thenReturn(Optional.empty());
        when(matchRepository.findByApiId(any())).thenReturn(Optional.empty());
        when(matchStatsRepository.findByMatchId(any())).thenReturn(Optional.empty());

        League league = new League();
        Team home = new Team();
        Team away = new Team();
        Match match = new Match();
        match.setCreatedAt(LocalDateTime.now());

        when(leagueMapper.mapApiDtoToEntity(any())).thenReturn(league);
        when(teamMapper.mapApiDtoToEntity(any())).thenReturn(home, away);
        when(matchMapper.mapApiDtoToEntity(any(), any(), any(), any())).thenReturn(match);
        when(leagueRepository.save(any())).thenReturn(league);
        when(teamRepository.save(any())).thenReturn(home, away);
        when(matchRepository.save(any())).thenReturn(match);
    }

    private static FixtureDTO fixture(String leagueName, String country, long fixtureId) {
        return new FixtureDTO(
                new FixtureDTO.FixtureInfo(fixtureId, "2024-05-20T20:00:00Z",
                        new FixtureDTO.StatusInfo("Not Started", "NS", "0")),
                new FixtureDTO.TeamsInfo(
                        new FixtureDTO.TeamInfo(1L, "Home", "logo1"),
                        new FixtureDTO.TeamInfo(2L, "Away", "logo2")
                ),
                new FixtureDTO.LeagueInfo(1L, leagueName, country, 2024),
                new FixtureDTO.GoalsInfo(0, 0)
        );
    }
}
