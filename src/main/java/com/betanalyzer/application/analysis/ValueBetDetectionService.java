package com.betanalyzer.application.analysis;

import com.betanalyzer.application.dto.MatchFeatureContextDTO;
import com.betanalyzer.application.dto.ValueBetOpportunityResponse;
import com.betanalyzer.application.dto.ValueBetsScanResponse;
import com.betanalyzer.domain.model.MatchStats;
import com.betanalyzer.infrastructure.persistence.MatchStatsRepository;
import com.betanalyzer.application.feature.service.FeatureCalculationService;
import com.betanalyzer.application.strategy.StrategyEvaluationService;
import com.betanalyzer.config.ValueBetProperties;
import com.betanalyzer.domain.enums.MatchStatus;
import com.betanalyzer.domain.enums.SupportedLeague;
import com.betanalyzer.domain.model.Match;
import com.betanalyzer.domain.model.Odds;
import com.betanalyzer.domain.strategy.StrategyResult;
import com.betanalyzer.infrastructure.persistence.MatchRepository;
import com.betanalyzer.infrastructure.persistence.OddsRepository;
import com.betanalyzer.shared.util.MarketUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ValueBetDetectionService {

    private static final String STRATEGY_NAME = "OVER_25_QUANT";

    private final MatchRepository matchRepository;
    private final OddsRepository oddsRepository;
    private final FeatureCalculationService featureCalculationService;
    private final StrategyEvaluationService strategyEvaluationService;
    private final ValueBetProperties valueBetProperties;
    private final MatchStatsRepository matchStatsRepository;

    @Transactional(readOnly = true)
    public ValueBetsScanResponse scanValueBetsWithMeta(LocalDate date, SupportedLeague leagueFilter) {
        List<ValueBetOpportunityResponse> opportunities = scanValueBets(date, leagueFilter);
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);

        List<Match> eligible = matchRepository.findByMatchDateBetween(start, end).stream()
                .filter(m -> m.getStatus() == MatchStatus.NS || m.getStatus() == MatchStatus.TBD)
                .filter(m -> SupportedLeague.findByLeague(m.getLeague().getName(), m.getLeague().getCountry()).isPresent())
                .filter(m -> leagueFilter == null || matchesLeague(m, leagueFilter))
                .toList();

        int enriched = 0;
        for (Match m : eligible) {
            Optional<MatchStats> stats = matchStatsRepository.findByMatchId(m.getId());
            if (stats.isPresent() && stats.get().hasUsableEnrichment()) {
                enriched++;
            }
        }

        boolean statsIncomplete = eligible.isEmpty() || enriched < eligible.size();
        String hint = statsIncomplete
                ? "Execute Admin → Enriquecer análise na data antes de esperar oportunidades EV+. "
                + enriched + "/" + eligible.size() + " partidas com stats."
                : null;

        return ValueBetsScanResponse.builder()
                .date(date)
                .opportunities(opportunities)
                .matchesConsidered(eligible.size())
                .matchesWithEnrichedStats(enriched)
                .statsIncomplete(statsIncomplete)
                .hint(hint)
                .build();
    }

    @Transactional(readOnly = true)
    public List<ValueBetOpportunityResponse> scanValueBets(LocalDate date, SupportedLeague leagueFilter) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);

        List<Match> matches = matchRepository.findByMatchDateBetween(start, end).stream()
                .filter(m -> m.getStatus() == MatchStatus.NS || m.getStatus() == MatchStatus.TBD)
                .filter(m -> SupportedLeague.findByLeague(m.getLeague().getName(), m.getLeague().getCountry()).isPresent())
                .filter(m -> leagueFilter == null || matchesLeague(m, leagueFilter))
                .toList();

        List<ValueBetOpportunityResponse> opportunities = new ArrayList<>();

        for (Match match : matches) {
            scanMatch(match).ifPresent(opportunities::add);
        }

        opportunities.sort(Comparator.comparing(ValueBetOpportunityResponse::getExpectedValue).reversed());
        return opportunities;
    }

    @Transactional(readOnly = true)
    public Optional<ValueBetOpportunityResponse> scanMatch(UUID matchId) {
        return matchRepository.findById(matchId).flatMap(this::scanMatch);
    }

    private Optional<ValueBetOpportunityResponse> scanMatch(Match match) {
        if (SupportedLeague.findByLeague(match.getLeague().getName(), match.getLeague().getCountry()).isEmpty()) {
            return Optional.empty();
        }

        MatchFeatureContextDTO features = featureCalculationService.calculateOver25Features(match);
        StrategyResult strategyResult;
        try {
            strategyResult = strategyEvaluationService.evaluateStrategy(STRATEGY_NAME, match, features);
        } catch (Exception e) {
            log.debug("Strategy evaluation failed for match {}: {}", match.getId(), e.getMessage());
            return Optional.empty();
        }

        Optional<Odds> bestOdd = findBestOver25Odd(match.getId());
        if (bestOdd.isEmpty()) {
            return Optional.empty();
        }

        Odds odds = bestOdd.get();
        double oddValue = odds.getOddsValue().doubleValue();
        double modelProb = strategyResult.confidence() / 100.0;
        double ev = modelProb * oddValue - 1.0;

        if (strategyResult.confidence() < valueBetProperties.getMinConfidence()
                || ev < valueBetProperties.getMinEv()) {
            return Optional.empty();
        }

        double kellyFull = calculateKellyFraction(modelProb, oddValue);
        double kellyApplied = kellyFull * valueBetProperties.getKellyFraction();
        BigDecimal suggestedStake = BigDecimal.valueOf(valueBetProperties.getBankroll() * kellyApplied)
                .setScale(2, RoundingMode.HALF_UP);

        return Optional.of(ValueBetOpportunityResponse.builder()
                .matchId(match.getId())
                .homeTeamName(match.getHomeTeam().getName())
                .awayTeamName(match.getAwayTeam().getName())
                .leagueName(match.getLeague().getName())
                .leagueCountry(match.getLeague().getCountry())
                .market(strategyResult.market())
                .bookmaker(odds.getBookmaker())
                .odd(odds.getOddsValue())
                .modelProbability(modelProb)
                .confidence(strategyResult.confidence())
                .expectedValue(ev)
                .kellyFraction(kellyApplied)
                .suggestedStake(suggestedStake)
                .strategyName(strategyResult.strategyName())
                .reasoning(strategyResult.reasoning())
                .matchDate(match.getMatchDate())
                .build());
    }

    private Optional<Odds> findBestOver25Odd(UUID matchId) {
        return oddsRepository.findByMatchId(matchId).stream()
                .filter(o -> o.getMarket() != null && MarketUtils.isOver25Market(o.getMarket()))
                .max(Comparator.comparing(o -> o.getOddsValue()));
    }

    public BigDecimal suggestStake(double confidencePercent, BigDecimal odd) {
        if (odd == null || odd.doubleValue() <= 1.0) {
            return BigDecimal.valueOf(100.0).setScale(2, RoundingMode.HALF_UP);
        }
        double modelProb = confidencePercent / 100.0;
        double kellyFull = calculateKellyFraction(modelProb, odd.doubleValue());
        double kellyApplied = kellyFull * valueBetProperties.getKellyFraction();
        return BigDecimal.valueOf(valueBetProperties.getBankroll() * kellyApplied)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private double calculateKellyFraction(double probability, double odd) {
        double b = odd - 1.0;
        if (b <= 0) {
            return 0.0;
        }
        double q = 1.0 - probability;
        double kelly = (b * probability - q) / b;
        return Math.max(kelly, 0.0);
    }

    private static boolean matchesLeague(Match match, SupportedLeague league) {
        return league.matches(match.getLeague().getName(), match.getLeague().getCountry());
    }
}
