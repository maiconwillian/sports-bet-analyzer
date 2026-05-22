package com.betanalyzer.application.strategy;

import com.betanalyzer.application.dto.MatchFeatureContextDTO;
import com.betanalyzer.domain.model.Match;
import com.betanalyzer.domain.model.Team;
import com.betanalyzer.domain.strategy.BettingStrategy;
import com.betanalyzer.domain.strategy.StrategyResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StrategyEvaluationServiceTest {

    private StrategyEvaluationService evaluationService;

    @Mock
    private BettingStrategy strategy1;

    @Mock
    private BettingStrategy strategy2;

    @BeforeEach
    void setUp() {
        evaluationService = new StrategyEvaluationService(List.of(strategy1, strategy2));
    }

    @Test
    void shouldEvaluateAllStrategiesSuccessfully() {
        // Arrange
        Match match = Match.builder()
                .homeTeam(Team.builder().name("Flamengo").build())
                .awayTeam(Team.builder().name("Palmeiras").build())
                .build();
        MatchFeatureContextDTO features = MatchFeatureContextDTO.builder().build();

        when(strategy1.analyze(any(), any())).thenReturn(new StrategyResult("S1", "1.0", true, "MARKET", 70.0, 0.1, java.math.BigDecimal.TEN, "Reason"));
        when(strategy2.analyze(any(), any())).thenReturn(new StrategyResult("S2", "1.0", false, "MARKET", 40.0, -0.1, java.math.BigDecimal.TEN, "Reason"));

        // Act
        List<StrategyResult> results = evaluationService.evaluateAll(match, features);

        // Assert
        assertThat(results).hasSize(2);
        assertThat(results.get(0).strategyName()).isEqualTo("S1");
        assertThat(results.get(1).strategyName()).isEqualTo("S2");
        verify(strategy1).analyze(match, features);
        verify(strategy2).analyze(match, features);
    }

    @Test
    void shouldHandleStrategyExceptionAndContinue() {
        // Arrange
        Match match = Match.builder()
                .homeTeam(Team.builder().name("Flamengo").build())
                .awayTeam(Team.builder().name("Palmeiras").build())
                .build();
        MatchFeatureContextDTO features = MatchFeatureContextDTO.builder().build();

        when(strategy1.analyze(any(), any())).thenThrow(new RuntimeException("Strategy failed"));
        when(strategy2.analyze(any(), any())).thenReturn(new StrategyResult("S2", "1.0", false, "MARKET", 40.0, -0.1, java.math.BigDecimal.TEN, "Reason"));

        // Act
        List<StrategyResult> results = evaluationService.evaluateAll(match, features);

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).strategyName()).isEqualTo("S2");
        verify(strategy1).analyze(match, features);
        verify(strategy2).analyze(match, features);
    }

    @Test
    void shouldEvaluateSpecificStrategySuccessfully() {
        // Arrange
        Match match = Match.builder()
                .homeTeam(Team.builder().name("Flamengo").build())
                .awayTeam(Team.builder().name("Palmeiras").build())
                .build();
        MatchFeatureContextDTO features = MatchFeatureContextDTO.builder().build();
        StrategyResult expectedResult = new StrategyResult("S1", "1.0", true, "MARKET", 70.0, 0.1, java.math.BigDecimal.TEN, "Reason");

        when(strategy1.getStrategyName()).thenReturn("S1");
        when(strategy1.analyze(any(), any())).thenReturn(expectedResult);

        // Act
        StrategyResult result = evaluationService.evaluateStrategy("S1", match, features);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.strategyName()).isEqualTo("S1");
        verify(strategy1).analyze(match, features);
    }
}
