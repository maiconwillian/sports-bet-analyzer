package com.betanalyzer.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "api.odds")
@Validated
@Data
public class TheOddsApiProperties {

    @NotBlank(message = "API base URL cannot be blank")
    private String baseUrl;

    @NotBlank(message = "API key cannot be blank")
    private String key;

    @Positive(message = "Connect timeout must be positive")
    private long connectTimeoutMs = 5000;

    @Positive(message = "Read timeout must be positive")
    private long readTimeoutMs = 10000;

    /** Bookmaker regions (e.g. us, eu, uk). Comma-separated for multiple. */
    private String regions = "us,eu";
}
