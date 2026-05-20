package com.betanalyzer.application.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record MatchStatsRequestDTO(
    @NotNull(message = "Match ID is required")
    UUID matchId,
    
    String homeTeamForm,
    String awayTeamForm,
    Double homeTeamGoalsAvg,
    Double awayTeamGoalsAvg,
    String headToHead
) {}