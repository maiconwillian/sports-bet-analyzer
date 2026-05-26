package com.betanalyzer.controller;

import com.betanalyzer.application.BetSuggestionService;
import com.betanalyzer.application.dto.SettlePendingResultDTO;
import com.betanalyzer.application.dto.SuggestionResponseDTO;
import com.betanalyzer.application.dto.request.CreateSuggestionRequest;
import com.betanalyzer.application.dto.request.SuggestionResultRequest;
import com.betanalyzer.domain.enums.SuggestionStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/suggestions")
@RequiredArgsConstructor
public class SuggestionController {

    private final BetSuggestionService suggestionService;

    @GetMapping
    public ResponseEntity<Page<SuggestionResponseDTO>> getSuggestions(
            @RequestParam(required = false) SuggestionStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Pageable pageable) {
        return ResponseEntity.ok(suggestionService.getSuggestions(status, date, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuggestionResponseDTO> getSuggestionById(@PathVariable UUID id) {
        return ResponseEntity.ok(suggestionService.getSuggestionById(id));
    }

    @PostMapping
    public ResponseEntity<SuggestionResponseDTO> createSuggestion(@RequestBody @Valid CreateSuggestionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(suggestionService.createSuggestion(request));
    }

    @PutMapping("/{id}/result")
    public ResponseEntity<SuggestionResponseDTO> updateResult(
            @PathVariable UUID id,
            @RequestBody @Valid SuggestionResultRequest request) {
        return ResponseEntity.ok(suggestionService.updateSuggestionResult(id, request));
    }

    @GetMapping("/match/{matchId}")
    public ResponseEntity<List<SuggestionResponseDTO>> getSuggestionsByMatch(@PathVariable UUID matchId) {
        return ResponseEntity.ok(suggestionService.getSuggestionsByMatch(matchId));
    }

    @PostMapping("/settle-pending")
    public ResponseEntity<SettlePendingResultDTO> settlePending() {
        return ResponseEntity.ok(suggestionService.settlePendingSuggestions());
    }
}
