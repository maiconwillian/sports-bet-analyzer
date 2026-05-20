package com.betanalyzer.application.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class OddsResponseDTO {
    private UUID id;
    private String bookmaker;
    private BigDecimal homeWinOdd;
    private BigDecimal drawOdd;
    private BigDecimal awayWinOdd;
    private LocalDateTime capturedAt;
}
