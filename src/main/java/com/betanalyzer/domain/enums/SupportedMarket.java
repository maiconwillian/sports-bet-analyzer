package com.betanalyzer.domain.enums;

import lombok.Getter;

@Getter
public enum SupportedMarket {
    OVER_2_5("Over 2.5 Goals", 95, "Extremamente estatístico, muito modelável, menos emocional"),
    BTTS("Both Teams To Score", 92, "Excelente para análise estatística, padrões claros");

    private final String description;
    private final int modelingScore;
    private final String reasoning;

    SupportedMarket(String description, int modelingScore, String reasoning) {
        this.description = description;
        this.modelingScore = modelingScore;
        this.reasoning = reasoning;
    }
}
