package com.betanalyzer.domain.enums;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public enum SupportedLeague {
    CHAMPIONS_LEAGUE(
        "UEFA Champions League",
        "World",
        97,
        "Apenas times elite + mercado gigantesco = MELHOR ROI",
        "soccer_uefa_champs_league"
    ),

    PREMIER_LEAGUE(
        "Premier League",
        "England",
        98,
        "Histórico rico, dados 100% confiáveis, menos variância",
        "soccer_epl"
    ),

    LA_LIGA(
        "La Liga",
        "Spain",
        95,
        "Padrões previsíveis, bom histórico",
        "soccer_spain_la_liga"
    ),

    BUNDESLIGA(
        "Bundesliga",
        "Germany",
        96,
        "Alta média de gols — ideal para Over 2.5, dados e odds líquidas",
        "soccer_germany_bundesliga"
    ),

    SERIE_A(
        "Serie A",
        "Italy",
        95,
        "Top 5 europeia, boa cobertura de odds, estilo mais tático porém previsível",
        "soccer_italy_serie_a"
    ),

    BRASILEIRAO(
        "Serie A",
        "Brazil",
        90,
        "Conhecimento local, mas mais ruído estatístico",
        "soccer_brazil_campeonato"
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

    public static Optional<SupportedLeague> findByApiName(String name) {
        return Arrays.stream(SupportedLeague.values())
            .filter(l -> l.getApiName().equalsIgnoreCase(name))
            .findFirst();
    }

    public static Optional<SupportedLeague> findByLeague(String name, String country) {
        return Arrays.stream(SupportedLeague.values())
            .filter(l -> l.matches(name, country))
            .findFirst();
    }

    public static Optional<SupportedLeague> fromEnumName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(SupportedLeague.valueOf(name.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * @deprecated Removido junto com o LeagueApiIdMapping.
     */
    @Deprecated(since = "2.1", forRemoval = true)
    public static SupportedLeague fromApiFootballId(Long id) {
        return null;
    }

    public boolean matches(String leagueName, String leagueCountry) {
        if (leagueName == null || !apiName.equalsIgnoreCase(leagueName.trim())) {
            return false;
        }
        return matchesCountry(leagueCountry);
    }

    public boolean matchesCountry(String leagueCountry) {
        if (leagueCountry == null || leagueCountry.isBlank()) {
            return false;
        }
        String normalized = leagueCountry.trim();
        if (normalized.equalsIgnoreCase(country)) {
            return true;
        }
        return countryAliases().stream().anyMatch(alias -> alias.equalsIgnoreCase(normalized));
    }

    private Set<String> countryAliases() {
        return switch (this) {
            case PREMIER_LEAGUE -> Set.of("England", "EN", "UK", "United Kingdom");
            case CHAMPIONS_LEAGUE -> Set.of("World", "Europe", "EU", "International");
            case LA_LIGA -> Set.of("Spain", "ES");
            case BUNDESLIGA -> Set.of("Germany", "DE");
            case SERIE_A -> Set.of("Italy", "IT");
            case BRASILEIRAO -> Set.of("Brazil", "BR");
        };
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
