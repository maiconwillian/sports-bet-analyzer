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
    void testSportKeys() {
        assertEquals("soccer_uefa_champions_league", SupportedLeague.CHAMPIONS_LEAGUE.getTheOddsSportKey());
        assertEquals("soccer_epl", SupportedLeague.PREMIER_LEAGUE.getTheOddsSportKey());
        assertEquals("soccer_la_liga", SupportedLeague.LA_LIGA.getTheOddsSportKey());
        assertEquals("soccer_brazil_serie_a", SupportedLeague.BRASILEIRAO.getTheOddsSportKey());
    }
}
