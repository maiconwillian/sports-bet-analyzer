package com.betanalyzer.controller;

import com.betanalyzer.application.MatchService;
import com.betanalyzer.application.dto.MatchResponseDTO;
import com.betanalyzer.application.dto.request.CreateMatchRequest;
import com.betanalyzer.application.dto.request.UpdateMatchRequest;
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
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @GetMapping
    public ResponseEntity<Page<MatchResponseDTO>> getAllMatches(Pageable pageable) {
        return ResponseEntity.ok(matchService.getAllMatches(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchResponseDTO> getMatchById(@PathVariable UUID id) {
        return ResponseEntity.ok(matchService.getMatchById(id));
    }

    @PostMapping
    public ResponseEntity<MatchResponseDTO> createMatch(@RequestBody @Valid CreateMatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(matchService.createMatch(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MatchResponseDTO> updateMatch(@PathVariable UUID id, @RequestBody @Valid UpdateMatchRequest request) {
        return ResponseEntity.ok(matchService.updateMatch(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMatch(@PathVariable UUID id) {
        matchService.deleteMatch(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<MatchResponseDTO>> getMatchesByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(matchService.getMatchesByDate(date));
    }

    @GetMapping("/league/{league}")
    public ResponseEntity<List<MatchResponseDTO>> getMatchesByLeague(@PathVariable String league) {
        return ResponseEntity.ok(matchService.getMatchesByLeague(league));
    }
}
