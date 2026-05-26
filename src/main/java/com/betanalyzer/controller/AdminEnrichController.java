package com.betanalyzer.controller;

import com.betanalyzer.application.EnrichMatchAnalysisService;
import com.betanalyzer.application.dto.EnrichResultDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/enrich")
@RequiredArgsConstructor
public class AdminEnrichController {

    private final EnrichMatchAnalysisService enrichMatchAnalysisService;

    @PostMapping("/fixtures")
    public ResponseEntity<EnrichResultDTO> enrichFixturesByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(enrichMatchAnalysisService.enrichMatchesForDate(date));
    }

    @PostMapping("/fixtures/range")
    public ResponseEntity<EnrichResultDTO> enrichFixturesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(enrichMatchAnalysisService.enrichMatchesForDateRange(from, to));
    }
}
