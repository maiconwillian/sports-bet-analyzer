package com.betanalyzer.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "match_stats")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchStats {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    private String homeTeamForm;
    private String awayTeamForm;

    /** Média de gols marcados (mandante). */
    @Column(name = "home_team_goals_avg")
    private Double homeTeamGoalsAvg;

    /** Média de gols marcados (visitante). */
    @Column(name = "away_team_goals_avg")
    private Double awayTeamGoalsAvg;

    @Column(name = "home_team_goals_conceded_avg")
    private Double homeTeamGoalsConcededAvg;

    @Column(name = "away_team_goals_conceded_avg")
    private Double awayTeamGoalsConcededAvg;

    @Column(name = "home_over25_rate")
    private Double homeOver25Rate;

    @Column(name = "away_over25_rate")
    private Double awayOver25Rate;

    @Column(name = "home_league_position")
    private Integer homeLeaguePosition;

    @Column(name = "away_league_position")
    private Integer awayLeaguePosition;

    @Column(columnDefinition = "TEXT")
    private String headToHead;

    private LocalDateTime lastUpdate;

    @Column(name = "stats_enriched", nullable = false)
    @Builder.Default
    private Boolean statsEnriched = false;

    public boolean hasUsableEnrichment() {
        return Boolean.TRUE.equals(statsEnriched)
                && homeTeamGoalsAvg != null && homeTeamGoalsAvg > 0
                && awayTeamGoalsAvg != null && awayTeamGoalsAvg > 0;
    }
}
