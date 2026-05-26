package com.betanalyzer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.backtest")
public class BacktestProperties {
    private int minSampleBets = 30;
}
