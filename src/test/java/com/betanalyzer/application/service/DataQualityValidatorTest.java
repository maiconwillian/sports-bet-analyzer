package com.betanalyzer.application.service;

import com.betanalyzer.domain.enums.SupportedLeague;
import com.betanalyzer.infrastructure.client.dto.FixtureDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DataQualityValidatorTest {

    private DataQualityValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DataQualityValidator();
        ReflectionTestUtils.setField(validator, "minimumQualityThreshold", 85);
        ReflectionTestUtils.setField(validator, "filterAmistosos", true);
        ReflectionTestUtils.setField(validator, "filterPreTemporada", true);
        ReflectionTestUtils.setField(validator, "filterEstaduais", true);
    }

    @Test
    void testShouldRejectAmistosos() {
        FixtureDTO fixture = createMockFixture("Club Friendlies");
        assertFalse(validator.isQualityFixture(fixture, SupportedLeague.PREMIER_LEAGUE));
    }

    @Test
    void testShouldRejectPreTemporada() {
        FixtureDTO fixture = createMockFixture("Cup of Traditions");
        assertFalse(validator.isQualityFixture(fixture, SupportedLeague.PREMIER_LEAGUE));
    }

    @Test
    void testShouldRejectObscureLeagues() {
        FixtureDTO fixture = createMockFixture("Paulista A1");
        assertFalse(validator.isQualityFixture(fixture, SupportedLeague.BRASILEIRAO));
    }

    @Test
    void testShouldAcceptPremierLeague() {
        FixtureDTO fixture = createMockFixture("Premier League");
        assertTrue(validator.isQualityFixture(fixture, SupportedLeague.PREMIER_LEAGUE));
    }

    @Test
    void testShouldAcceptBrasileirao() {
        FixtureDTO fixture = createMockFixture("Série A");
        assertTrue(validator.isQualityFixture(fixture, SupportedLeague.BRASILEIRAO));
    }

    @Test
    void testShouldAcceptEvenIfBelowThreshold() {
        // Agora o score de qualidade deve ser ignorado
        ReflectionTestUtils.setField(validator, "minimumQualityThreshold", 95);
        FixtureDTO fixture = createMockFixture("Série A");
        assertTrue(validator.isQualityFixture(fixture, SupportedLeague.BRASILEIRAO)); // Brasileirão is 90, but should pass now
    }

    private FixtureDTO createMockFixture(String leagueName) {
        FixtureDTO.LeagueInfo leagueInfo = new FixtureDTO.LeagueInfo(1L, leagueName, "BR", 2024);
        FixtureDTO.FixtureInfo fixtureInfo = new FixtureDTO.FixtureInfo(1L, "2024-05-20T20:00:00Z", null);
        return new FixtureDTO(fixtureInfo, null, leagueInfo, null);
    }
}
