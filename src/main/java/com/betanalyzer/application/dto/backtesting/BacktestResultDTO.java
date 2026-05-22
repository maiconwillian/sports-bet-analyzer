package com.betanalyzer.application.dto.backtesting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BacktestResultDTO {
    private String strategyVersion;
    private int matchesAnalyzed;
    private int betsPlaced;
    private int wins;
    private int losses;
    private int voids;
    private double winRate;
    private double roi;
    private BigDecimal profit;
    private double maxDrawdown;
    private double averageOdd;
    private double averageEV;
    private double averageCLV;
    private double profitFactor;
    private List<BacktestBetResultDTO> bets;
}
