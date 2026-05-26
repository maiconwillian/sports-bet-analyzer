package com.betanalyzer.domain.enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SupportedLeagueTest {

    @Test
    void testBrasileiraoPremierChampions() {
        assertNotNull(SupportedLeague.BRASILEIRAO);
        assertNotNull(SupportedLeague.PREMIER_LEAGUE);
        assertNotNull(SupportedLeague.CHAMPIONS_LEAGUE);

        assertEquals("Serie A", SupportedLeague.BRASILEIRAO.getApiName());
        assertEquals("Premier League", SupportedLeague.PREMIER_LEAGUE.getApiName());
        assertEquals("UEFA Champions League", SupportedLeague.CHAMPIONS_LEAGUE.getApiName());
    }

    @Test
    void testDataQualityScores() {
        assertEquals(90, SupportedLeague.BRASILEIRAO.getDataQualityScore());
        assertEquals(98, SupportedLeague.PREMIER_LEAGUE.getDataQualityScore());
        assertEquals(97, SupportedLeague.CHAMPIONS_LEAGUE.getDataQualityScore());
        assertEquals(96, SupportedLeague.BUNDESLIGA.getDataQualityScore());
        assertEquals(95, SupportedLeague.SERIE_A.getDataQualityScore());
    }

    @Test
    void testSportKeys() {
        assertEquals("soccer_uefa_champs_league", SupportedLeague.CHAMPIONS_LEAGUE.getTheOddsSportKey());
        assertEquals("soccer_epl", SupportedLeague.PREMIER_LEAGUE.getTheOddsSportKey());
        assertEquals("soccer_spain_la_liga", SupportedLeague.LA_LIGA.getTheOddsSportKey());
        assertEquals("soccer_brazil_campeonato", SupportedLeague.BRASILEIRAO.getTheOddsSportKey());
        assertEquals("soccer_germany_bundesliga", SupportedLeague.BUNDESLIGA.getTheOddsSportKey());
        assertEquals("soccer_italy_serie_a", SupportedLeague.SERIE_A.getTheOddsSportKey());
    }

    @Test
    void shouldDistinguishSerieAItalyFromBrazil() {
        assertTrue(SupportedLeague.SERIE_A.matches("Serie A", "Italy"));
        assertTrue(SupportedLeague.BRASILEIRAO.matches("Serie A", "Brazil"));
        assertFalse(SupportedLeague.SERIE_A.matches("Serie A", "Brazil"));
        assertFalse(SupportedLeague.BRASILEIRAO.matches("Serie A", "Italy"));
    }
}
