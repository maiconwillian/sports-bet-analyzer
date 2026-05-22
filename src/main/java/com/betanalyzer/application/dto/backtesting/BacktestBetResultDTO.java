package com.betanalyzer.application.dto.backtesting;

import com.betanalyzer.application.backtesting.BacktestBetStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BacktestBetResultDTO {
    private UUID matchId;
    private String strategyVersion;
    private String market;
    private BigDecimal oddsCaptured;
    private BigDecimal closingOdd;
    private BigDecimal clv;
    private BigDecimal stake;
    private Double confidenceScore;
    private Double expectedValue;
    private BacktestBetStatus result;
    private BigDecimal profitLoss;
    private LocalDateTime matchDate;
    private String score;
    private Map<String, Object> features;
    private String reasoning;
}
