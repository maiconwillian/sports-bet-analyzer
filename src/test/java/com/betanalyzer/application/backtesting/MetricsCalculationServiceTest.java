package com.betanalyzer.application.backtesting;

import com.betanalyzer.application.dto.backtesting.BacktestBetResultDTO;
import com.betanalyzer.application.dto.backtesting.BacktestResultDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MetricsCalculationServiceTest {

    private MetricsCalculationService metricsCalculationService;

    @BeforeEach
    void setUp() {
        metricsCalculationService = new MetricsCalculationService();
    }

    @Test
    void shouldCalculateMetricsCorrectly() {
        // Given
        BacktestBetResultDTO winBet = BacktestBetResultDTO.builder()
                .matchId(UUID.randomUUID())
                .stake(BigDecimal.valueOf(10))
                .oddsCaptured(BigDecimal.valueOf(2.0))
                .result(BacktestBetStatus.WIN)
                .profitLoss(BigDecimal.valueOf(10)) // 10 * (2.0 - 1)
                .expectedValue(0.1)
                .clv(BigDecimal.valueOf(5))
                .build();

        BacktestBetResultDTO lossBet = BacktestBetResultDTO.builder()
                .matchId(UUID.randomUUID())
                .stake(BigDecimal.valueOf(10))
                .oddsCaptured(BigDecimal.valueOf(2.0))
                .result(BacktestBetStatus.LOSS)
                .profitLoss(BigDecimal.valueOf(-10))
                .expectedValue(0.1)
                .clv(BigDecimal.valueOf(-2))
                .build();

        List<BacktestBetResultDTO> bets = List.of(winBet, lossBet);

        // When
        BacktestResultDTO result = metricsCalculationService.calculate("V1", 10, bets);

        // Then
        assertEquals(10, result.getMatchesAnalyzed());
        assertEquals(2, result.getBetsPlaced());
        assertEquals(1, result.getWins());
        assertEquals(1, result.getLosses());
        assertEquals(50.0, result.getWinRate());
        assertEquals(0.0, result.getRoi()); // (10 - 10) / 20 * 100
        assertEquals(BigDecimal.ZERO, result.getProfit().stripTrailingZeros());
        assertEquals(1.0, result.getProfitFactor()); // 10 / 10
        assertEquals(2.0, result.getAverageOdd());
        assertEquals(0.1, result.getAverageEV());
        assertEquals(1.5, result.getAverageCLV()); // (5 - 2) / 2
    }

    @Test
    void shouldCalculateMaxDrawdownCorrectly() {
        // Sequence of bets: WIN (+10), LOSS (-10), LOSS (-10), WIN (+10)
        // Profit sequence: 10, 0, -10, 0
        // Peak: 10
        // Drawdown at step 3: 10 - (-10) = 20
        
        BacktestBetResultDTO win1 = createBet(10, BacktestBetStatus.WIN, 10);
        BacktestBetResultDTO loss1 = createBet(-10, BacktestBetStatus.LOSS, 10);
        BacktestBetResultDTO loss2 = createBet(-10, BacktestBetStatus.LOSS, 10);
        BacktestBetResultDTO win2 = createBet(10, BacktestBetStatus.WIN, 10);

        List<BacktestBetResultDTO> bets = List.of(win1, loss1, loss2, win2);

        BacktestResultDTO result = metricsCalculationService.calculate("V1", 4, bets);

        assertEquals(0.0, result.getProfit().doubleValue());
        assertEquals(20.0, result.getMaxDrawdown());
    }

    @Test
    void shouldCalculateAverageClvIgnoringNullValues() {
        BacktestBetResultDTO betWithClv = BacktestBetResultDTO.builder()
                .matchId(UUID.randomUUID())
                .stake(BigDecimal.valueOf(10))
                .oddsCaptured(BigDecimal.valueOf(2.0))
                .result(BacktestBetStatus.WIN)
                .profitLoss(BigDecimal.valueOf(10))
                .clv(BigDecimal.valueOf(6))
                .build();

        BacktestBetResultDTO betWithoutClv = BacktestBetResultDTO.builder()
                .matchId(UUID.randomUUID())
                .stake(BigDecimal.valueOf(10))
                .oddsCaptured(BigDecimal.valueOf(2.0))
                .result(BacktestBetStatus.LOSS)
                .profitLoss(BigDecimal.valueOf(-10))
                .clv(null)
                .build();

        BacktestResultDTO result = metricsCalculationService.calculate("V1", 2, List.of(betWithClv, betWithoutClv));

        assertEquals(6.0, result.getAverageCLV());
    }

    private BacktestBetResultDTO createBet(double profit, BacktestBetStatus status, double stake) {
        return BacktestBetResultDTO.builder()
                .matchId(UUID.randomUUID())
                .stake(BigDecimal.valueOf(stake))
                .oddsCaptured(BigDecimal.valueOf(2.0))
                .result(status)
                .profitLoss(BigDecimal.valueOf(profit))
                .build();
    }
}
