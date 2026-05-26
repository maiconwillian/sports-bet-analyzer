package com.betanalyzer.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SyncResult {
    private int created;
    private int updated;
    private int failed;
    private int skippedUnsupported;
    private int skippedQuality;
    private List<String> errors;
    private LocalDateTime syncedAt;
    private String message;
    private Integer settled;
    private Integer won;
    private Integer lost;
    private Integer voided;
    private Integer skippedSettlement;

    public int getTotalProcessed() {
        return created + updated;
    }
}
