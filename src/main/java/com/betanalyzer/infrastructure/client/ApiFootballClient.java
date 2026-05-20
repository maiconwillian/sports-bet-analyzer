package com.betanalyzer.infrastructure.client;

import com.betanalyzer.application.service.DataQualityValidator;
import com.betanalyzer.domain.enums.SupportedLeague;
import com.betanalyzer.domain.enums.SupportedMarket;
import com.betanalyzer.infrastructure.client.dto.ApiFootballResponseDTO;
import com.betanalyzer.infrastructure.client.dto.FixtureDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApiFootballClient {
    
    private final WebClient webClient;
    private final DataQualityValidator dataQualityValidator;
    
    /**
     * Busca todos os jogos de uma data específica
     * @param date Data no formato YYYY-MM-DD
     * @return Lista de fixtures
     */
    public List<FixtureDTO> getFixturesByDate(LocalDate date) {
        log.info("Fetching fixtures for date: {}", date);
        
        try {
            Mono<ApiFootballResponseDTO> response = webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                    .path("/fixtures")
                    .queryParam("date", date.toString())
                    .build()
                )
                .retrieve()
                .bodyToMono(ApiFootballResponseDTO.class);
            
            ApiFootballResponseDTO result = response.block();
            
            if (result != null && result.fixtures() != null) {
                log.info("Successfully fetched {} fixtures", result.fixtures().size());
                return result.fixtures();
            }
            
            log.warn("No fixtures found for date: {}", date);
            return List.of();
            
        } catch (Exception e) {
            log.error("Error fetching fixtures from API-Football", e);
            throw new RuntimeException("Failed to fetch fixtures: " + e.getMessage());
        }
    }

    public List<FixtureDTO> getQualityFixtures(LocalDate date, SupportedLeague league) {
        log.info("Fetching quality fixtures for league: {} on date: {}", league, date);
        
        List<FixtureDTO> allFixtures = getFixturesByDate(date);
        
        return allFixtures.stream()
            .filter(f -> f.league().name().equalsIgnoreCase(league.getApiName()))
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
