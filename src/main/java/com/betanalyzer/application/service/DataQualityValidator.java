package com.betanalyzer.application.service;

import com.betanalyzer.domain.enums.SupportedLeague;
import com.betanalyzer.domain.model.Match;
import com.betanalyzer.infrastructure.client.dto.FixtureDTO;
import com.betanalyzer.shared.exception.DataQualityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class DataQualityValidator {

    @Value("${app.data-quality.minimum-threshold:85}")
    private int minimumQualityThreshold;

    @Value("${app.data-quality.filter-amistosos:true}")
    private boolean filterAmistosos;

    @Value("${app.data-quality.filter-pre-temporada:true}")
    private boolean filterPreTemporada;

    @Value("${app.data-quality.filter-estaduais:true}")
    private boolean filterEstaduais;

    private static final List<String> AMISTOSO_KEYWORDS = Arrays.asList("Friendly", "Amistoso", "Club Friendlies");
    private static final List<String> PRE_TEMPORADA_KEYWORDS = Arrays.asList("Pre-season", "Cup of Traditions", "Florida Cup");
    private static final List<String> ESTADUAL_KEYWORDS = Arrays.asList("Paulista", "Carioca", "Mineiro", "Gaucho", "Baiano", "Paranaense", "Catarinense");

    public boolean isQualityFixture(FixtureDTO fixture, SupportedLeague league) {
        try {
            validateLeague(league);
            validateDataQualityScore(league);
            validateNotFriendly(fixture);
            validateNotPreSeason(fixture);
            validateNotObscureOrStateLeague(fixture);
            validateHistoricalData(fixture);
            return true;
        } catch (DataQualityException e) {
            log.debug("Fixture {} filtered: {}", fixture.fixture().id(), e.getMessage());
            return false;
        }
    }

    public boolean isQualityFixture(Match match, SupportedLeague league) {
        // Overloaded method for Match entity if needed, though most filtering happens at DTO level
        try {
             validateLeague(league);
             validateDataQualityScore(league);
            // Additional checks on Match entity if needed
            return true;
        } catch (DataQualityException e) {
            log.debug("Match {} filtered: {}", match.getApiId(), e.getMessage());
            return false;
        }
    }

    private void validateLeague(SupportedLeague league) {
        if (league == null) {
            throw new DataQualityException("League is not supported", DataQualityException.Reason.NOT_SUPPORTED_LEAGUE);
        }
    }

    private void validateDataQualityScore(SupportedLeague league) {
        if (league.getDataQualityScore() < minimumQualityThreshold) {
            throw new DataQualityException(
                String.format("League %s has low quality score: %d (threshold: %d)", 
                    league.name(), league.getDataQualityScore(), minimumQualityThreshold),
                DataQualityException.Reason.LOW_QUALITY_SCORE
            );
        }
    }

    private void validateNotFriendly(FixtureDTO fixture) {
        if (filterAmistosos) {
            String leagueName = fixture.league().name();
            if (AMISTOSO_KEYWORDS.stream().anyMatch(leagueName::contains)) {
                throw new DataQualityException("Match is a friendly", DataQualityException.Reason.AMISTOSO);
            }
        }
    }

    private void validateNotPreSeason(FixtureDTO fixture) {
        if (filterPreTemporada) {
            String leagueName = fixture.league().name();
            if (PRE_TEMPORADA_KEYWORDS.stream().anyMatch(leagueName::contains)) {
                throw new DataQualityException("Match is pre-season", DataQualityException.Reason.PRE_TEMPORADA);
            }
        }
    }

    private void validateNotObscureOrStateLeague(FixtureDTO fixture) {
        if (filterEstaduais) {
            String leagueName = fixture.league().name();
            if (ESTADUAL_KEYWORDS.stream().anyMatch(leagueName::contains)) {
                throw new DataQualityException("Match is from an obscure or state league", DataQualityException.Reason.OBSCURE_LEAGUE);
            }
        }
    }

    private void validateHistoricalData(FixtureDTO fixture) {
        // In a real scenario, this would check if we have enough historical data for the teams/league
        // For now, we'll assume it's OK if it passed previous filters
    }
}
