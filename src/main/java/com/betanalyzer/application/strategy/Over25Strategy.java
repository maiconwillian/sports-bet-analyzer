package com.betanalyzer.application.strategy;

import com.betanalyzer.application.dto.MatchFeatureContextDTO;
import com.betanalyzer.domain.model.Match;
import com.betanalyzer.domain.strategy.BettingStrategy;
import com.betanalyzer.domain.strategy.StrategyResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Estratégia quantitativa para Over 2.5 gols.
 * Baseia-se em médias de gols, taxas de over e inteligência combinada.
 */
@Component
@Slf4j
public class Over25Strategy implements BettingStrategy {

    private static final String STRATEGY_NAME = "OVER_25_QUANT";
    private static final String VERSION = "1.0.0";
    private static final String MARKET = "OVER_2_5";

    @Override
    public StrategyResult analyze(Match match, MatchFeatureContextDTO features) {
        double confidence = calculateConfidence(features);
        double expectedValue = calculateExpectedValue(confidence);
        boolean shouldBet = confidence >= 65.0 && expectedValue > 0.0;
        String reasoning = buildReasoning(features, confidence);
        
        log.info("Over25Strategy result for {}: shouldBet={}, confidence={}, ev={}", 
            match.getId(), shouldBet, confidence, expectedValue);
        
        return new StrategyResult(
            STRATEGY_NAME,
            VERSION,
            shouldBet,
            MARKET,
            confidence,
            expectedValue,
            new BigDecimal("100.00"),
            reasoning
        );
    }

    private double calculateConfidence(MatchFeatureContextDTO features) {
        double combinedAvg = features.getCombinedGoalAverage() != null ? features.getCombinedGoalAverage() : 0.0;
        
        // Base: combinedAvg * 25 (0-100%)
        double confidence = combinedAvg * 25;
        
        // Ajustes
        if (features.getHomeOver25Rate() != null && features.getHomeOver25Rate() > 75) {
            confidence += 10;
        }
        if (features.getAwayOver25Rate() != null && features.getAwayOver25Rate() > 75) {
            confidence += 10;
        }
        if (features.getHomeCleanSheetRate() != null && features.getHomeCleanSheetRate() > 60) {
            confidence -= 5;
        }
        if (features.getAwayCleanSheetRate() != null && features.getAwayCleanSheetRate() > 60) {
            confidence -= 5;
        }
        
        return Math.min(Math.max(confidence, 0), 100);
    }

    private double calculateExpectedValue(double confidence) {
        // Assume odd de 1.90 para Over 2.5
        double odd = 1.90;
        return (confidence / 100) * odd - 1.0;
    }

    private String buildReasoning(MatchFeatureContextDTO features, double confidence) {
        double combinedAvg = features.getCombinedGoalAverage() != null ? features.getCombinedGoalAverage() : 0.0;
        
        return String.format("Combined goals %.2f + Confidence %.1f%% → Over 2.5 %s",
            combinedAvg,
            confidence,
            confidence >= 65.0 ? "RECOMENDADO" : "NÃO RECOMENDADO"
        );
    }

    @Override
    public String getStrategyName() {
        return STRATEGY_NAME;
    }

    @Override
    public String getStrategyVersion() {
        return VERSION;
    }
}
