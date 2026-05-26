package com.betanalyzer.application.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record MatchStatsResponseDTO(
        UUID matchId,
        String homeTeamForm,
        String awayTeamForm,
        Double homeTeamGoalsAvg,
        Double awayTeamGoalsAvg,
        Double homeTeamGoalsConcededAvg,
        Double awayTeamGoalsConcededAvg,
        Double homeOver25Rate,
        Double awayOver25Rate,
        Integer homeLeaguePosition,
        Integer awayLeaguePosition,
        String headToHead,
        LocalDateTime lastUpdate,
        boolean statsEnriched
) {}
