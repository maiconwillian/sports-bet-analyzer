package com.betanalyzer.shared.exception;

import lombok.Getter;

@Getter
public class DataQualityException extends RuntimeException {
    
    private final Reason reason;

    public DataQualityException(String message, Reason reason) {
        super(message);
        this.reason = reason;
    }

    public enum Reason {
        AMISTOSO,
        PRE_TEMPORADA,
        OBSCURE_LEAGUE,
        LOW_QUALITY_SCORE,
        INSUFFICIENT_HISTORICAL_DATA,
        NOT_SUPPORTED_LEAGUE
    }
}
