package com.betanalyzer.shared.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MarketUtilsTest {

    @Test
    void recognizesOver25Variants() {
        assertThat(MarketUtils.isOver25Market("OVER_2_5")).isTrue();
        assertThat(MarketUtils.isOver25Market("Over 2.5 Goals")).isTrue();
        assertThat(MarketUtils.isOver25Market("BTTS")).isFalse();
    }
}
