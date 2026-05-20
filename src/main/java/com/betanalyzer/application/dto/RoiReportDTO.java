package com.betanalyzer.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoiReportDTO {
    private Double totalROI;
    private BigDecimal totalLucro;
    private Double winrate;
    private Long totalSuggestions;
    private Long wonCount;
    private Long lostCount;
    private Long voidCount;
    private String period;
}
