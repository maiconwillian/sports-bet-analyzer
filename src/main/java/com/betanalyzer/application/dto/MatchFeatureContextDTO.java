package com.betanalyzer.application.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class MatchFeatureContextDTO {
    
    // Match Context
    private String homeTeamName;
    private String awayTeamName;
    private String leagueName;
    private LocalDateTime matchDate;
    
    // Home Team Stats (últimos 5 jogos)
    private Double homeAvgGoalsScored;     // Gols que marca
    private Double homeAvgGoalsConceded;   // Gols que leva
    private Double homeOver25Rate;         // % de jogos com Over 2.5
    private Double homeBttsRate;           // % BTTS
    private Double homeCleanSheetRate;     // % sem sofrer gols
    
    // Away Team Stats (últimos 5 jogos)
    private Double awayAvgGoalsScored;
    private Double awayAvgGoalsConceded;
    private Double awayOver25Rate;
    private Double awayBttsRate;
    private Double awayCleanSheetRate;
    
    // Combined Features (INTELIGÊNCIA DERIVADA)
    private Double combinedGoalAverage;    // (home_scored + away_conceded) / 2
    private Double expectedGoalPressure;   // expectativa de gols no jogo
    private Double homeAttackScore;        // quão forte é o ataque do time 1
    private Double awayDefensiveWeakness;  // quão fraco é a defesa do time 2
    
    // Confidence
    private Double confidence;              // 0-100
    private String reasoning;               // por que essa sugestão
}
