package com.betanalyzer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.value-bet")
public class ValueBetProperties {
    private double minEv = 0.05;
    private double minConfidence = 65.0;
    private double kellyFraction = 0.25;
    private double bankroll = 1000.0;
}
