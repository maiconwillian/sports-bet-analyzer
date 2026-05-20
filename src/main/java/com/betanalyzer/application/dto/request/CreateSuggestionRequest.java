package com.betanalyzer.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateSuggestionRequest(
    @NotNull(message = "Match ID is required")
    UUID matchId,
    
    @NotBlank(message = "Market is required")
    String market,
    
    @NotNull(message = "Picked odd is required")
    BigDecimal pickedOdd,
    
    @NotBlank(message = "Picked bookmaker is required")
    String pickedBookmaker,
    
    @NotNull(message = "Confidence is required")
    Double confidence,
    
    @NotNull(message = "Expected value is required")
    Double expectedValue,
    
    BigDecimal stake
) {
    public CreateSuggestionRequest {
        if (stake == null) {
            stake = new BigDecimal("100.00");
        }
    }
}