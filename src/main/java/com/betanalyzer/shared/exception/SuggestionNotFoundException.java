package com.betanalyzer.shared.exception;

public class SuggestionNotFoundException extends RuntimeException {
    public SuggestionNotFoundException(String message) {
        super(message);
    }
}