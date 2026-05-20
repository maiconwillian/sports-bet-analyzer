package com.betanalyzer.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiFootballTeamStatsDTO(
    @JsonProperty("response")
    List<TeamStats> stats
) {
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TeamStats(
        @JsonProperty("team")
        TeamInfo team,
        
        @JsonProperty("statistics")
        List<StatItem> statistics
    ) {}
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TeamInfo(
        Long id,
        String name,
        String logo
    ) {}
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StatItem(
        String type,
        Object value
    ) {}
}
