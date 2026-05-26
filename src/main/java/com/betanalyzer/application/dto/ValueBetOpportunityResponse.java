package com.betanalyzer.application.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ValueBetOpportunityResponse {
    private UUID matchId;
    private String homeTeamName;
    private String awayTeamName;
    private String leagueName;
    private String leagueCountry;
    private String market;
    private String bookmaker;
    private BigDecimal odd;
    private Double modelProbability;
    private Double confidence;
    private Double expectedValue;
    private Double kellyFraction;
    private BigDecimal suggestedStake;
    private String strategyName;
    private String reasoning;
    private LocalDateTime matchDate;
}
