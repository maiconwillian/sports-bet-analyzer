package com.betanalyzer.shared.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
    String error,
    String message,
    LocalDateTime timestamp,
    int status
) {}
