package com.betanalyzer.application.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record MatchInsightsResponseDTO(
        LocalDate date,
        List<MatchInsightRowDTO> insights,
        double minConfidence,
        double minEv,
        int matchesConsidered
) {}
