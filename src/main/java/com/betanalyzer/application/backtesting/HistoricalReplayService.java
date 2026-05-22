package com.betanalyzer.application.backtesting;

import com.betanalyzer.application.dto.MatchFeatureContextDTO;
import com.betanalyzer.application.dto.backtesting.BacktestBetResultDTO;
import com.betanalyzer.application.feature.service.FeatureCalculationService;
import com.betanalyzer.application.strategy.StrategyEvaluationService;
import com.betanalyzer.domain.enums.MatchStatus;
import com.betanalyzer.domain.model.Match;
import com.betanalyzer.domain.model.Odds;
import com.betanalyzer.domain.strategy.StrategyResult;
import com.betanalyzer.infrastructure.persistence.OddsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class HistoricalReplayService {

    private final FeatureCalculationService featureCalculationService;
    private final StrategyEvaluationService strategyEvaluationService;
    private final OddsRepository oddsRepository;

    public Optional<BacktestBetResultDTO> replayMatch(Match match, String strategyVersion, BigDecimal defaultStake, Double minimumConfidence) {
        if (match.getStatus() != MatchStatus.FT) {
            log.debug("Skipping match {} as it is not finished", match.getId());
            return Optional.empty();
        }

        if (match.getHomeGoals() == null || match.getAwayGoals() == null) {
            log.warn("Skipping match {} as it has no score", match.getId());
            return Optional.empty();
        }

        // 1. Reconstruir contexto histórico (Data Leakage Prevention)
        // [IMPORTANT] FeatureCalculationService uses MatchStats which must represent the state BEFORE the match.
        // Current implementation assumes MatchStats are pre-calculated and not updated with post-match data.
        MatchFeatureContextDTO features = featureCalculationService.calculateOver25Features(match);

        // 2. Executar Estratégia
        StrategyResult strategyResult;
        try {
            strategyResult = strategyEvaluationService.evaluateStrategy(strategyVersion, match, features);
        } catch (Exception e) {
            log.error("Strategy {} not found or failed for match {}", strategyVersion, match.getId());
            return Optional.empty();
        }

        if (!strategyResult.shouldBet() || strategyResult.confidence() < (minimumConfidence != null ? minimumConfidence : 0)) {
            return Optional.empty();
        }

        // 3. Capturar Odd (Melhor odd disponível antes do jogo)
        List<Odds> matchOdds = oddsRepository.findByMatchIdOrderByCapturedAtAsc(match.getId()).stream()
                .filter(odds -> odds.getCapturedAt() != null)
                .filter(odds -> odds.getCapturedAt().isBefore(match.getMatchDate()))
                .toList();

        if (matchOdds.isEmpty()) {
            log.warn("No pre-match odds found for match {}", match.getId());
            return Optional.empty();
        }

        String market = strategyResult.market();
        Optional<Odds> pickedOddOpt = matchOdds.stream()
                .filter(o -> o.getMarket() != null)
                .filter(o -> isSameMarket(o.getMarket(), market))
                .findFirst();

        if (pickedOddOpt.isEmpty()) {
            log.warn("No pre-match odds found for market {} in match {}", market, match.getId());
            return Optional.empty();
        }

        List<Odds> marketOdds = matchOdds.stream()
                .filter(o -> o.getMarket() != null)
                .filter(o -> isSameMarket(o.getMarket(), market))
                .toList();

        Odds pickedOdd = pickedOddOpt.get();
        Odds closingOdd = marketOdds.stream()
                .reduce((first, second) -> second)
                .orElse(pickedOdd);

        // 4. Calcular Resultado
        BacktestBetStatus status = calculateResult(match, market);
        BigDecimal profitLoss = calculateProfitLoss(status, pickedOdd.getOddsValue(), defaultStake);

        BigDecimal clv = null;
        if (marketOdds.size() > 1 && closingOdd.getOddsValue().compareTo(BigDecimal.ZERO) > 0) {
            clv = pickedOdd.getOddsValue().subtract(closingOdd.getOddsValue())
                    .divide(closingOdd.getOddsValue(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        return Optional.of(BacktestBetResultDTO.builder()
                .matchId(match.getId())
                .strategyVersion(strategyVersion)
                .market(market)
                .oddsCaptured(pickedOdd.getOddsValue())
                .closingOdd(marketOdds.size() > 1 ? closingOdd.getOddsValue() : null)
                .clv(clv)
                .stake(defaultStake)
                .confidenceScore(strategyResult.confidence())
                .expectedValue(strategyResult.expectedValue())
                .result(status)
                .profitLoss(profitLoss)
                .matchDate(match.getMatchDate())
                .score(match.getHomeGoals() + " - " + match.getAwayGoals())
                .reasoning(strategyResult.reasoning())
                .features(Map.of(
                        "homeAvgGoalsScored", features.getHomeAvgGoalsScored(),
                        "awayAvgGoalsConceded", features.getAwayAvgGoalsConceded(),
                        "expectedGoalPressure", features.getExpectedGoalPressure()
                ))
                .build());
    }

    private boolean isSameMarket(String oddsMarket, String strategyMarket) {
        if (oddsMarket.equalsIgnoreCase(strategyMarket)) {
            return true;
        }

        String normalizedOddsMarket = oddsMarket.toUpperCase();
        String normalizedStrategyMarket = strategyMarket.toUpperCase();

        return normalizedOddsMarket.contains(normalizedStrategyMarket)
                || normalizedStrategyMarket.contains(normalizedOddsMarket);
    }

    private BacktestBetStatus calculateResult(Match match, String market) {
        int totalGoals = match.getHomeGoals() + match.getAwayGoals();

        if (market.toUpperCase().contains("OVER_2_5")) {
            return totalGoals >= 3 ? BacktestBetStatus.WIN : BacktestBetStatus.LOSS;
        }
        // Adicionar outros mercados conforme necessário
        return BacktestBetStatus.VOID;
    }

    private BigDecimal calculateProfitLoss(BacktestBetStatus status, BigDecimal odd, BigDecimal stake) {
        if (status == BacktestBetStatus.WIN) {
            return stake.multiply(odd.subtract(BigDecimal.ONE));
        } else if (status == BacktestBetStatus.LOSS) {
            return stake.negate();
        }
        return BigDecimal.ZERO;
    }
}
