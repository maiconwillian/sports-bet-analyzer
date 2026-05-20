package com.betanalyzer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Component
@ConfigurationProperties(prefix = "api.football")
@Validated
@Data
public class ApiFootballProperties {
    
    @NotBlank(message = "API base URL cannot be blank")
    private String baseUrl;
    
    @NotBlank(message = "API key cannot be blank")
    private String key;
    
    @NotBlank(message = "API host cannot be blank")
    private String host;
    
    @Positive(message = "Connect timeout must be positive")
    private long connectTimeoutMs = 5000;
    
    @Positive(message = "Read timeout must be positive")
    private long readTimeoutMs = 10000;
}
