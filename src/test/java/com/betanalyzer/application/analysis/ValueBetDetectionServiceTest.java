package com.betanalyzer.application.analysis;

import com.betanalyzer.application.feature.service.FeatureCalculationService;
import com.betanalyzer.application.strategy.StrategyEvaluationService;
import com.betanalyzer.config.ValueBetProperties;
import com.betanalyzer.domain.enums.MatchStatus;
import com.betanalyzer.domain.model.*;
import com.betanalyzer.domain.strategy.StrategyResult;
import com.betanalyzer.infrastructure.persistence.MatchRepository;
import com.betanalyzer.infrastructure.persistence.MatchStatsRepository;
import com.betanalyzer.infrastructure.persistence.OddsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValueBetDetectionServiceTest {

    @Mock
    private MatchRepository matchRepository;
    @Mock
    private OddsRepository oddsRepository;
    @Mock
    private FeatureCalculationService featureCalculationService;
    @Mock
    private StrategyEvaluationService strategyEvaluationService;
    @Mock
    private MatchStatsRepository matchStatsRepository;
    @Mock
    private ValueBetProperties valueBetProperties;

    @InjectMocks
    private ValueBetDetectionService service;

    private Match match;
    private UUID matchId;

    @BeforeEach
    void setUp() {
        matchId = UUID.randomUUID();
        League league = League.builder().apiId(1L).name("Premier League").country("England").season(2024).build();
        Team home = Team.builder().apiId(10L).name("Home FC").build();
        Team away = Team.builder().apiId(20L).name("Away FC").build();
        match = Match.builder()
                .id(matchId)
                .apiId(99L)
                .league(league)
                .homeTeam(home)
                .awayTeam(away)
                .status(MatchStatus.NS)
                .matchDate(LocalDateTime.now().plusDays(1))
                .build();

        when(valueBetProperties.getMinConfidence()).thenReturn(65.0);
        when(valueBetProperties.getMinEv()).thenReturn(0.05);
        when(valueBetProperties.getKellyFraction()).thenReturn(0.25);
        when(valueBetProperties.getBankroll()).thenReturn(1000.0);
    }

    @Test
    void scanMatch_shouldReturnOpportunityWhenStatsAndStrategyPass() {
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(featureCalculationService.calculateOver25Features(match)).thenReturn(
                com.betanalyzer.application.dto.MatchFeatureContextDTO.builder()
                        .combinedGoalAverage(3.0)
                        .homeOver25Rate(80.0)
                        .awayOver25Rate(80.0)
                        .build());
        when(strategyEvaluationService.evaluateStrategy(eq("OVER_25_QUANT"), eq(match), any()))
                .thenReturn(new StrategyResult("OVER_25_QUANT", "1.0.0", true, "OVER_2_5", 75.0, 0.1,
                        BigDecimal.TEN, "Strong over signal"));

        Odds odd = Odds.builder()
                .market("TOTALS_OVER_2_5")
                .oddsValue(BigDecimal.valueOf(2.0))
                .bookmaker("Pinnacle")
                .build();
        when(oddsRepository.findByMatchId(matchId)).thenReturn(List.of(odd));

        var result = service.scanMatch(matchId);

        assertTrue(result.isPresent());
        assertEquals(75.0, result.get().getConfidence());
        assertTrue(result.get().getExpectedValue() >= 0.05);
    }

    @Test
    void scanMatch_shouldReturnEmptyWhenConfidenceLow() {
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(featureCalculationService.calculateOver25Features(match)).thenReturn(
                com.betanalyzer.application.dto.MatchFeatureContextDTO.builder()
                        .combinedGoalAverage(1.0)
                        .build());
        when(strategyEvaluationService.evaluateStrategy(eq("OVER_25_QUANT"), eq(match), any()))
                .thenReturn(new StrategyResult("OVER_25_QUANT", "1.0.0", false, "OVER_2_5", 25.0, -0.5,
                        BigDecimal.TEN, "Weak"));

        Odds odd = Odds.builder()
                .market("TOTALS_OVER_2_5")
                .oddsValue(BigDecimal.valueOf(2.0))
                .bookmaker("Pinnacle")
                .build();
        when(oddsRepository.findByMatchId(matchId)).thenReturn(List.of(odd));

        assertTrue(service.scanMatch(matchId).isEmpty());
    }
}
