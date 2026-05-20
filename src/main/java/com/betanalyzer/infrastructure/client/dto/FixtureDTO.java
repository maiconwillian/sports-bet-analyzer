
package com.betanalyzer.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FixtureDTO(
        @JsonProperty("fixture")
        FixtureInfo fixture,

        @JsonProperty("teams")
        TeamsInfo teams,

        @JsonProperty("league")
        LeagueInfo league,

        @JsonProperty("goals")
        GoalsInfo goals
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FixtureInfo(
            Long id,
            String date,
            @JsonProperty("status")
            StatusInfo status
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StatusInfo(
            @JsonProperty("long")
            String longStatus,

            @JsonProperty("short")
            String shortStatus,

            String elapsed
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TeamsInfo(
            @JsonProperty("home")
            TeamInfo home,

            @JsonProperty("away")
            TeamInfo away
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TeamInfo(
            Long id,
            String name,
            String logo
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LeagueInfo(
            Long id,
            String name,
            String country,
            Integer season
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GoalsInfo(
            Integer home,
            Integer away
    ) {}
}