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

    private Double homeTeamGoalsAvg;
    private Double awayTeamGoalsAvg;

    @Column(columnDefinition = "TEXT")
    private String headToHead;

    private LocalDateTime lastUpdate;
}
