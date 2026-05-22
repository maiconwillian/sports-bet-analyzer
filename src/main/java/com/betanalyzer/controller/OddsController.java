package com.betanalyzer.controller;

import com.betanalyzer.application.OddsHistoryService;
import com.betanalyzer.application.OddsService;
import com.betanalyzer.application.dto.CLVResponseDTO;
import com.betanalyzer.application.dto.OddsResponseDTO;
import com.betanalyzer.application.dto.request.OddsRequestDTO;
import com.betanalyzer.application.mapper.OddsMapper;
import com.betanalyzer.domain.model.Match;
import com.betanalyzer.domain.model.Odds;
import com.betanalyzer.infrastructure.persistence.MatchRepository;
import com.betanalyzer.shared.exception.MatchNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/odds")
@RequiredArgsConstructor
public class OddsController {

    private final OddsService oddsService;
    private final OddsHistoryService oddsHistoryService;
    private final OddsMapper oddsMapper;
    private final MatchRepository matchRepository;

    /**
     * Endpoint 1: Capturar Odds Reais (The Odds API)
     */
    @PostMapping("/capture/{matchId}")
    public ResponseEntity<List<OddsResponseDTO>> captureOdds(@PathVariable UUID matchId, 
                                                            @RequestParam(defaultValue = "soccer_brazil") String sport) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException("Match not found: " + matchId));
        
        List<Odds> captured = oddsHistoryService.captureAndSaveOdds(match, sport);
        return ResponseEntity.status(HttpStatus.CREATED).body(oddsMapper.toResponseDTOList(captured));
    }

    /**
     * Endpoint 2: Listar Histórico de Odds
     */
    @GetMapping("/history/{matchId}")
    public ResponseEntity<List<OddsResponseDTO>> getOddsHistory(@PathVariable UUID matchId) {
        List<Odds> history = oddsHistoryService.getOddsHistory(matchId);
        return ResponseEntity.ok(oddsMapper.toResponseDTOList(history));
    }

    /**
     * Endpoint 3: Calcular CLV (Closing Line Value)
     */
    @GetMapping("/{matchId}/clv")
    public ResponseEntity<CLVResponseDTO> calculateCLV(@PathVariable UUID matchId, 
                                                       @RequestParam UUID pickedOddId, 
                                                       @RequestParam UUID finalOddId) {
        List<Odds> history = oddsHistoryService.getOddsHistory(matchId);
        
        Odds picked = history.stream()
                .filter(o -> o.getId().equals(pickedOddId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Picked odd not found"));

        Odds finalOdd = history.stream()
                .filter(o -> o.getId().equals(finalOddId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Final odd not found"));

        BigDecimal clv = oddsHistoryService.calculateCLV(picked, finalOdd);
        
        CLVResponseDTO response = new CLVResponseDTO(
            matchId,
            picked.getOddsValue(),
            finalOdd.getOddsValue(),
            clv,
            clv.compareTo(BigDecimal.ZERO) > 0 ? "Positivo" : "Negativo"
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint 4: Listar Odds por Match (ANTIGO - Compatibilidade)
     */
    @GetMapping("/match/{matchId}")
    public ResponseEntity<List<OddsResponseDTO>> getOddsByMatch(@PathVariable UUID matchId) {
        return ResponseEntity.ok(oddsService.getOddsByMatch(matchId));
    }

    /**
     * Endpoint 5: Última Odd de um Match (ANTIGO - Compatibilidade)
     */
    @GetMapping("/match/{matchId}/latest")
    public ResponseEntity<OddsResponseDTO> getLatestOddsByMatch(@PathVariable UUID matchId) {
        return ResponseEntity.ok(oddsService.getLatestOddsByMatch(matchId));
    }

    /**
     * Endpoint 6: Listar por Bookmaker (ANTIGO - Compatibilidade)
     */
    @GetMapping("/bookmaker/{bookmaker}")
    public ResponseEntity<List<OddsResponseDTO>> getOddsByBookmaker(@PathVariable String bookmaker) {
        return ResponseEntity.ok(oddsService.getOddsByBookmaker(bookmaker));
    }

    /**
     * Salvar Odds Manuais (Compatibilidade)
     */
    @PostMapping
    public ResponseEntity<OddsResponseDTO> saveOdds(@RequestBody @Valid OddsRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(oddsService.saveOdds(request.matchId(), request));
    }
}
