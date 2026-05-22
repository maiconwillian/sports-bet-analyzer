package com.betanalyzer.application.backtesting;

import com.betanalyzer.application.dto.backtesting.BacktestRequest;
import com.betanalyzer.domain.enums.SupportedLeague;
import com.betanalyzer.infrastructure.persistence.LeagueRepository;
import com.betanalyzer.infrastructure.persistence.MatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class BacktestingServiceValidationTest {

    @Mock
    private MatchRepository matchRepository;
    @Mock
    private LeagueRepository leagueRepository;
    @Mock
    private HistoricalReplayService historicalReplayService;
    @Mock
    private MetricsCalculationService metricsCalculationService;

    @InjectMocks
    private BacktestingService backtestingService;

    private BacktestRequest.BacktestRequestBuilder requestBuilder;

    @BeforeEach
    void setUp() {
        requestBuilder = BacktestRequest.builder()
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 1, 31))
                .league(SupportedLeague.PREMIER_LEAGUE)
                .strategyVersion("OVER_25_V1")
                .stake(BigDecimal.TEN)
                .simulationMode(SimulationMode.FIXED_STAKE);
    }

    @Test
    void shouldThrowExceptionWhenStartDateIsAfterEndDate() {
        BacktestRequest request = requestBuilder
                .startDate(LocalDate.of(2025, 2, 1))
                .endDate(LocalDate.of(2025, 1, 31))
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
                backtestingService.runBacktest(request));
        
        assertEquals("startDate must be before or equal to endDate", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenSimulationModeIsNotFixedStake() {
        BacktestRequest request = requestBuilder
                .simulationMode(SimulationMode.PERCENTAGE_BANKROLL)
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
                backtestingService.runBacktest(request));
        
        assertEquals("Only FIXED_STAKE simulation mode is supported in Phase 1.7 V1", exception.getMessage());
    }
}
