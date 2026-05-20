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
import com.betanalyzer.shared.exception.ApiIntegrationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FixtureSyncService {

    private final ApiFootballClient apiFootballClient;
    private final LeagueRepository leagueRepository;
    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final LeagueMapper leagueMapper;
    private final TeamMapper teamMapper;
    private final MatchMapper matchMapper;
    private final MatchSyncValidator validator;

    @Transactional
    public SyncResult syncFixturesByDate(LocalDate date) {
        log.info("Starting sync for date: {}", date);
        List<FixtureDTO> fixtures;
        try {
            fixtures = apiFootballClient.getFixturesByDate(date);
        } catch (Exception e) {
            log.error("Failed to fetch fixtures for date {}: {}", date, e.getMessage());
            return SyncResult.builder()
                    .failed(1)
                    .errors(List.of("API Error: " + e.getMessage()))
                    .syncedAt(LocalDateTime.now())
                    .build();
        }

        int created = 0;
        int updated = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        for (FixtureDTO dto : fixtures) {
            try {
                validator.validateFixtureDTO(dto);
                
                League league = findOrCreateLeague(dto.league());
                Team homeTeam = findOrCreateTeam(dto.teams().home());
                Team awayTeam = findOrCreateTeam(dto.teams().away());
                
                Match match = findOrCreateMatch(dto, league, homeTeam, awayTeam);
                
                if (match.getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(5))) {
                    created++;
                } else {
                    updated++;
                }
            } catch (Exception e) {
                log.error("Failed to sync fixture {}: {}", dto.fixture().id(), e.getMessage());
                failed++;
                errors.add("Fixture " + dto.fixture().id() + ": " + e.getMessage());
            }
        }

        return SyncResult.builder()
                .created(created)
                .updated(updated)
                .failed(failed)
                .errors(errors)
                .syncedAt(LocalDateTime.now())
                .build();
    }

    @Transactional
    public SyncResult syncFixturesByDateRange(LocalDate start, LocalDate end) {
        log.info("Starting sync for range: {} to {}", start, end);
        int totalCreated = 0;
        int totalUpdated = 0;
        int totalFailed = 0;
        List<String> totalErrors = new ArrayList<>();

        LocalDate current = start;
        while (!current.isAfter(end)) {
            SyncResult result = syncFixturesByDate(current);
            totalCreated += result.getCreated();
            totalUpdated += result.getUpdated();
            totalFailed += result.getFailed();
            totalErrors.addAll(result.getErrors());
            current = current.plusDays(1);
        }

        return SyncResult.builder()
                .created(totalCreated)
                .updated(totalUpdated)
                .failed(totalFailed)
                .errors(totalErrors)
                .syncedAt(LocalDateTime.now())
                .build();
    }

    private League findOrCreateLeague(FixtureDTO.LeagueInfo leagueInfo) {
        return leagueRepository.findByApiId(leagueInfo.id())
                .map(existing -> {
                    leagueMapper.updateEntity(leagueInfo, existing);
                    return leagueRepository.save(existing);
                })
                .orElseGet(() -> leagueRepository.save(leagueMapper.mapApiDtoToEntity(leagueInfo)));
    }

    private Team findOrCreateTeam(FixtureDTO.TeamInfo teamInfo) {
        return teamRepository.findByApiId(teamInfo.id())
                .map(existing -> {
                    teamMapper.updateEntity(teamInfo, existing);
                    return teamRepository.save(existing);
                })
                .orElseGet(() -> teamRepository.save(teamMapper.mapApiDtoToEntity(teamInfo)));
    }

    private Match findOrCreateMatch(FixtureDTO dto, League league, Team homeTeam, Team awayTeam) {
        return matchRepository.findByApiId(dto.fixture().id())
                .map(existing -> {
                    matchMapper.updateEntity(dto, league, homeTeam, awayTeam, existing);
                    return matchRepository.save(existing);
                })
                .orElseGet(() -> matchRepository.save(matchMapper.mapApiDtoToEntity(dto, league, homeTeam, awayTeam)));
    }
}