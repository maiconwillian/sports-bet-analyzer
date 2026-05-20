package com.betanalyzer.shared.exception;

/**
 * Exception lançada quando há erro na comunicação com API externa.
 * Não é retryable automaticamente.
 */
public class ApiClientException extends RuntimeException {
    
    private final int statusCode;
    private final String responseBody;
    
    public ApiClientException(String message) {
        super(message);
        this.statusCode = -1;
        this.responseBody = null;
    }
    
    public ApiClientException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
        this.responseBody = null;
    }
    
    public ApiClientException(String message, int statusCode, String responseBody) {
        super(String.format("%s [Status: %d]", message, statusCode));
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public String getResponseBody() {
        return responseBody;
    }
}
