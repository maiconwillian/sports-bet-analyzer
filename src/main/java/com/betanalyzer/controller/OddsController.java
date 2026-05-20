package com.betanalyzer.controller;

import com.betanalyzer.application.OddsService;
import com.betanalyzer.application.dto.OddsResponseDTO;
import com.betanalyzer.application.dto.request.OddsRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/odds")
@RequiredArgsConstructor
public class OddsController {

    private final OddsService oddsService;

    @GetMapping("/match/{matchId}")
    public ResponseEntity<List<OddsResponseDTO>> getOddsByMatch(@PathVariable UUID matchId) {
        return ResponseEntity.ok(oddsService.getOddsByMatch(matchId));
    }

    @GetMapping("/match/{matchId}/latest")
    public ResponseEntity<OddsResponseDTO> getLatestOddsByMatch(@PathVariable UUID matchId) {
        return ResponseEntity.ok(oddsService.getLatestOddsByMatch(matchId));
    }

    @PostMapping
    public ResponseEntity<OddsResponseDTO> saveOdds(@RequestBody @Valid OddsRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(oddsService.saveOdds(request.matchId(), request));
    }

    @GetMapping("/bookmaker/{bookmaker}")
    public ResponseEntity<List<OddsResponseDTO>> getOddsByBookmaker(@PathVariable String bookmaker) {
        return ResponseEntity.ok(oddsService.getOddsByBookmaker(bookmaker));
    }

    @PostMapping("/capture/{matchId}")
    public ResponseEntity<List<OddsResponseDTO>> captureOddsSnapshot(@PathVariable UUID matchId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(oddsService.captureOddsSnapshot(matchId));
    }

    @GetMapping("/history/{matchId}")
    public ResponseEntity<List<OddsResponseDTO>> getOddsHistory(@PathVariable UUID matchId) {
        return ResponseEntity.ok(oddsService.getOddsHistory(matchId));
    }
}
