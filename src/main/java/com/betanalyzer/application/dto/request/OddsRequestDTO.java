package com.betanalyzer.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record OddsRequestDTO(
    @NotNull(message = "Match ID is required")
    UUID matchId,
    
    @NotBlank(message = "Bookmaker is required")
    String bookmaker,
    
    @NotNull(message = "Home win odd is required")
    @DecimalMin(value = "1.01", message = "Odd must be at least 1.01")
    BigDecimal homeWinOdd,
    
    @NotNull(message = "Draw odd is required")
    @DecimalMin(value = "1.01", message = "Odd must be at least 1.01")
    BigDecimal drawOdd,
    
    @NotNull(message = "Away win odd is required")
    @DecimalMin(value = "1.01", message = "Odd must be at least 1.01")
    BigDecimal awayWinOdd
) {}