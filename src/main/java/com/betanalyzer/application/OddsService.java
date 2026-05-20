package com.betanalyzer.application;

import com.betanalyzer.application.dto.OddsResponseDTO;
import com.betanalyzer.application.dto.request.OddsRequestDTO;
import com.betanalyzer.application.mapper.OddsMapper;
import com.betanalyzer.domain.model.Match;
import com.betanalyzer.domain.model.Odds;
import com.betanalyzer.infrastructure.persistence.MatchRepository;
import com.betanalyzer.infrastructure.persistence.OddsRepository;
import com.betanalyzer.shared.exception.MatchNotFoundException;
import com.betanalyzer.shared.exception.OddsNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OddsService {

    private final OddsRepository oddsRepository;
    private final MatchRepository matchRepository;
    private final OddsMapper oddsMapper;

    @Transactional(readOnly = true)
    public List<OddsResponseDTO> getOddsByMatch(UUID matchId) {
        return oddsRepository.findByMatchId(matchId).stream()
                .map(oddsMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OddsResponseDTO getLatestOddsByMatch(UUID matchId) {
        return oddsRepository.findFirstByMatchIdOrderByCapturedAtDesc(matchId)
                .map(oddsMapper::toResponseDTO)
                .orElseThrow(() -> new OddsNotFoundException("No odds found for match: " + matchId));
    }

    @Transactional
    public OddsResponseDTO saveOdds(UUID matchId, OddsRequestDTO request) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException("Match not found with id: " + matchId));
        
        Odds odds = oddsMapper.toEntity(request);
        odds.setMatch(match);
        return oddsMapper.toResponseDTO(oddsRepository.save(odds));
    }

    @Transactional(readOnly = true)
    public List<OddsResponseDTO> getOddsByBookmaker(String bookmaker) {
        return oddsRepository.findByBookmaker(bookmaker).stream()
                .map(oddsMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<OddsResponseDTO> captureOddsSnapshot(UUID matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException("Match not found with id: " + matchId));
        
        // Em um cenário real, aqui chamaria um scraper ou API externa.
        // Como é CRUD básico, vamos simular buscando a última e salvando uma nova captura.
        Odds latest = oddsRepository.findFirstByMatchIdOrderByCapturedAtDesc(matchId)
                .orElse(null);
        
        if (latest != null) {
            Odds snapshot = Odds.builder()
                    .match(match)
                    .bookmaker(latest.getBookmaker())
                    .homeWinOdd(latest.getHomeWinOdd())
                    .drawOdd(latest.getDrawOdd())
                    .awayWinOdd(latest.getAwayWinOdd())
                    .capturedAt(LocalDateTime.now())
                    .build();
            oddsRepository.save(snapshot);
        }
        
        return getOddsByMatch(matchId);
    }

    @Transactional(readOnly = true)
    public List<OddsResponseDTO> getOddsHistory(UUID matchId) {
        return oddsRepository.findByMatchIdOrderByCapturedAtAsc(matchId).stream()
                .map(oddsMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}