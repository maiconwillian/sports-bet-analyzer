package com.betanalyzer.application.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record ValueBetsScanResponse(
        LocalDate date,
        List<ValueBetOpportunityResponse> opportunities,
        int matchesConsidered,
        int matchesWithEnrichedStats,
        boolean statsIncomplete,
        String hint
) {}
