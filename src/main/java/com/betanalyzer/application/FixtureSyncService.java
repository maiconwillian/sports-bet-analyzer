package com.betanalyzer.application;

import com.betanalyzer.application.dto.SettlePendingResultDTO;
import com.betanalyzer.application.dto.SyncResult;
import com.betanalyzer.application.mapper.LeagueMapper;
import com.betanalyzer.application.mapper.MatchMapper;
import com.betanalyzer.application.mapper.TeamMapper;
import com.betanalyzer.application.service.DataQualityValidator;
import com.betanalyzer.domain.enums.SupportedLeague;
import com.betanalyzer.domain.model.League;
import com.betanalyzer.domain.model.Match;
import com.betanalyzer.domain.model.Team;
import com.betanalyzer.infrastructure.client.ApiFootballClient;
import com.betanalyzer.infrastructure.client.dto.FixtureDTO;
import com.betanalyzer.infrastructure.persistence.LeagueRepository;
import com.betanalyzer.infrastructure.persistence.MatchRepository;
import com.betanalyzer.infrastructure.persistence.MatchStatsRepository;
import com.betanalyzer.infrastructure.persistence.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FixtureSyncService {

    private final ApiFootballClient apiFootballClient;
    private final LeagueRepository leagueRepository;
    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final MatchStatsRepository matchStatsRepository;
    private final LeagueMapper leagueMapper;
    private final TeamMapper teamMapper;
    private final MatchMapper matchMapper;
    private final MatchSyncValidator validator;
    private final SuggestionSettlementService suggestionSettlementService;
    private final DataQualityValidator dataQualityValidator;

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

        SyncCounters counters = processFixtures(fixtures);
        log.info("Sync completed - Created: {}, Updated: {}, Failed: {}, Skipped unsupported: {}, Skipped quality: {}",
                counters.created, counters.updated, counters.failed,
                counters.skippedUnsupported, counters.skippedQuality);

        return buildSyncResult(counters);
    }

    @Transactional
    public SyncResult syncFixturesByDateRange(LocalDate start, LocalDate end) {
        log.info("Starting sync for range: {} to {}", start, end);
        SyncCounters total = new SyncCounters();

        LocalDate current = start;
        while (!current.isAfter(end)) {
            SyncResult result = syncFixturesByDate(current);
            total.created += result.getCreated();
            total.updated += result.getUpdated();
            total.failed += result.getFailed();
            total.skippedUnsupported += result.getSkippedUnsupported();
            total.skippedQuality += result.getSkippedQuality();
            if (result.getErrors() != null) {
                total.errors.addAll(result.getErrors());
            }
            current = current.plusDays(1);
        }

        return buildSyncResult(total);
    }

    private SyncCounters processFixtures(List<FixtureDTO> fixtures) {
        SyncCounters counters = new SyncCounters();

        for (FixtureDTO dto : fixtures) {
            try {
                validator.validateFixtureDTO(dto);

                Optional<SupportedLeague> supported = SupportedLeague.findByLeague(
                        dto.league().name(),
                        dto.league().country()
                );
                if (supported.isEmpty()) {
                    counters.skippedUnsupported++;
                    log.debug("Skipping unsupported league: {} ({})",
                            dto.league().name(), dto.league().country());
                    continue;
                }

                if (!dataQualityValidator.isQualityFixture(dto, supported.get())) {
                    counters.skippedQuality++;
                    log.debug("Skipping low-quality fixture: {} ({})",
                            dto.league().name(), dto.league().country());
                    continue;
                }

                League league = findOrCreateLeague(dto.league());
                Team homeTeam = findOrCreateTeam(dto.teams().home());
                Team awayTeam = findOrCreateTeam(dto.teams().away());

                boolean isNew = matchRepository.findByApiId(dto.fixture().id()).isEmpty();

                Match match = findOrCreateMatch(dto, league, homeTeam, awayTeam);
                createOrUpdateMatchStats(match);

                if (isNew) {
                    counters.created++;
                    log.info("Created new match: {} - {} vs {}",
                            dto.fixture().id(),
                            dto.teams().home().name(),
                            dto.teams().away().name());
                } else {
                    counters.updated++;
                    log.info("Updated existing match: {}", dto.fixture().id());
                }
            } catch (Exception e) {
                log.error("Failed to sync fixture {}: {}", dto.fixture().id(), e.getMessage(), e);
                counters.failed++;
                counters.errors.add("Fixture " + dto.fixture().id() + ": " + e.getMessage());
            }
        }

        return counters;
    }

    private SyncResult buildSyncResult(SyncCounters counters) {
        SettlePendingResultDTO settlement = suggestionSettlementService.settlePendingSuggestions();
        String message = String.format(
                "Sync OK — %d criadas, %d atualizadas, %d ignoradas (liga não suportada), %d ignoradas (qualidade). "
                        + "Liquidação: %d sugestões (%d ganhas, %d perdidas, %d void).",
                counters.created,
                counters.updated,
                counters.skippedUnsupported,
                counters.skippedQuality,
                settlement.getSettled(),
                settlement.getWon(),
                settlement.getLost(),
                settlement.getVoided()
        );
        return SyncResult.builder()
                .created(counters.created)
                .updated(counters.updated)
                .failed(counters.failed)
                .skippedUnsupported(counters.skippedUnsupported)
                .skippedQuality(counters.skippedQuality)
                .errors(counters.errors)
                .syncedAt(LocalDateTime.now())
                .message(message)
                .settled(settlement.getSettled())
                .won(settlement.getWon())
                .lost(settlement.getLost())
                .voided(settlement.getVoided())
                .skippedSettlement(settlement.getSkipped())
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

    private void createOrUpdateMatchStats(Match match) {
        var existingStats = matchStatsRepository.findByMatchId(match.getId());

        if (existingStats.isEmpty()) {
            var stats = com.betanalyzer.domain.model.MatchStats.builder()
                    .match(match)
                    .homeTeamGoalsAvg(0.0)
                    .awayTeamGoalsAvg(0.0)
                    .homeTeamForm("TBD")
                    .awayTeamForm("TBD")
                    .statsEnriched(false)
                    .lastUpdate(LocalDateTime.now())
                    .build();

            matchStatsRepository.save(stats);
            log.info("Created MatchStats for match: {}", match.getId());
        }
    }

    private static final class SyncCounters {
        int created;
        int updated;
        int failed;
        int skippedUnsupported;
        int skippedQuality;
        final List<String> errors = new ArrayList<>();
    }
}
