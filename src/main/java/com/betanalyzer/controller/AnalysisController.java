package com.betanalyzer.controller;

import com.betanalyzer.application.analysis.ValueBetDetectionService;
import com.betanalyzer.application.dto.ValueBetOpportunityResponse;
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

    @GetMapping("/value-bets")
    public ResponseEntity<List<ValueBetOpportunityResponse>> getValueBets(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) SupportedLeague league) {
        return ResponseEntity.ok(valueBetDetectionService.scanValueBets(date, league));
    }

    @GetMapping("/value-bets/match/{matchId}")
    public ResponseEntity<ValueBetOpportunityResponse> getValueBetForMatch(@PathVariable UUID matchId) {
        return valueBetDetectionService.scanMatch(matchId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
