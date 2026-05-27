package com.betanalyzer.application.dto;

import com.betanalyzer.domain.enums.SuggestionStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class SuggestionResponseDTO {
    private UUID id;
    private UUID matchId;
    private String homeTeamName;
    private String awayTeamName;
    private String leagueName;
    private String market;
    private BigDecimal pickedOdd;
    private String pickedBookmaker;
    private Double confidence;
    private Double expectedValue;
    private BigDecimal stake;
    private SuggestionStatus status;
    private Double roi;
    private String proposalReason;
}
