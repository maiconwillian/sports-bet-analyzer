package com.betanalyzer.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CLVResponseDTO(
    UUID matchId,
    BigDecimal pickedOdd,
    BigDecimal finalOdd,
    BigDecimal clvPercentage,
    String analysis
) {}
