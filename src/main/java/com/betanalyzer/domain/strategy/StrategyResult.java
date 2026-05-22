package com.betanalyzer.domain.strategy;

import java.math.BigDecimal;

public record StrategyResult(
    String strategyName,
    String strategyVersion,
    boolean shouldBet,
    String market,
    Double confidence,
    Double expectedValue,
    BigDecimal recommendedStake,
    String reasoning
) {}
