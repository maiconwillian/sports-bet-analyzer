package com.betanalyzer.application.feature.extractor;

import com.betanalyzer.application.dto.MatchFeatureContextDTO;
import com.betanalyzer.domain.model.Match;
import com.betanalyzer.domain.model.MatchStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class Over25FeatureExtractor {
    
    /**
     * Extrai features para Over 2.5 de um jogo
     * Baseado em: últimas 5 performances + head-to-head
     */
    public MatchFeatureContextDTO extractOver25Features(Match match, MatchStats stats) {
        
        // Usar dados de MatchStats para calcular features
        double homeAvgGoalsScored = stats.getHomeTeamGoalsAvg() != null ? stats.getHomeTeamGoalsAvg() : 0.0;
        double awayAvgGoalsConceded = stats.getAwayTeamGoalsAvg() != null ? stats.getAwayTeamGoalsAvg() : 0.0;
        
        // (FUTURO) Vão vir de um repository: últimos 5 jogos
        // Por enquanto use MatchStats como proxy
        
        double combinedGoalAverage = (homeAvgGoalsScored + awayAvgGoalsConceded) / 2;
        double expectedGoalPressure = homeAvgGoalsScored + awayAvgGoalsConceded;
        
        // Confidence inicial: quanto maior expected, mais confiante
        double confidence = Math.min(expectedGoalPressure * 25, 95); // 0-95%
        
        return MatchFeatureContextDTO.builder()
                .homeTeamName(match.getHomeTeam().getName())
                .awayTeamName(match.getAwayTeam().getName())
                .leagueName(match.getLeague().getName())
                .matchDate(match.getMatchDate())
                
                .homeAvgGoalsScored(homeAvgGoalsScored)
                .awayAvgGoalsConceded(awayAvgGoalsConceded)
                
                .combinedGoalAverage(combinedGoalAverage)
                .expectedGoalPressure(expectedGoalPressure)
                .confidence(confidence)
                .reasoning("Expected goals: " + String.format("%.2f", expectedGoalPressure))
                
                .build();
    }
}
