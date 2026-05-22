package com.betanalyzer.application.dto.request;

import com.betanalyzer.domain.enums.MatchStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateMatchRequest(
    @NotNull(message = "Status is required")
    MatchStatus status,
    Integer homeGoals,
    Integer awayGoals,
    String homeTeam,
    String awayTeam
) {}