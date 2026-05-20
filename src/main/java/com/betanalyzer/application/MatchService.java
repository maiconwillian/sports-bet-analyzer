package com.betanalyzer.application;

import com.betanalyzer.application.dto.MatchResponseDTO;
import com.betanalyzer.application.dto.request.CreateMatchRequest;
import com.betanalyzer.application.dto.request.UpdateMatchRequest;
import com.betanalyzer.application.mapper.MatchMapper;
import com.betanalyzer.domain.model.Match;
import com.betanalyzer.infrastructure.persistence.BetSuggestionRepository;
import com.betanalyzer.infrastructure.persistence.MatchRepository;
import com.betanalyzer.infrastructure.persistence.MatchStatsRepository;
import com.betanalyzer.infrastructure.persistence.OddsRepository;
import com.betanalyzer.shared.exception.MatchNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchService {

    private static final Logger log = LoggerFactory.getLogger(MatchService.class);

    private final MatchRepository matchRepository;
    private final BetSuggestionRepository suggestionRepository;
    private final OddsRepository oddsRepository;
    private final MatchStatsRepository matchStatsRepository;
    private final MatchMapper matchMapper;

    @Transactional(readOnly = true)
    public Page<MatchResponseDTO> getAllMatches(Pageable pageable) {
        return matchRepository.findAll(pageable).map(matchMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public MatchResponseDTO getMatchById(UUID id) {
        return matchRepository.findById(id)
                .map(matchMapper::toResponseDTO)
                .orElseThrow(() -> new MatchNotFoundException("Match not found with id: " + id));
    }

    @Transactional
    public MatchResponseDTO createMatch(CreateMatchRequest request) {
        log.info("Creating match: {} vs {}", request.homeTeam(), request.awayTeam());
        Match match = matchMapper.toEntity(request);
        return matchMapper.toResponseDTO(matchRepository.save(match));
    }

    @Transactional
    public MatchResponseDTO updateMatch(UUID id, UpdateMatchRequest request) {
        log.info("Updating match status for id: {}", id);
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new MatchNotFoundException("Match not found with id: " + id));
        matchMapper.updateEntityFromRequest(request, match);
        return matchMapper.toResponseDTO(matchRepository.save(match));
    }

    @Transactional
    public void deleteMatch(UUID id) {
        log.info("Deleting match with id: {}", id);
        
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new MatchNotFoundException("Match not found with id: " + id));
        
        // Deleta em ordem (respeita FK constraints)
        // 1. Deletar sugestões
        suggestionRepository.deleteByMatchId(id);
        
        // 2. Deletar odds
        oddsRepository.deleteByMatchId(id);
        
        // 3. Deletar stats
        matchStatsRepository.deleteByMatchId(id);
        
        // 4. Agora pode deletar o match
        matchRepository.delete(match);
        
        log.info("Match {} deleted successfully with all dependencies", id);
    }

    @Transactional(readOnly = true)
    public List<MatchResponseDTO> getMatchesByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        return matchRepository.findByMatchDateBetween(startOfDay, endOfDay).stream()
                .map(matchMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MatchResponseDTO> getMatchesByLeague(String leagueName) {
        // Agora league é uma entidade, precisamos buscar por nome ou ID.
        // Como o método original recebia String, vamos assumir busca por nome na entidade League.
        // Simplificando: vamos remover esse método ou ajustar para usar LeagueRepository
        return matchRepository.findAll().stream()
                .filter(m -> m.getLeague().getName().equalsIgnoreCase(leagueName))
                .map(matchMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}