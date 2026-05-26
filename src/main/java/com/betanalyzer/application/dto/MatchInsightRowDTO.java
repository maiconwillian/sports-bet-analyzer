package com.betanalyzer.application.dto;

import com.betanalyzer.domain.enums.MatchStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record MatchInsightRowDTO(
        UUID matchId,
        String homeTeamName,
        String awayTeamName,
        String leagueName,
        String leagueCountry,
        LocalDateTime matchDate,
        MatchStatus status,
        boolean statsReady,
        boolean hasOdds,
        Double confidence,
        Double expectedValue,
        BigDecimal referenceOdd,
        String bookmaker,
        boolean passesEvFilters,
        String signalTier
) {}
