package com.betanalyzer.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CreateMatchRequest(
    @NotBlank(message = "Home team is required")
    String homeTeam,
    
    @NotBlank(message = "Away team is required")
    String awayTeam,
    
    @NotNull(message = "Match date is required")
    LocalDateTime matchDate,
    
    @NotBlank(message = "League is required")
    String league
) {}