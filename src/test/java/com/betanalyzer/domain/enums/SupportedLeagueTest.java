package com.betanalyzer.domain.enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SupportedLeagueTest {

    @Test
    void testBrasileiraoPremierChampions() {
        assertNotNull(SupportedLeague.BRASILEIRAO);
        assertNotNull(SupportedLeague.PREMIER_LEAGUE);
        assertNotNull(SupportedLeague.CHAMPIONS_LEAGUE);
        
        assertEquals("Série A", SupportedLeague.BRASILEIRAO.getApiName());
        assertEquals("Premier League", SupportedLeague.PREMIER_LEAGUE.getApiName());
        assertEquals("UEFA Champions League", SupportedLeague.CHAMPIONS_LEAGUE.getApiName());
    }

    @Test
    void testDataQualityScores() {
        assertEquals(90, SupportedLeague.BRASILEIRAO.getDataQualityScore());
        assertEquals(98, SupportedLeague.PREMIER_LEAGUE.getDataQualityScore());
        assertEquals(97, SupportedLeague.CHAMPIONS_LEAGUE.getDataQualityScore());
    }

    @Test
    void testIsHighQuality() {
        assertTrue(SupportedLeague.BRASILEIRAO.isHighQuality(85));
        assertTrue(SupportedLeague.PREMIER_LEAGUE.isHighQuality(95));
        assertFalse(SupportedLeague.BRASILEIRAO.isHighQuality(95));
    }
}
