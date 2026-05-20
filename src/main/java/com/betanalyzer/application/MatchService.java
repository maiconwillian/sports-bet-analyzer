package com.betanalyzer.application;

import com.betanalyzer.application.dto.MatchResponseDTO;
import com.betanalyzer.application.dto.request.CreateMatchRequest;
import com.betanalyzer.application.dto.request.UpdateMatchRequest;
import com.betanalyzer.application.mapper.MatchMapper;
import com.betanalyzer.domain.model.Match;
import com.betanalyzer.infrastructure.persistence.MatchRepository;
import com.betanalyzer.shared.exception.MatchNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
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
        Match match = matchMapper.toEntity(request);
        return matchMapper.toResponseDTO(matchRepository.save(match));
    }

    @Transactional
    public MatchResponseDTO updateMatch(UUID id, UpdateMatchRequest request) {
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new MatchNotFoundException("Match not found with id: " + id));
        matchMapper.updateEntityFromRequest(request, match);
        return matchMapper.toResponseDTO(matchRepository.save(match));
    }

    @Transactional
    public void deleteMatch(UUID id) {
        if (!matchRepository.existsById(id)) {
            throw new MatchNotFoundException("Match not found with id: " + id);
        }
        matchRepository.deleteById(id);
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
    public List<MatchResponseDTO> getMatchesByLeague(String league) {
        return matchRepository.findByLeague(league).stream()
                .map(matchMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}