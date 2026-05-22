package com.betanalyzer.application.backtesting;

import com.betanalyzer.application.dto.backtesting.BacktestBetResultDTO;
import com.betanalyzer.application.dto.backtesting.BacktestRequest;
import com.betanalyzer.application.dto.backtesting.BacktestResultDTO;
import com.betanalyzer.domain.model.League;
import com.betanalyzer.domain.model.Match;
import com.betanalyzer.infrastructure.persistence.LeagueRepository;
import com.betanalyzer.infrastructure.persistence.MatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BacktestingService {

    private final MatchRepository matchRepository;
    private final LeagueRepository leagueRepository;
    private final HistoricalReplayService historicalReplayService;
    private final MetricsCalculationService metricsCalculationService;

    public BacktestResultDTO runBacktest(BacktestRequest request) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("startDate must be before or equal to endDate");
        }

        if (request.getSimulationMode() != SimulationMode.FIXED_STAKE) {
            throw new IllegalArgumentException("Only FIXED_STAKE simulation mode is supported in Phase 1.7 V1");
        }

        log.info("Starting backtest for strategy {} on league {} from {} to {}", 
                request.getStrategyVersion(), request.getLeague(), request.getStartDate(), request.getEndDate());

        // 1. Localizar Liga
        Optional<League> leagueOpt = leagueRepository.findAll().stream()
                .filter(l -> l.getName().equalsIgnoreCase(request.getLeague().getApiName()))
                .findFirst();

        if (leagueOpt.isEmpty()) {
            throw new IllegalArgumentException("League not found: " + request.getLeague().getApiName());
        }

        // 2. Carregar partidas históricas no período
        LocalDateTime start = request.getStartDate().atStartOfDay();
        LocalDateTime end = request.getEndDate().atTime(23, 59, 59);
        
        List<Match> matches = matchRepository.findByMatchDateBetween(start, end).stream()
                .filter(m -> m.getLeague().getId().equals(leagueOpt.get().getId()))
                .toList();

        log.info("Found {} matches for backtesting", matches.size());

        // 3. Replay de cada partida
        List<BacktestBetResultDTO> betResults = new ArrayList<>();
        int matchesAnalyzed = 0;

        for (Match match : matches) {
            matchesAnalyzed++;
            historicalReplayService.replayMatch(
                    match, 
                    request.getStrategyVersion(), 
                    request.getStake(), 
                    request.getMinimumConfidence()
            ).ifPresent(betResults::add);
        }

        // 4. Calcular Métricas Finais
        return metricsCalculationService.calculate(
                request.getStrategyVersion(), 
                matchesAnalyzed, 
                betResults
        );
    }
}
