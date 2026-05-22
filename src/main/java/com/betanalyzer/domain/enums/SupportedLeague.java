package com.betanalyzer.domain.enums;

public enum SupportedLeague {
    // PRIORITY 1: Champions League (MELHOR ROI)
    CHAMPIONS_LEAGUE(
        "UEFA Champions League", 
        "EU", 
        97, 
        "Apenas times elite + mercado gigantesco = MELHOR ROI",
        "soccer_uefa_champions_league"
    ),
    
    // PRIORITY 2: Premier League (MAIS CONSISTENTE)
    PREMIER_LEAGUE(
        "Premier League", 
        "EN", 
        98, 
        "Histórico rico, dados 100% confiáveis, menos variância",
        "soccer_epl"
    ),
    
    // PRIORITY 3: La Liga (BOM BALANCE)
    LA_LIGA(
        "La Liga", 
        "Spain",
        95, 
        "Padrões previsíveis, bom histórico",
        "soccer_spain_la_liga"
    ),
    
    // PRIORITY 4: Brasileirão (SECUNDÁRIO)
    BRASILEIRAO(
        "Serie A",
        "Brazil",
        90, 
        "Conhecimento local, mas mais ruído estatístico",
        "soccer_brazil_serie_a"
    );

    private final String apiName;
    private final String country;
    private final int dataQualityScore;
    private final String reasoning;
    private final String theOddsSportKey;

    SupportedLeague(String apiName, String country, int dataQualityScore, String reasoning, String theOddsSportKey) {
        this.apiName = apiName;
        this.country = country;
        this.dataQualityScore = dataQualityScore;
        this.reasoning = reasoning;
        this.theOddsSportKey = theOddsSportKey;
    }

    public static java.util.Optional<SupportedLeague> findByApiName(String name) {
        return java.util.Arrays.stream(SupportedLeague.values())
            .filter(l -> l.getApiName().equalsIgnoreCase(name))
            .findFirst();
    }

    /**
     * @deprecated Removido junto com o LeagueApiIdMapping.
     */
    @Deprecated(since = "2.1", forRemoval = true)
    public static SupportedLeague fromApiFootballId(Long id) {
        return null;
    }

    public String getApiName() {
        return apiName;
    }

    public String getCountry() {
        return country;
    }

    public int getDataQualityScore() {
        return dataQualityScore;
    }

    public String getTheOddsSportKey() {
        return theOddsSportKey;
    }

    public boolean isHighQuality(int threshold) {
        return this.dataQualityScore >= threshold;
    }
}
