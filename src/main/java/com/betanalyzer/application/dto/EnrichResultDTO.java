package com.betanalyzer.application.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record EnrichResultDTO(
        LocalDate date,
        int matchesProcessed,
        int enriched,
        int failed,
        List<String> errors,
        String message
) {}
