package com.betanalyzer.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OddsResponseDTO(
    UUID id,
    UUID matchId,
    String bookmaker,
    String bookmakerKey,
    String market,
    BigDecimal oddsValue,
    LocalDateTime capturedAt,
    LocalDateTime createdAt
) {}
