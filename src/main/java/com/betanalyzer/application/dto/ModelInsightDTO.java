package com.betanalyzer.application.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ModelInsightDTO(
        String strategyName,
        String market,
        Double confidence,
        Double expectedValue,
        BigDecimal suggestedOdd,
        String bookmaker,
        String reasoning,
        boolean wouldPassValueBetFilters
) {}
