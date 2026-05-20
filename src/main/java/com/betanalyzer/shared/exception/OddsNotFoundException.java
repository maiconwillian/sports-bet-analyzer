package com.betanalyzer.shared.exception;

public class OddsNotFoundException extends RuntimeException {
    public OddsNotFoundException(String message) {
        super(message);
    }
}