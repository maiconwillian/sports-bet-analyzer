package com.betanalyzer.application.analysis;

import com.betanalyzer.application.dto.MatchInsightRowDTO;
import com.betanalyzer.application.dto.MatchInsightsResponseDTO;
import com.betanalyzer.application.feature.service.FeatureCalculationService;
import com.betanalyzer.application.strategy.StrategyEvaluationService;
import com.betanalyzer.config.ValueBetProperties;
import com.betanalyzer.domain.enums.MatchStatus;
import com.betanalyzer.domain.enums.SupportedLeague;
import com.betanalyzer.domain.model.Match;
import com.betanalyzer.domain.model.MatchStats;
import com.betanalyzer.domain.model.Odds;
import com.betanalyzer.domain.strategy.StrategyResult;
import com.betanalyzer.infrastructure.persistence.MatchRepository;
import com.betanalyzer.infrastructure.persistence.MatchStatsRepository;
import com.betanalyzer.infrastructure.persistence.OddsRepository;
import com.betanalyzer.shared.util.MarketUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MatchInsightsService {

    private static final String STRATEGY_NAME = "OVER_25_QUANT";
    private static final double NEAR_CONFIDENCE_THRESHOLD = 55.0;

    private final MatchRepository matchRepository;
    private final MatchStatsRepository matchStatsRepository;
    private final OddsRepository oddsRepository;
    private final FeatureCalculationService featureCalculationService;
    private final StrategyEvaluationService strategyEvaluationService;
    private final ValueBetProperties valueBetProperties;

    @Transactional(readOnly = true)
    public MatchInsightsResponseDTO getMatchInsights(LocalDate date, SupportedLeague leagueFilter) {
        List<Match> eligible = findEligibleMatches(date, leagueFilter);
        List<MatchInsightRowDTO> insights = new ArrayList<>();

        for (Match match : eligible) {
            insights.add(buildInsightRow(match));
        }

        insights.sort(Comparator.comparing(MatchInsightRowDTO::confidence, Comparator.nullsLast(Comparator.reverseOrder())));

        return MatchInsightsResponseDTO.builder()
                .date(date)
                .insights(insights)
                .minConfidence(valueBetProperties.getMinConfidence())
                .minEv(valueBetProperties.getMinEv())
                .matchesConsidered(eligible.size())
                .build();
    }

    private List<Match> findEligibleMatches(LocalDate date, SupportedLeague leagueFilter) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);

        return matchRepository.findByMatchDateBetween(start, end).stream()
                .filter(m -> m.getStatus() == MatchStatus.NS || m.getStatus() == MatchStatus.TBD)
                .filter(m -> SupportedLeague.findByLeague(m.getLeague().getName(), m.getLeague().getCountry()).isPresent())
                .filter(m -> leagueFilter == null || matchesLeague(m, leagueFilter))
                .toList();
    }

    private MatchInsightRowDTO buildInsightRow(Match match) {
        Optional<MatchStats> statsOpt = matchStatsRepository.findByMatchId(match.getId());
        boolean statsReady = statsOpt.map(MatchStats::hasUsableEnrichment).orElse(false);
        Optional<Odds> bestOdd = findBestOver25Odd(match.getId());
        boolean hasOdds = bestOdd.isPresent();

        double confidence = 0.0;
        Double expectedValue = null;
        boolean passesEvFilters = false;
        String signalTier = "NO_STATS";

        if (statsReady) {
            var features = featureCalculationService.calculateOver25Features(match);
            StrategyResult strategyResult = strategyEvaluationService.evaluateStrategy(STRATEGY_NAME, match, features);
            confidence = strategyResult.confidence();

            if (bestOdd.isPresent()) {
                double oddValue = bestOdd.get().getOddsValue().doubleValue();
                expectedValue = (confidence / 100.0) * oddValue - 1.0;
                passesEvFilters = confidence >= valueBetProperties.getMinConfidence()
                        && expectedValue >= valueBetProperties.getMinEv();
            }

            signalTier = resolveSignalTier(confidence, passesEvFilters, statsReady);
        } else if (hasOdds) {
            signalTier = "NO_STATS";
        }

        return MatchInsightRowDTO.builder()
                .matchId(match.getId())
                .homeTeamName(match.getHomeTeam().getName())
                .awayTeamName(match.getAwayTeam().getName())
                .leagueName(match.getLeague().getName())
                .leagueCountry(match.getLeague().getCountry())
                .matchDate(match.getMatchDate())
                .status(match.getStatus())
                .statsReady(statsReady)
                .hasOdds(hasOdds)
                .confidence(confidence)
                .expectedValue(expectedValue)
                .referenceOdd(bestOdd.map(Odds::getOddsValue).orElse(null))
                .bookmaker(bestOdd.map(Odds::getBookmaker).orElse(null))
                .passesEvFilters(passesEvFilters)
                .signalTier(signalTier)
                .build();
    }

    static String resolveSignalTier(double confidence, boolean passesEvFilters, boolean statsReady) {
        if (!statsReady) {
            return "NO_STATS";
        }
        if (passesEvFilters) {
            return "EV_PLUS";
        }
        if (confidence >= NEAR_CONFIDENCE_THRESHOLD) {
            return "NEAR";
        }
        return "WEAK";
    }

    private Optional<Odds> findBestOver25Odd(java.util.UUID matchId) {
        return oddsRepository.findByMatchId(matchId).stream()
                .filter(o -> o.getMarket() != null && MarketUtils.isOver25Market(o.getMarket()))
                .max(Comparator.comparing(Odds::getOddsValue));
    }

    private static boolean matchesLeague(Match match, SupportedLeague league) {
        return league.matches(match.getLeague().getName(), match.getLeague().getCountry());
    }
}
