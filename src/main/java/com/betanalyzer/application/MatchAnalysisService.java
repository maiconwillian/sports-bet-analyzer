package com.betanalyzer.application;

import com.betanalyzer.application.dto.MatchAnalysisResponseDTO;
import com.betanalyzer.application.dto.MatchStatsResponseDTO;
import com.betanalyzer.application.dto.ModelInsightDTO;
import com.betanalyzer.application.feature.service.FeatureCalculationService;
import com.betanalyzer.application.strategy.StrategyEvaluationService;
import com.betanalyzer.domain.enums.SupportedLeague;
import com.betanalyzer.domain.model.Match;
import com.betanalyzer.domain.model.MatchStats;
import com.betanalyzer.domain.strategy.StrategyResult;
import com.betanalyzer.config.ValueBetProperties;
import com.betanalyzer.infrastructure.persistence.MatchRepository;
import com.betanalyzer.infrastructure.persistence.MatchStatsRepository;
import com.betanalyzer.infrastructure.persistence.OddsRepository;
import com.betanalyzer.shared.exception.MatchNotFoundException;
import com.betanalyzer.shared.util.MarketUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchAnalysisService {

    private static final String STRATEGY_NAME = "OVER_25_QUANT";

    private final MatchRepository matchRepository;
    private final MatchStatsRepository matchStatsRepository;
    private final FeatureCalculationService featureCalculationService;
    private final StrategyEvaluationService strategyEvaluationService;
    private final OddsRepository oddsRepository;
    private final ValueBetProperties valueBetProperties;

    @Transactional(readOnly = true)
    public MatchAnalysisResponseDTO getMatchAnalysis(UUID matchId) {
        Match match = matchRepository.findDetailedById(matchId)
                .orElseThrow(() -> new MatchNotFoundException("Match not found: " + matchId));

        MatchStats stats = matchStatsRepository.findByMatchId(matchId).orElse(null);
        MatchStatsResponseDTO statsDto = stats != null ? toStatsDto(matchId, stats) : emptyStatsDto(matchId);

        ModelInsightDTO insight = buildInsight(match, stats);

        return MatchAnalysisResponseDTO.builder()
                .matchId(matchId)
                .homeTeamName(match.getHomeTeam().getName())
                .awayTeamName(match.getAwayTeam().getName())
                .stats(statsDto)
                .modelInsight(insight)
                .statsEnriched(stats != null && stats.hasUsableEnrichment())
                .build();
    }

    private ModelInsightDTO buildInsight(Match match, MatchStats stats) {
        if (stats == null || !stats.hasUsableEnrichment()) {
            return ModelInsightDTO.builder()
                    .strategyName(STRATEGY_NAME)
                    .market("OVER_2_5")
                    .confidence(0.0)
                    .expectedValue(-1.0)
                    .reasoning("Execute enriquecimento de análise (Admin ou botão na partida) antes de confiar no modelo.")
                    .wouldPassValueBetFilters(false)
                    .build();
        }

        var features = featureCalculationService.calculateOver25Features(match);
        StrategyResult strategyResult = strategyEvaluationService.evaluateStrategy(STRATEGY_NAME, match, features);

        var bestOdd = oddsRepository.findByMatchId(match.getId()).stream()
                .filter(o -> o.getMarket() != null && MarketUtils.isOver25Market(o.getMarket()))
                .max((a, b) -> a.getOddsValue().compareTo(b.getOddsValue()));

        Double expectedValue = null;
        boolean passesFilters = false;
        if (bestOdd.isPresent()) {
            double oddValue = bestOdd.get().getOddsValue().doubleValue();
            expectedValue = (strategyResult.confidence() / 100.0) * oddValue - 1.0;
            passesFilters = strategyResult.confidence() >= valueBetProperties.getMinConfidence()
                    && expectedValue >= valueBetProperties.getMinEv();
        }

        return ModelInsightDTO.builder()
                .strategyName(strategyResult.strategyName())
                .market(strategyResult.market())
                .confidence(strategyResult.confidence())
                .expectedValue(expectedValue)
                .suggestedOdd(bestOdd.map(o -> o.getOddsValue()).orElse(null))
                .bookmaker(bestOdd.map(com.betanalyzer.domain.model.Odds::getBookmaker).orElse(null))
                .reasoning(strategyResult.reasoning())
                .wouldPassValueBetFilters(passesFilters)
                .build();
    }

    private MatchStatsResponseDTO toStatsDto(UUID matchId, MatchStats stats) {
        return MatchStatsResponseDTO.builder()
                .matchId(matchId)
                .homeTeamForm(stats.getHomeTeamForm())
                .awayTeamForm(stats.getAwayTeamForm())
                .homeTeamGoalsAvg(stats.getHomeTeamGoalsAvg())
                .awayTeamGoalsAvg(stats.getAwayTeamGoalsAvg())
                .homeTeamGoalsConcededAvg(stats.getHomeTeamGoalsConcededAvg())
                .awayTeamGoalsConcededAvg(stats.getAwayTeamGoalsConcededAvg())
                .homeOver25Rate(stats.getHomeOver25Rate())
                .awayOver25Rate(stats.getAwayOver25Rate())
                .homeLeaguePosition(stats.getHomeLeaguePosition())
                .awayLeaguePosition(stats.getAwayLeaguePosition())
                .headToHead(stats.getHeadToHead())
                .lastUpdate(stats.getLastUpdate())
                .statsEnriched(Boolean.TRUE.equals(stats.getStatsEnriched()))
                .build();
    }

    private MatchStatsResponseDTO emptyStatsDto(UUID matchId) {
        return MatchStatsResponseDTO.builder()
                .matchId(matchId)
                .homeTeamForm("TBD")
                .awayTeamForm("TBD")
                .homeTeamGoalsAvg(0.0)
                .awayTeamGoalsAvg(0.0)
                .statsEnriched(false)
                .build();
    }
}
