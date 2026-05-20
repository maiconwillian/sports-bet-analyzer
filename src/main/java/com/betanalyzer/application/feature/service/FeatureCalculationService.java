package com.betanalyzer.application.feature.service;

import com.betanalyzer.application.dto.MatchFeatureContextDTO;
import com.betanalyzer.application.feature.extractor.Over25FeatureExtractor;
import com.betanalyzer.domain.model.Match;
import com.betanalyzer.infrastructure.persistence.MatchStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureCalculationService {
    
    private final MatchStatsRepository matchStatsRepository;
    private final Over25FeatureExtractor over25FeatureExtractor;
    
    @Transactional(readOnly = true)
    public MatchFeatureContextDTO calculateOver25Features(Match match) {
        log.info("Calculating Over 2.5 features for match: {} vs {}", 
                match.getHomeTeam().getName(), 
                match.getAwayTeam().getName());
        
        var stats = matchStatsRepository.findByMatchId(match.getId())
                .orElseThrow(() -> new RuntimeException("Stats not found for match: " + match.getId()));
        
        return over25FeatureExtractor.extractOver25Features(match, stats);
    }
}
