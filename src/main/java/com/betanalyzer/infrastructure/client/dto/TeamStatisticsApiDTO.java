package com.betanalyzer.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TeamStatisticsApiDTO(
        @JsonProperty("response")
        TeamStatisticsEntry response
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TeamStatisticsEntry(
            String form,
            Goals goals
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Goals(
            @JsonProperty("for")
            GoalSide forGoals,
            @JsonProperty("against")
            GoalSide against
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GoalSide(
            Average average
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Average(
            String total,
            String home,
            String away
    ) {}
}
