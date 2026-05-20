package com.betanalyzer.shared.exception;

public class MatchNotFoundException extends RuntimeException {
    public MatchNotFoundException(String message) {
        super(message);
    }
}