package com.betanalyzer.application;

import com.betanalyzer.application.dto.EnrichResultDTO;
import com.betanalyzer.application.dto.TeamEnrichmentSnapshot;
import com.betanalyzer.config.EnrichProperties;
import com.betanalyzer.domain.enums.SupportedLeague;
import com.betanalyzer.domain.model.League;
import com.betanalyzer.domain.model.Match;
import com.betanalyzer.domain.model.MatchStats;
import com.betanalyzer.infrastructure.client.ApiFootballEnrichmentClient;
import com.betanalyzer.infrastructure.persistence.MatchRepository;
import com.betanalyzer.infrastructure.persistence.MatchStatsRepository;
import com.betanalyzer.shared.exception.MatchNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrichMatchAnalysisService {

    private final MatchRepository matchRepository;
    private final MatchStatsRepository matchStatsRepository;
    private final ApiFootballEnrichmentClient enrichmentClient;
    private final EnrichProperties enrichProperties;

    @Transactional
    public EnrichResultDTO enrichMatch(UUID matchId) {
        Match match = matchRepository.findDetailedById(matchId)
                .orElseThrow(() -> new MatchNotFoundException("Match not found: " + matchId));

        if (SupportedLeague.findByLeague(match.getLeague().getName(), match.getLeague().getCountry()).isEmpty()) {
            return EnrichResultDTO.builder()
                    .matchesProcessed(1)
                    .enriched(0)
                    .failed(1)
                    .errors(List.of("Unsupported league for enrichment"))
                    .message("Liga não suportada")
                    .build();
        }

        boolean ok = enrichSingleMatch(match);
        return EnrichResultDTO.builder()
                .matchesProcessed(1)
                .enriched(ok ? 1 : 0)
                .failed(ok ? 0 : 1)
                .errors(ok ? List.of() : List.of("API returned no team statistics for match " + matchId))
                .message(ok ? "Análise atualizada" : "Falha ao enriquecer partida")
                .build();
    }

    @Transactional
    public EnrichResultDTO enrichMatchesForDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        List<Match> matches = matchRepository.findDetailedByMatchDateBetween(start, end);

        int enriched = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        for (Match match : matches) {
            if (SupportedLeague.findByLeague(match.getLeague().getName(), match.getLeague().getCountry()).isEmpty()) {
                continue;
            }
            try {
                if (enrichSingleMatch(match)) {
                    enriched++;
                } else {
                    failed++;
                    errors.add(match.getHomeTeam().getName() + " vs " + match.getAwayTeam().getName());
                }
            } catch (Exception e) {
                failed++;
                errors.add(match.getId() + ": " + e.getMessage());
                log.warn("Enrich failed for match {}", match.getId(), e);
            }
        }

        return EnrichResultDTO.builder()
                .date(date)
                .matchesProcessed(matches.size())
                .enriched(enriched)
                .failed(failed)
                .errors(errors)
                .message(String.format("Enriquecimento %s — %d ok, %d falhas", date, enriched, failed))
                .build();
    }

    @Transactional
    public EnrichResultDTO enrichMatchesForDateRange(LocalDate from, LocalDate to) {
        if (to.isBefore(from)) {
            return EnrichResultDTO.builder()
                    .matchesProcessed(0)
                    .enriched(0)
                    .failed(0)
                    .errors(List.of("Data final anterior à inicial"))
                    .message("Intervalo inválido")
                    .build();
        }

        int totalProcessed = 0;
        int totalEnriched = 0;
        int totalFailed = 0;
        List<String> errors = new ArrayList<>();

        for (LocalDate current = from; !current.isAfter(to); current = current.plusDays(1)) {
            EnrichResultDTO dayResult = enrichMatchesForDate(current);
            totalProcessed += dayResult.matchesProcessed();
            totalEnriched += dayResult.enriched();
            totalFailed += dayResult.failed();
            if (dayResult.errors() != null) {
                errors.addAll(dayResult.errors());
            }
        }

        return EnrichResultDTO.builder()
                .date(from)
                .matchesProcessed(totalProcessed)
                .enriched(totalEnriched)
                .failed(totalFailed)
                .errors(errors)
                .message(String.format("Enriquecimento %s a %s — %d ok, %d falhas", from, to, totalEnriched, totalFailed))
                .build();
    }

    private boolean enrichSingleMatch(Match match) {
        League league = match.getLeague();
        int season = resolveSeason(league, match.getMatchDate());
        Long leagueApiId = league.getApiId();
        Long homeApiId = match.getHomeTeam().getApiId();
        Long awayApiId = match.getAwayTeam().getApiId();

        Map<Long, Integer> standings = enrichmentClient.fetchStandingsRanks(leagueApiId, season);
        int lastN = enrichProperties.getLastFixturesForForm();

        var homeSnap = enrichmentClient.fetchTeamEnrichment(
                homeApiId, leagueApiId, season, lastN, standings);
        var awaySnap = enrichmentClient.fetchTeamEnrichment(
                awayApiId, leagueApiId, season, lastN, standings);

        if (homeSnap.isEmpty() || awaySnap.isEmpty()) {
            return false;
        }

        persistStats(match, homeSnap.get(), awaySnap.get());
        return true;
    }

    private void persistStats(Match match, TeamEnrichmentSnapshot home, TeamEnrichmentSnapshot away) {
        MatchStats stats = matchStatsRepository.findByMatchId(match.getId())
                .orElse(MatchStats.builder().match(match).build());

        stats.setHomeTeamForm(home.form());
        stats.setAwayTeamForm(away.form());
        stats.setHomeTeamGoalsAvg(home.goalsScoredAvg());
        stats.setAwayTeamGoalsAvg(away.goalsScoredAvg());
        stats.setHomeTeamGoalsConcededAvg(home.goalsConcededAvg());
        stats.setAwayTeamGoalsConcededAvg(away.goalsConcededAvg());
        stats.setHomeOver25Rate(home.over25Rate());
        stats.setAwayOver25Rate(away.over25Rate());
        stats.setHomeLeaguePosition(home.leaguePosition());
        stats.setAwayLeaguePosition(away.leaguePosition());
        stats.setStatsEnriched(true);
        stats.setLastUpdate(LocalDateTime.now());

        matchStatsRepository.save(stats);
        log.info("Enriched stats for {} vs {}", match.getHomeTeam().getName(), match.getAwayTeam().getName());
    }

    private int resolveSeason(League league, LocalDateTime matchDate) {
        if (league.getSeason() != null && league.getSeason() > 0) {
            return league.getSeason();
        }
        return matchDate.getMonth().getValue() >= 7
                ? matchDate.getYear()
                : matchDate.getYear() - 1;
    }
}
