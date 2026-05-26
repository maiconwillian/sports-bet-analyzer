package com.betanalyzer.infrastructure.client;

import com.betanalyzer.application.service.DataQualityValidator;
import com.betanalyzer.domain.enums.SupportedLeague;
import com.betanalyzer.domain.enums.SupportedMarket;
import com.betanalyzer.infrastructure.client.dto.ApiFootballResponseDTO;
import com.betanalyzer.infrastructure.client.dto.FixtureDTO;
import com.betanalyzer.shared.exception.ApiIntegrationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ApiFootballClient {

    private final WebClient webClient;
    private final DataQualityValidator dataQualityValidator;

    public ApiFootballClient(@Qualifier("footballWebClient") WebClient webClient, DataQualityValidator dataQualityValidator) {
        this.webClient = webClient;
        this.dataQualityValidator = dataQualityValidator;
    }

    /**
     * Busca TODAS as fixtures (sem filtro)
     */
    public List<FixtureDTO> getFixturesByDate(LocalDate date) {
        log.info("Fetching fixtures for date: {}", date);
        try {
            ApiFootballResponseDTO response = webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/fixtures")
                            .queryParam("date", date)
                            .build())
                    .retrieve()
                    .bodyToMono(ApiFootballResponseDTO.class)
                    .block();

            if (response == null || response.fixtures() == null) {
                log.warn("No fixtures found for date: {}", date);
                return List.of();
            }

            log.info("Fetched {} fixtures total", response.fixtures().size());
            return response.fixtures();

        } catch (Exception e) {
            log.error("Error fetching fixtures for date {}: {}", date, e.getMessage(), e);
            throw new ApiIntegrationException("Failed to fetch fixtures: " + e.getMessage(), e);
        }
    }

    public List<FixtureDTO> getQualityFixtures(LocalDate date, SupportedLeague league) {
        log.info("Fetching quality fixtures for league: {} on date: {}", league, date);

        List<FixtureDTO> allFixtures = getFixturesByDate(date);

        return allFixtures.stream()
            .filter(f -> league.matches(f.league().name(), f.league().country()))
            .filter(f -> dataQualityValidator.isQualityFixture(f, league))
            .collect(Collectors.toList());
    }

    public List<FixtureDTO> getQualityFixturesForMarket(LocalDate date, SupportedLeague league, SupportedMarket market) {
        log.info("Fetching quality fixtures for league: {} and market: {} on date: {}", league, market, date);

        // At this stage, market validation might just be checking if the market is supported
        // and potentially if the league is suitable for that market.
        // For now, it's similar to getQualityFixtures but with market awareness.

        return getQualityFixtures(date, league);
    }
}
