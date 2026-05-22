package com.betanalyzer.application.strategy;

import com.betanalyzer.application.dto.MatchFeatureContextDTO;
import com.betanalyzer.domain.model.Match;
import com.betanalyzer.domain.strategy.BettingStrategy;
import com.betanalyzer.domain.strategy.StrategyResult;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Motor central que executa todas as estratégias registradas para uma partida.
 */
@Service
@RequiredArgsConstructor
public class StrategyEvaluationService {

    private static final Logger log = LoggerFactory.getLogger(StrategyEvaluationService.class);
    
    // Spring injeta automaticamente todos os beans que implementam BettingStrategy
    private final List<BettingStrategy> strategies;

    /**
     * Executa todas as estratégias disponíveis para uma determinada partida e suas features.
     *
     * @param match A partida
     * @param features Contexto de features calculadas
     * @return Lista de resultados de cada estratégia
     */
    public List<StrategyResult> evaluateAll(Match match, MatchFeatureContextDTO features) {
        log.info("Evaluating {} strategies for match: {} vs {}", 
                strategies.size(), match.getHomeTeam().getName(), match.getAwayTeam().getName());

        return strategies.stream()
                .map(strategy -> {
                    try {
                        return strategy.analyze(match, features);
                    } catch (Exception e) {
                        log.error("Error executing strategy {}: {}", strategy.getStrategyName(), e.getMessage());
                        return null;
                    }
                })
                .filter(result -> result != null)
                .collect(Collectors.toList());
    }

    public StrategyResult evaluateStrategy(String strategyName, Match match, MatchFeatureContextDTO features) {
        log.info("Evaluating strategy {} for match: {} vs {}", 
                strategyName, match.getHomeTeam().getName(), match.getAwayTeam().getName());

        return strategies.stream()
                .filter(strategy -> strategy.getStrategyName().equals(strategyName))
                .findFirst()
                .map(strategy -> {
                    try {
                        return strategy.analyze(match, features);
                    } catch (Exception e) {
                        log.error("Error executing strategy {}: {}", strategyName, e.getMessage());
                        return null;
                    }
                })
                .orElseThrow(() -> new IllegalArgumentException("Strategy not found: " + strategyName));
    }
}
