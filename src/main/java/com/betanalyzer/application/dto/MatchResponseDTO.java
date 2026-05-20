package com.betanalyzer.application.dto;

import com.betanalyzer.domain.enums.MatchStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MatchResponseDTO {
    private UUID id;
    private Long apiId;
    private String homeTeamName;
    private String awayTeamName;
    private Integer homeGoals;
    private Integer awayGoals;
    private LocalDateTime matchDate;
    private String leagueName;
    private MatchStatus status;
}
