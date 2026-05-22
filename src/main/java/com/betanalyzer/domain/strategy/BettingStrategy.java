package com.betanalyzer.domain.strategy;

import com.betanalyzer.application.dto.MatchFeatureContextDTO;
import com.betanalyzer.domain.model.Match;

/**
 * Define o contrato para uma estratégia de apostas.
 * Permite que diferentes algoritmos e lógicas sejam implementados de forma isolada.
 */
public interface BettingStrategy {
    
    /**
     * Analisa uma partida com base nas features calculadas.
     * 
     * @param match A partida bruta
     * @param features O contexto de features pré-calculadas
     * @return O resultado da análise da estratégia
     */
    StrategyResult analyze(Match match, MatchFeatureContextDTO features);

    /**
     * @return Nome único da estratégia (ex: "OVER_25_QUANT_V1")
     */
    String getStrategyName();

    /**
     * @return Versão da estratégia para rastreabilidade de ROI
     */
    String getStrategyVersion();
}
