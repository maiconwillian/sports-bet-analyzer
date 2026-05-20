package com.betanalyzer.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.UUID;

public record MatchStatsRequestDTO(
    @NotNull(message = "Match ID is required")
    UUID matchId,
    
    String homeTeamForm,
    String awayTeamForm,
    
    @PositiveOrZero(message = "Goals average must be non-negative")
    Double homeTeamGoalsAvg,
    
    @PositiveOrZero(message = "Goals average must be non-negative")
    Double awayTeamGoalsAvg,
    
    String headToHead
) {}