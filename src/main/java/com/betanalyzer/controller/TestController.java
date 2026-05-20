package com.betanalyzer.controller;

import com.betanalyzer.infrastructure.client.ApiFootballClient;
import com.betanalyzer.infrastructure.client.dto.FixtureDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {
    
    private final ApiFootballClient apiFootballClient;
    
    /**
     * Endpoint temporário para testar API-Football
     * GET /api/test/fixtures?date=2026-05-25
     */
    @GetMapping("/fixtures")
    public ResponseEntity<List<FixtureDTO>> testFixtures(
            @RequestParam LocalDate date) {
        
        List<FixtureDTO> fixtures = apiFootballClient.getFixturesByDate(date);
        return ResponseEntity.ok(fixtures);
    }
}
