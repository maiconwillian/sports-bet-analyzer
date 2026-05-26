package com.betanalyzer.application.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record MatchAnalysisResponseDTO(
        UUID matchId,
        String homeTeamName,
        String awayTeamName,
        MatchStatsResponseDTO stats,
        ModelInsightDTO modelInsight,
        boolean statsEnriched
) {}
