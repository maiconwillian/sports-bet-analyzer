package com.betanalyzer.application.dto.request;

import com.betanalyzer.domain.enums.SuggestionStatus;
import jakarta.validation.constraints.NotNull;

public record SuggestionResultRequest(
    @NotNull(message = "Actual result status is required")
    SuggestionStatus actualResult
) {}