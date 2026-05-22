package com.betanalyzer.application.strategy;

import com.betanalyzer.application.dto.MatchFeatureContextDTO;
import com.betanalyzer.domain.model.Match;
import com.betanalyzer.domain.strategy.StrategyResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class Over25StrategyTest {

    private Over25Strategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new Over25Strategy();
    }

    @Test
    void shouldReturnShouldBetTrueWhenConditionsMet() {
        // Arrange
        Match match = Match.builder().build();
        MatchFeatureContextDTO features = MatchFeatureContextDTO.builder()
                .combinedGoalAverage(3.0)
                .homeOver25Rate(80.0)
                .awayOver25Rate(80.0)
                .build();

        // Act
        StrategyResult result = strategy.analyze(match, features);

        // Assert
        assertThat(result.shouldBet()).isTrue();
        assertThat(result.strategyName()).isEqualTo("OVER_25_QUANT");
        assertThat(result.market()).isEqualTo("OVER_2_5");
        assertThat(result.recommendedStake()).isEqualTo(new BigDecimal("100.00"));
        assertThat(result.confidence()).isGreaterThanOrEqualTo(65.0);
    }

    @Test
    void shouldReturnShouldBetFalseWhenGoalAvgLow() {
        // Arrange
        Match match = Match.builder().build();
        MatchFeatureContextDTO features = MatchFeatureContextDTO.builder()
                .combinedGoalAverage(1.0)
                .build();

        // Act
        StrategyResult result = strategy.analyze(match, features);

        // Assert
        assertThat(result.shouldBet()).isFalse();
    }

    @Test
    void shouldReturnShouldBetFalseWhenConfidenceLow() {
        // Arrange
        Match match = Match.builder().build();
        MatchFeatureContextDTO features = MatchFeatureContextDTO.builder()
                .combinedGoalAverage(2.0) // 2.0 * 25 = 50% base
                .build();

        // Act
        StrategyResult result = strategy.analyze(match, features);

        // Assert
        assertThat(result.shouldBet()).isFalse();
    }
}
