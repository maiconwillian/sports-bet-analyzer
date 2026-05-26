package com.betanalyzer.application.dto;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class SettlePendingResultDTO {
    @Builder.Default
    private int settled = 0;
    @Builder.Default
    private int won = 0;
    @Builder.Default
    private int lost = 0;
    @Builder.Default
    private int voided = 0;
    @Builder.Default
    private int skipped = 0;
    @Builder.Default
    private List<String> errors = new ArrayList<>();
}
