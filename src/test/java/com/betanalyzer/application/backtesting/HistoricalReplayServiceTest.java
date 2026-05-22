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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistoricalReplayServiceTest {

    @Mock
    private FeatureCalculationService featureCalculationService;
    @Mock
    private StrategyEvaluationService strategyEvaluationService;
    @Mock
    private OddsRepository oddsRepository;

    @InjectMocks
    private HistoricalReplayService historicalReplayService;

    private Match match;
    private LocalDateTime matchDate;
    private UUID matchId;

    @BeforeEach
    void setUp() {
        matchId = UUID.randomUUID();
        matchDate = LocalDateTime.of(2025, 5, 20, 20, 0);
        match = new Match();
        match.setId(matchId);
        match.setMatchDate(matchDate);
        match.setStatus(MatchStatus.FT);
        match.setHomeGoals(2);
        match.setAwayGoals(1);
    }

    @Test
    void shouldFilterOddsAfterMatchStart() {
        // GIVEN
        String strategyVersion = "OVER25_V1";
        BigDecimal stake = BigDecimal.TEN;
        
        MatchFeatureContextDTO features = MatchFeatureContextDTO.builder()
                .homeAvgGoalsScored(1.5)
                .awayAvgGoalsConceded(1.2)
                .expectedGoalPressure(0.8)
                .build();
        when(featureCalculationService.calculateOver25Features(match)).thenReturn(features);
        
        StrategyResult strategyResult = new StrategyResult("Strategy", strategyVersion, true, "OVER_2_5", 80.0, 0.1, BigDecimal.TEN, "Reasoning");
        when(strategyEvaluationService.evaluateStrategy(eq(strategyVersion), eq(match), any())).thenReturn(strategyResult);

        Odds preMatchOdd = new Odds();
        preMatchOdd.setMarket("OVER_2_5");
        preMatchOdd.setOddsValue(BigDecimal.valueOf(1.90));
        preMatchOdd.setCapturedAt(matchDate.minusHours(2));

        Odds postMatchOdd = new Odds();
        postMatchOdd.setMarket("OVER_2_5");
        postMatchOdd.setOddsValue(BigDecimal.valueOf(2.50));
        postMatchOdd.setCapturedAt(matchDate.plusMinutes(10));

        when(oddsRepository.findByMatchIdOrderByCapturedAtAsc(matchId)).thenReturn(Arrays.asList(preMatchOdd, postMatchOdd));

        // WHEN
        Optional<BacktestBetResultDTO> result = historicalReplayService.replayMatch(match, strategyVersion, stake, 70.0);

        // THEN
        assertTrue(result.isPresent());
        assertEquals(BigDecimal.valueOf(1.90), result.get().getOddsCaptured());
        // CLV should be null because there's only one pre-match odd for the market
        assertNull(result.get().getClv());
        assertNull(result.get().getClosingOdd());
    }

    @Test
    void shouldReturnEmptyWhenNoPreMatchOddsFound() {
        // GIVEN
        String strategyVersion = "OVER25_V1";
        
        MatchFeatureContextDTO features = MatchFeatureContextDTO.builder()
                .homeAvgGoalsScored(1.5)
                .awayAvgGoalsConceded(1.2)
                .expectedGoalPressure(0.8)
                .build();
        when(featureCalculationService.calculateOver25Features(match)).thenReturn(features);
        
        StrategyResult strategyResult = new StrategyResult("Strategy", strategyVersion, true, "OVER_2_5", 80.0, 0.1, BigDecimal.TEN, "Reasoning");
        when(strategyEvaluationService.evaluateStrategy(eq(strategyVersion), eq(match), any())).thenReturn(strategyResult);

        Odds postMatchOdd = new Odds();
        postMatchOdd.setMarket("OVER_2_5");
        postMatchOdd.setOddsValue(BigDecimal.valueOf(2.50));
        postMatchOdd.setCapturedAt(matchDate.plusMinutes(10));

        when(oddsRepository.findByMatchIdOrderByCapturedAtAsc(matchId)).thenReturn(Collections.singletonList(postMatchOdd));

        // WHEN
        Optional<BacktestBetResultDTO> result = historicalReplayService.replayMatch(match, strategyVersion, BigDecimal.TEN, 70.0);

        // THEN
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldHandleDifferentMarketNames() {
        // GIVEN
        String strategyVersion = "OVER25_V1";
        
        MatchFeatureContextDTO features = MatchFeatureContextDTO.builder()
                .homeAvgGoalsScored(1.5)
                .awayAvgGoalsConceded(1.2)
                .expectedGoalPressure(0.8)
                .build();
        when(featureCalculationService.calculateOver25Features(match)).thenReturn(features);
        
        // Strategy returns "OVER_2_5"
        StrategyResult strategyResult = new StrategyResult("Strategy", strategyVersion, true, "OVER_2_5", 80.0, 0.1, BigDecimal.TEN, "Reasoning");
        when(strategyEvaluationService.evaluateStrategy(eq(strategyVersion), eq(match), any())).thenReturn(strategyResult);

        // Repository has "TOTALS_OVER_2_5"
        Odds odd = new Odds();
        odd.setMarket("TOTALS_OVER_2_5");
        odd.setOddsValue(BigDecimal.valueOf(1.90));
        odd.setCapturedAt(matchDate.minusHours(2));

        when(oddsRepository.findByMatchIdOrderByCapturedAtAsc(matchId)).thenReturn(Collections.singletonList(odd));

        // WHEN
        Optional<BacktestBetResultDTO> result = historicalReplayService.replayMatch(match, strategyVersion, BigDecimal.TEN, 70.0);

        // THEN
        assertTrue(result.isPresent());
        assertEquals("OVER_2_5", result.get().getMarket());
        assertEquals(BigDecimal.valueOf(1.90), result.get().getOddsCaptured());
    }
}
