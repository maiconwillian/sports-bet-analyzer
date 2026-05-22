package com.betanalyzer.application.backtesting;

import com.betanalyzer.application.dto.backtesting.BacktestBetResultDTO;
import com.betanalyzer.application.dto.backtesting.BacktestResultDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class MetricsCalculationService {

    public BacktestResultDTO calculate(String strategyVersion, int matchesAnalyzed, List<BacktestBetResultDTO> bets) {
        int betsPlaced = bets.size();
        int wins = 0;
        int losses = 0;
        int voids = 0;
        BigDecimal totalProfit = BigDecimal.ZERO;
        BigDecimal totalStake = BigDecimal.ZERO;
        BigDecimal grossProfit = BigDecimal.ZERO;
        BigDecimal grossLoss = BigDecimal.ZERO;
        double totalOdds = 0;
        double totalEV = 0;
        double totalCLV = 0;
        int oddsCount = 0;
        int evCount = 0;
        int clvCount = 0;

        BigDecimal currentDrawdown = BigDecimal.ZERO;
        BigDecimal maxDrawdown = BigDecimal.ZERO;
        BigDecimal peakProfit = BigDecimal.ZERO;
        BigDecimal runningProfit = BigDecimal.ZERO;

        for (BacktestBetResultDTO bet : bets) {
            totalStake = totalStake.add(bet.getStake());
            totalProfit = totalProfit.add(bet.getProfitLoss());
            runningProfit = runningProfit.add(bet.getProfitLoss());
            
            if (bet.getResult() == BacktestBetStatus.WIN) {
                wins++;
                grossProfit = grossProfit.add(bet.getProfitLoss());
            } else if (bet.getResult() == BacktestBetStatus.LOSS) {
                losses++;
                grossLoss = grossLoss.add(bet.getProfitLoss().abs());
            } else if (bet.getResult() == BacktestBetStatus.VOID) {
                voids++;
            }

            if (bet.getOddsCaptured() != null) {
                totalOdds += bet.getOddsCaptured().doubleValue();
                oddsCount++;
            }
            if (bet.getExpectedValue() != null) {
                totalEV += bet.getExpectedValue();
                evCount++;
            }
            if (bet.getClv() != null) {
                totalCLV += bet.getClv().doubleValue();
                clvCount++;
            }

            // Max Drawdown calculation
            if (runningProfit.compareTo(peakProfit) > 0) {
                peakProfit = runningProfit;
            }
            currentDrawdown = peakProfit.subtract(runningProfit);
            if (currentDrawdown.compareTo(maxDrawdown) > 0) {
                maxDrawdown = currentDrawdown;
            }
        }

        double winRate = betsPlaced > 0 ? (double) wins / betsPlaced * 100 : 0;
        double roi = totalStake.compareTo(BigDecimal.ZERO) > 0 
                ? totalProfit.divide(totalStake, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue() 
                : 0;
        
        double profitFactor = grossLoss.compareTo(BigDecimal.ZERO) > 0 
                ? grossProfit.divide(grossLoss, 2, RoundingMode.HALF_UP).doubleValue() 
                : (grossProfit.compareTo(BigDecimal.ZERO) > 0 ? 99.0 : 0);

        return BacktestResultDTO.builder()
                .strategyVersion(strategyVersion)
                .matchesAnalyzed(matchesAnalyzed)
                .betsPlaced(betsPlaced)
                .wins(wins)
                .losses(losses)
                .voids(voids)
                .winRate(winRate)
                .roi(roi)
                .profit(totalProfit)
                .maxDrawdown(maxDrawdown.doubleValue())
                .averageOdd(oddsCount > 0 ? totalOdds / oddsCount : 0)
                .averageEV(evCount > 0 ? totalEV / evCount : 0)
                .averageCLV(clvCount > 0 ? totalCLV / clvCount : 0)
                .profitFactor(profitFactor)
                .bets(bets)
                .build();
    }
}
