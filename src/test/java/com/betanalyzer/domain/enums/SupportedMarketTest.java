package com.betanalyzer.domain.enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SupportedMarketTest {

    @Test
    void testOver25Goals() {
        assertNotNull(SupportedMarket.OVER_2_5);
        assertEquals("Over 2.5 Goals", SupportedMarket.OVER_2_5.getDescription());
        assertTrue(SupportedMarket.OVER_2_5.getModelingScore() >= 90);
    }

    @Test
    void testBTTS() {
        assertNotNull(SupportedMarket.BTTS);
        assertEquals("Both Teams To Score", SupportedMarket.BTTS.getDescription());
        assertTrue(SupportedMarket.BTTS.getModelingScore() >= 90);
    }
}
