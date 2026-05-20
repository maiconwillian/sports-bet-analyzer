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
    private List<String> errors;
    private LocalDateTime syncedAt;
}