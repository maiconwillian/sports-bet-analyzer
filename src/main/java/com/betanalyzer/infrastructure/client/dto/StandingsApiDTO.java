package com.betanalyzer.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StandingsApiDTO(
        @JsonProperty("response")
        List<LeagueStandings> response
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LeagueStandings(
            @JsonProperty("league")
            LeagueInfo league,
            List<List<StandingRow>> standings
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LeagueInfo(
            Long id,
            String name,
            Integer season
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StandingRow(
            Integer rank,
            @JsonProperty("team")
            TeamRef team
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TeamRef(
            Long id,
            String name
    ) {}
}
