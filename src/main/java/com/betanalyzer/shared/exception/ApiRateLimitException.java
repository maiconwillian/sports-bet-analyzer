package com.betanalyzer.shared.exception;

/**
 * Exception lançada quando API retorna 429 (Too Many Requests).
 * Indica que rate limit foi atingido.
 */
public class ApiRateLimitException extends RuntimeException {
    
    private final long retryAfterSeconds;
    
    public ApiRateLimitException(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }
    
    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
