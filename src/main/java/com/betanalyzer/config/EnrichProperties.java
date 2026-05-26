package com.betanalyzer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.enrich")
public class EnrichProperties {
    private boolean autoAfterSync = false;
    private int lastFixturesForForm = 10;
}
