package com.betanalyzer.controller;

import com.betanalyzer.application.FixtureSyncService;
import com.betanalyzer.application.dto.SyncResult;
import com.betanalyzer.infrastructure.persistence.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/sync")
@RequiredArgsConstructor
public class AdminController {

    private final FixtureSyncService fixtureSyncService;
    private final MatchRepository matchRepository;

    @PostMapping("/fixtures")
    public ResponseEntity<SyncResult> syncFixtures(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(fixtureSyncService.syncFixturesByDate(date));
    }

    @PostMapping("/fixtures/range")
    public ResponseEntity<SyncResult> syncFixturesRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(fixtureSyncService.syncFixturesByDateRange(from, to));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSyncStatus() {
        long totalMatches = matchRepository.count();
        // Em um cenário real, poderíamos buscar o último sync de uma tabela de logs ou cache
        return ResponseEntity.ok(Map.of(
                "totalMatchesSynced", totalMatches,
                "lastSyncCheck", LocalDateTime.now()
        ));
    }
}