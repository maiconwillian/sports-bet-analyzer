package com.betanalyzer.application.dto;

import com.betanalyzer.domain.enums.MatchStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MatchResponseDTO {
    private UUID id;
    private String homeTeam;
    private String awayTeam;
    private LocalDateTime matchDate;
    private String league;
    private MatchStatus status;
}
