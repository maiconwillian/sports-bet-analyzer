package com.betanalyzer.infrastructure.client;

import com.betanalyzer.config.TheOddsApiProperties;
import com.betanalyzer.infrastructure.client.dto.TheOddsResponseDTO;
import com.betanalyzer.infrastructure.client.dto.TheOddsResponseDTO.BookmakerDTO;
import com.betanalyzer.shared.exception.ApiIntegrationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class TheOddsApiClient {

    private final WebClient webClient;
    private final TheOddsApiProperties properties;

    public TheOddsApiClient(@Qualifier("oddsWebClient") WebClient webClient, TheOddsApiProperties properties) {
        this.webClient = webClient;
        this.properties = properties;
    }

    /**
     * Busca Odds por Match
     * Chama: GET /sports/{sport}/events
     */
    public List<TheOddsResponseDTO> getOddsForMatch(String sport, LocalDate date) {
        log.info("Fetching odds for sport: {} and date: {}", sport, date);

        try {
            Mono<List<TheOddsResponseDTO>> response = webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/sports/{sport}/events")
                            .queryParam("apiKey", properties.getKey())
                            .queryParam("dateFormat", "iso")
                            .build(sport)
                    )
                    .retrieve()
                    .bodyToFlux(TheOddsResponseDTO.class)
                    .collectList();

            List<TheOddsResponseDTO> result = response.block();

            if (result != null) {
                List<TheOddsResponseDTO> filtered = result.stream()
                        .filter(dto -> dto.commenceTime().startsWith(date.toString()))
                        .toList();
                log.info("Successfully fetched {} events for date: {}", filtered.size(), date);
                return filtered;
            }

            return List.of();

        } catch (Exception e) {
            log.error("Error fetching odds: {}", e.getMessage(), e);
            throw new ApiIntegrationException("Failed to fetch odds: " + e.getMessage(), e);
        }
    }

    /**
     * Busca Odd Específica para Over/Under 2.5
     */
    public Double getOver25Odd(String sport, LocalDate date, String homeTeam, String awayTeam, String bookmaker) {
        List<TheOddsResponseDTO> events = getOddsForMatch(sport, date);

        return events.stream()
                .filter(e -> matchesTeams(e, homeTeam, awayTeam))
                .findFirst()
                .flatMap(e -> e.bookmakers().stream()
                        .filter(b -> b.key().equalsIgnoreCase(bookmaker))
                        .findFirst())
                .flatMap(b -> b.markets().stream()
                        .filter(m -> m.key().equalsIgnoreCase("totals"))
                        .findFirst())
                .flatMap(m -> m.outcomes().stream()
                        .filter(o -> o.name().equalsIgnoreCase("Over") && Objects.equals(o.point(), 2.5))
                        .findFirst())
                .map(TheOddsResponseDTO.OutcomeDTO::price)
                .orElse(null);
    }

    private boolean matchesTeams(TheOddsResponseDTO event, String home, String away) {
        return (event.homeTeam().equalsIgnoreCase(home) && event.awayTeam().equalsIgnoreCase(away)) ||
               (event.homeTeam().contains(home.split(" ")[0]) && event.awayTeam().contains(away.split(" ")[0]));
    }
}
