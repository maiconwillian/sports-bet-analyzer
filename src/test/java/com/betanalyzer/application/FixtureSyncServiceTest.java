package com.betanalyzer.application;

import com.betanalyzer.application.dto.SyncResult;
import com.betanalyzer.application.mapper.LeagueMapper;
import com.betanalyzer.application.mapper.MatchMapper;
import com.betanalyzer.application.mapper.TeamMapper;
import com.betanalyzer.domain.model.League;
import com.betanalyzer.domain.model.Match;
import com.betanalyzer.domain.model.Team;
import com.betanalyzer.infrastructure.client.ApiFootballClient;
import com.betanalyzer.infrastructure.client.dto.FixtureDTO;
import com.betanalyzer.infrastructure.persistence.LeagueRepository;
import com.betanalyzer.infrastructure.persistence.MatchRepository;
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
    private LeagueMapper leagueMapper;
    @Mock
    private TeamMapper teamMapper;
    @Mock
    private MatchMapper matchMapper;
    @Mock
    private MatchSyncValidator validator;

    @InjectMocks
    private FixtureSyncService service;

    private FixtureDTO fixtureDTO;

    @BeforeEach
    void setUp() {
        fixtureDTO = new FixtureDTO(
            new FixtureDTO.FixtureInfo(1L, "2024-05-20T20:00:00Z", new FixtureDTO.StatusInfo("NS", "NS", "0")),
            new FixtureDTO.TeamsInfo(
                new FixtureDTO.TeamInfo(1L, "Home", "logo1"),
                new FixtureDTO.TeamInfo(2L, "Away", "logo2")
            ),
            new FixtureDTO.LeagueInfo(1L, "League", "Country", 2024),
            new FixtureDTO.GoalsInfo(0, 0)
        );
    }

    @Test
    void testSyncFixturesByDate_Success_NewMatch() {
        LocalDate date = LocalDate.now();
        when(apiFootballClient.getFixturesByDate(date)).thenReturn(List.of(fixtureDTO));
        when(leagueRepository.findByApiId(any())).thenReturn(Optional.empty());
        when(teamRepository.findByApiId(any())).thenReturn(Optional.empty());
        when(matchRepository.findByApiId(any())).thenReturn(Optional.empty());
        
        League league = new League();
        Team home = new Team();
        Team away = new Team();
        Match match = new Match();
        match.setCreatedAt(LocalDateTime.now());

        when(leagueMapper.mapApiDtoToEntity(any())).thenReturn(league);
        when(teamMapper.mapApiDtoToEntity(any())).thenReturn(home).thenReturn(away);
        when(matchMapper.mapApiDtoToEntity(any(), any(), any(), any())).thenReturn(match);
        
        when(leagueRepository.save(any())).thenReturn(league);
        when(teamRepository.save(any())).thenReturn(home).thenReturn(away);
        when(matchRepository.save(any())).thenReturn(match);

        SyncResult result = service.syncFixturesByDate(date);

        assertEquals(1, result.getCreated());
        assertEquals(0, result.getUpdated());
        verify(validator).validateFixtureDTO(fixtureDTO);
    }

    @Test
    void testSyncFixturesByDate_UpdateExistingMatch() {
        LocalDate date = LocalDate.now();
        when(apiFootballClient.getFixturesByDate(date)).thenReturn(List.of(fixtureDTO));
        
        League league = new League();
        Team home = new Team();
        Team away = new Team();
        Match existingMatch = new Match();
        existingMatch.setCreatedAt(LocalDateTime.now().minusHours(1));

        when(leagueRepository.findByApiId(any())).thenReturn(Optional.of(league));
        when(teamRepository.findByApiId(any())).thenReturn(Optional.of(home)).thenReturn(Optional.of(away));
        when(matchRepository.findByApiId(any())).thenReturn(Optional.of(existingMatch));
        
        when(leagueRepository.save(any())).thenReturn(league);
        when(teamRepository.save(any())).thenReturn(home).thenReturn(away);
        when(matchRepository.save(any())).thenReturn(existingMatch);

        SyncResult result = service.syncFixturesByDate(date);

        assertEquals(0, result.getCreated());
        assertEquals(1, result.getUpdated());
    }
}