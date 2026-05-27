package com.betanalyzer.controller;

import com.betanalyzer.application.analysis.MatchInsightsService;
import com.betanalyzer.application.analysis.PickRankingService;
import com.betanalyzer.application.analysis.ValueBetDetectionService;
import com.betanalyzer.application.dto.MatchInsightsResponseDTO;
import com.betanalyzer.application.dto.MatchPickDTO;
import com.betanalyzer.application.dto.RoundPicksResponseDTO;
import com.betanalyzer.application.dto.ValueBetOpportunityResponse;
import com.betanalyzer.application.dto.ValueBetsScanResponse;
import com.betanalyzer.domain.enums.SupportedLeague;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final ValueBetDetectionService valueBetDetectionService;
    private final MatchInsightsService matchInsightsService;
    private final PickRankingService pickRankingService;

    @GetMapping("/round-picks")
    public ResponseEntity<RoundPicksResponseDTO> getRoundPicks(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) SupportedLeague league) {
        return ResponseEntity.ok(pickRankingService.getRoundPicks(date, league));
    }

    @GetMapping("/match/{matchId}/picks")
    public ResponseEntity<MatchPickDTO> getMatchPicks(@PathVariable UUID matchId) {
        return ResponseEntity.ok(pickRankingService.getMatchPicks(matchId));
    }

    @GetMapping("/match-insights")
    public ResponseEntity<MatchInsightsResponseDTO> getMatchInsights(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) SupportedLeague league) {
        return ResponseEntity.ok(matchInsightsService.getMatchInsights(date, league));
    }

    @GetMapping("/value-bets")
    public ResponseEntity<ValueBetsScanResponse> getValueBets(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) SupportedLeague league) {
        return ResponseEntity.ok(valueBetDetectionService.scanValueBetsWithMeta(date, league));
    }

    @GetMapping("/value-bets/match/{matchId}")
    public ResponseEntity<ValueBetOpportunityResponse> getValueBetForMatch(@PathVariable UUID matchId) {
        return valueBetDetectionService.scanMatch(matchId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
