package com.betanalyzer.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateSuggestionRequest(
    @NotNull(message = "Match ID is required")
    UUID matchId,
    
    @NotBlank(message = "Market is required")
    String market,
    
    @NotNull(message = "Picked odd is required")
    @DecimalMin(value = "1.01", message = "Odd must be at least 1.01")
    BigDecimal pickedOdd,
    
    @NotBlank(message = "Picked bookmaker is required")
    String pickedBookmaker,
    
    @NotNull(message = "Confidence is required")
    @DecimalMin(value = "0.0", message = "Confidence must be non-negative")
    Double confidence,
    
    @NotNull(message = "Expected value is required")
    Double expectedValue,
    
    @Positive(message = "Stake must be positive")
    BigDecimal stake
) {
    public CreateSuggestionRequest {
        if (stake == null) {
            stake = new BigDecimal("100.00");
        }
    }
}