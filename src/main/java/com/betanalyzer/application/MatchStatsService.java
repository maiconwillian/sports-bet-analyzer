package com.betanalyzer.application;

import com.betanalyzer.application.dto.MatchStatsResponseDTO;
import com.betanalyzer.application.dto.request.MatchStatsRequestDTO;
import com.betanalyzer.application.mapper.MatchStatsMapper;
import com.betanalyzer.domain.model.Match;
import com.betanalyzer.domain.model.MatchStats;
import com.betanalyzer.infrastructure.persistence.MatchRepository;
import com.betanalyzer.infrastructure.persistence.MatchStatsRepository;
import com.betanalyzer.shared.exception.MatchNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchStatsService {

    private final MatchStatsRepository statsRepository;
    private final MatchRepository matchRepository;
    private final MatchStatsMapper statsMapper;

    @Transactional(readOnly = true)
    public MatchStatsResponseDTO getStatsByMatch(UUID matchId) {
        return statsRepository.findByMatchId(matchId)
                .map(statsMapper::toResponseDTO)
                .orElseThrow(() -> new MatchNotFoundException("Stats not found for match: " + matchId));
    }

    @Transactional
    public MatchStatsResponseDTO createStats(UUID matchId, MatchStatsRequestDTO request) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException("Match not found with id: " + matchId));
        
        MatchStats stats = statsMapper.toEntity(request);
        stats.setMatch(match);
        return statsMapper.toResponseDTO(statsRepository.save(stats));
    }

    @Transactional
    public MatchStatsResponseDTO updateStats(UUID matchId, MatchStatsRequestDTO request) {
        MatchStats stats = statsRepository.findByMatchId(matchId)
                .orElseThrow(() -> new MatchNotFoundException("Stats not found for match: " + matchId));
        
        statsMapper.updateEntityFromRequest(request, stats);
        return statsMapper.toResponseDTO(statsRepository.save(stats));
    }

    public Double calculateFormAverage(String form) {
        if (form == null || form.isEmpty()) {
            return 0.0;
        }
        
        double totalPoints = 0;
        for (char c : form.toCharArray()) {
            if (c == 'W') totalPoints += 3;
            else if (c == 'D') totalPoints += 1;
        }
        
        return totalPoints / form.length();
    }
}