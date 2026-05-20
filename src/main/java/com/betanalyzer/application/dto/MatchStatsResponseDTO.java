package com.betanalyzer.application.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class MatchStatsResponseDTO {
    private UUID id;
    private UUID matchId;
    private String homeTeamForm;
    private String awayTeamForm;
    private Double homeTeamGoalsAvg;
    private Double awayTeamGoalsAvg;
    private String headToHead;
}