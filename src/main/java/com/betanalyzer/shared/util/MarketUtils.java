package com.betanalyzer.shared.util;

public final class MarketUtils {

    private MarketUtils() {
    }

    public static boolean isOver25Market(String market) {
        if (market == null || market.isBlank()) {
            return false;
        }
        String normalized = market.toUpperCase().replace(" ", "").replace(".", "_");
        return normalized.contains("OVER_2_5")
                || normalized.contains("OVER25")
                || normalized.contains("OVER_2_5_GOALS");
    }
}
