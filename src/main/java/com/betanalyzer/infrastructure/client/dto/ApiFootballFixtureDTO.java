package com.betanalyzer.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiFootballFixtureDTO(
    @JsonProperty("fixture")
    FixtureDetails fixture,
    
    @JsonProperty("teams")
    TeamsDetails teams,
    
    @JsonProperty("league")
    LeagueDetails league,
    
    @JsonProperty("goals")
    GoalsDetails goals
) {
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FixtureDetails(
        Long id,
        LocalDateTime date,
        String referee,
        String timezone,
        
        @JsonProperty("venue")
        Venue venue,
        
        @JsonProperty("status")
        Status status
    ) {}
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Venue(
        Long id,
        String name,
        String city
    ) {}
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Status(
        String long_,
        String short_,
        String elapsed
    ) {}
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TeamsDetails(
        @JsonProperty("home")
        TeamInfo home,
        
        @JsonProperty("away")
        TeamInfo away
    ) {}
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TeamInfo(
        Long id,
        String name,
        String logo,
        Boolean winner
    ) {}
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LeagueDetails(
        Long id,
        String name,
        String country,
        String logo,
        Integer season,
        String round
    ) {}
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GoalsDetails(
        Integer home,
        Integer away
    ) {}
}
