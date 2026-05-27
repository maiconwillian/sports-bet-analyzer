package com.betanalyzer.controller;

import com.betanalyzer.application.BetSuggestionService;
import com.betanalyzer.application.WeeklyProposalService;
import com.betanalyzer.application.dto.GenerateWeeklyResultDTO;
import com.betanalyzer.application.dto.request.AcceptProposalRequest;
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
    private final WeeklyProposalService weeklyProposalService;

    @GetMapping("/proposed")
    public ResponseEntity<List<SuggestionResponseDTO>> getProposed(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(weeklyProposalService.getProposedSuggestions(from, to));
    }

    @PostMapping("/generate-weekly")
    public ResponseEntity<GenerateWeeklyResultDTO> generateWeekly(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(weeklyProposalService.generateWeeklyProposals(from, to));
    }

    @PostMapping("/proposed/{id}/accept")
    public ResponseEntity<SuggestionResponseDTO> acceptProposal(
            @PathVariable UUID id,
            @RequestBody(required = false) @Valid AcceptProposalRequest request) {
        return ResponseEntity.ok(weeklyProposalService.acceptProposal(id, request));
    }

    @PostMapping("/proposed/{id}/reject")
    public ResponseEntity<SuggestionResponseDTO> rejectProposal(@PathVariable UUID id) {
        return ResponseEntity.ok(weeklyProposalService.rejectProposal(id));
    }

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
