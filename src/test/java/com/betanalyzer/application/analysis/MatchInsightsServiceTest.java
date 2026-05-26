package com.betanalyzer.application.analysis;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MatchInsightsServiceTest {

    @Test
    void resolveSignalTier_nearConfidenceWhenBelowEvThreshold() {
        assertThat(MatchInsightsService.resolveSignalTier(61.0, false, true)).isEqualTo("NEAR");
    }

    @Test
    void resolveSignalTier_evPlusWhenPassesFilters() {
        assertThat(MatchInsightsService.resolveSignalTier(70.0, true, true)).isEqualTo("EV_PLUS");
    }

    @Test
    void resolveSignalTier_noStatsWhenNotEnriched() {
        assertThat(MatchInsightsService.resolveSignalTier(0.0, false, false)).isEqualTo("NO_STATS");
    }

    @Test
    void resolveSignalTier_weakWhenLowConfidence() {
        assertThat(MatchInsightsService.resolveSignalTier(40.0, false, true)).isEqualTo("WEAK");
    }
}
