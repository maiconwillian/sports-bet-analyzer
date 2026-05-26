package com.betanalyzer.application.dto;

import lombok.Builder;

/**
 * Dados agregados da API-Football para um time antes de persistir em MatchStats.
 */
@Builder
public record TeamEnrichmentSnapshot(
        String form,
        double goalsScoredAvg,
        double goalsConcededAvg,
        double over25Rate,
        Integer leaguePosition
) {}
