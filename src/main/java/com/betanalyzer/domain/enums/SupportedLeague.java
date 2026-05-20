package com.betanalyzer.domain.enums;

import lombok.Getter;

@Getter
public enum SupportedLeague {
    BRASILEIRAO("Série A", "BR", 90, "Conhecimento local, muitos dados"),
    PREMIER_LEAGUE("Premier League", "EN", 98, "Histórico rico, dados consistentes"),
    CHAMPIONS_LEAGUE("UEFA Champions League", "EU", 97, "Relevância, liquidez alta");

    private final String apiName;
    private final String country;
    private final int dataQualityScore;
    private final String reasoning;

    SupportedLeague(String apiName, String country, int dataQualityScore, String reasoning) {
        this.apiName = apiName;
        this.country = country;
        this.dataQualityScore = dataQualityScore;
        this.reasoning = reasoning;
    }

    public boolean isHighQuality(int threshold) {
        return this.dataQualityScore >= threshold;
    }
}
