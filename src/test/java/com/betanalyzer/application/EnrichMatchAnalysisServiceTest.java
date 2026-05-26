package com.betanalyzer.application;

import com.betanalyzer.application.dto.TeamEnrichmentSnapshot;
import com.betanalyzer.config.EnrichProperties;
import com.betanalyzer.domain.model.League;
import com.betanalyzer.domain.model.Match;
import com.betanalyzer.domain.model.MatchStats;
import com.betanalyzer.domain.model.Team;
import com.betanalyzer.infrastructure.client.ApiFootballEnrichmentClient;
import com.betanalyzer.infrastructure.persistence.MatchRepository;
import com.betanalyzer.infrastructure.persistence.MatchStatsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrichMatchAnalysisServiceTest {

    @Mock
    private MatchRepository matchRepository;
    @Mock
    private MatchStatsRepository matchStatsRepository;
    @Mock
    private ApiFootballEnrichmentClient enrichmentClient;
    @Mock
    private EnrichProperties enrichProperties;

    @InjectMocks
    private EnrichMatchAnalysisService service;

    private Match match;
    private UUID matchId;

    @BeforeEach
    void setUp() {
        matchId = UUID.randomUUID();
        League league = League.builder().apiId(39L).name("Premier League").country("England").season(2024).build();
        Team home = Team.builder().apiId(85L).name("PSG").build();
        Team away = Team.builder().apiId(42L).name("Arsenal").build();
        match = Match.builder()
                .id(matchId)
                .league(league)
                .homeTeam(home)
                .awayTeam(away)
                .matchDate(LocalDateTime.of(2026, 5, 30, 16, 0))
                .build();

        when(enrichProperties.getLastFixturesForForm()).thenReturn(10);
    }

    @Test
    void enrichMatch_shouldPersistStatsWhenApiReturnsData() {
        when(matchRepository.findDetailedById(matchId)).thenReturn(Optional.of(match));
        when(enrichmentClient.fetchStandingsRanks(39L, 2024)).thenReturn(Map.of(85L, 1, 42L, 2));
        when(enrichmentClient.fetchTeamEnrichment(eq(85L), eq(39L), eq(2024), eq(10), any()))
                .thenReturn(Optional.of(TeamEnrichmentSnapshot.builder()
                        .form("WWDLW")
                        .goalsScoredAvg(2.1)
                        .goalsConcededAvg(0.9)
                        .over25Rate(70.0)
                        .leaguePosition(1)
                        .build()));
        when(enrichmentClient.fetchTeamEnrichment(eq(42L), eq(39L), eq(2024), eq(10), any()))
                .thenReturn(Optional.of(TeamEnrichmentSnapshot.builder()
                        .form("WDWWW")
                        .goalsScoredAvg(1.8)
                        .goalsConcededAvg(1.0)
                        .over25Rate(60.0)
                        .leaguePosition(2)
                        .build()));
        when(matchStatsRepository.findByMatchId(matchId)).thenReturn(Optional.empty());

        var result = service.enrichMatch(matchId);

        assertEquals(1, result.enriched());
        ArgumentCaptor<MatchStats> captor = ArgumentCaptor.forClass(MatchStats.class);
        verify(matchStatsRepository).save(captor.capture());
        assertEquals(2.1, captor.getValue().getHomeTeamGoalsAvg());
        assertTrue(captor.getValue().getStatsEnriched());
    }
}
