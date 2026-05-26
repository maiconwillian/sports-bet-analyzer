package com.betanalyzer.infrastructure.client;

import com.betanalyzer.domain.enums.SupportedLeague;
import com.betanalyzer.config.TheOddsApiProperties;
import com.betanalyzer.infrastructure.client.dto.TheOddsResponseDTO;
import com.betanalyzer.infrastructure.client.dto.TheOddsResponseDTO.BookmakerDTO;
import com.betanalyzer.shared.exception.ApiIntegrationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
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
     * Chama: GET /sports/{sport}/odds com regions obrigatório
     */
    public List<TheOddsResponseDTO> getOddsForMatch(String sport, LocalDate date) {
        log.info("Fetching odds for sport: {} and date: {}", sport, date);

        try {
            Mono<List<TheOddsResponseDTO>> response = webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/sports/{sport}/odds")
                            .queryParam("apiKey", properties.getKey())
                            .queryParam("regions", properties.getRegions())
                            .queryParam("markets", "totals")
                            .queryParam("dateFormat", "iso")
                            .build(sport)
                    )
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                        int status = clientResponse.statusCode().value();
                        log.error("Client error: {} from The Odds API", status);
                        
                        if (status == 401) {
                            return Mono.error(new ApiIntegrationException("The Odds API: Unauthorized (Check API Key)"));
                        }
                        if (status == 404) {
                            return Mono.error(new ApiIntegrationException("The Odds API: Sport not found: " + sport));
                        }
                        if (status == 422) {
                            return Mono.error(new ApiIntegrationException("The Odds API: Invalid parameters (check regions, sport key)"));
                        }
                        if (status == 429) {
                            return Mono.error(new ApiIntegrationException("The Odds API: Rate limit exceeded"));
                        }
                        return Mono.error(new ApiIntegrationException("The Odds API: Client error: " + status));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> 
                        Mono.error(new ApiIntegrationException("The Odds API: Server error"))
                    )
                    .bodyToFlux(TheOddsResponseDTO.class)
                    .collectList();

            List<TheOddsResponseDTO> result = response.block();

            if (result != null && !result.isEmpty()) {
                List<TheOddsResponseDTO> filtered = result.stream()
                        .filter(dto -> dto.commenceTime().startsWith(date.toString()))
                        .toList();
                log.info("Successfully fetched {} events for date: {}", filtered.size(), date);
                return filtered;
            }

            log.warn("No odds found for sport: {} and date: {}", sport, date);
            return List.of();

        } catch (ApiIntegrationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching odds: {}", e.getMessage(), e);
            throw new ApiIntegrationException("Failed to fetch odds: " + e.getMessage(), e);
        }
    }

    /**
     * Busca Odds por Match usando API-Football ID (BRIDGE)
     */
    public List<TheOddsResponseDTO> getOddsForMatchByApiFootballId(Long apiFootballId, LocalDate date) {
        // Agora que removemos o LeagueApiIdMapping, precisamos de uma forma de saber qual o sport key 
        // para o The Odds API. Se não houver mapeamento, talvez devêssemos passar o sport key diretamente
        // ou buscar de alguma configuração/banco.
        // Por enquanto, vou manter o método mas logar que o mapeamento automático foi removido.
        log.warn("Automatic mapping from API-Football ID {} to The Odds API sport key is no longer supported.", apiFootballId);
        return List.of();
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

    public boolean matchesTeams(TheOddsResponseDTO event, String home, String away) {
        String eventHome = normalizeTeamName(event.homeTeam());
        String eventAway = normalizeTeamName(event.awayTeam());
        String targetHome = normalizeTeamName(home);
        String targetAway = normalizeTeamName(away);

        // Exact match
        if (eventHome.equals(targetHome) && eventAway.equals(targetAway)) {
            return true;
        }

        // Reverse check (times podem estar invertidos)
        if (eventHome.equals(targetAway) && eventAway.equals(targetHome)) {
            return true;
        }

        // Fuzzy: primeiras palavras + levenshtein distance
        String[] targetHomeWords = targetHome.split(" ");
        String[] targetAwayWords = targetAway.split(" ");

        String firstWordHome = targetHomeWords[0];
        String firstWordAway = targetAwayWords[0];

        boolean homeMatch = eventHome.contains(firstWordHome) || levenshteinDistance(eventHome, firstWordHome) <= 2;
        boolean awayMatch = eventAway.contains(firstWordAway) || levenshteinDistance(eventAway, firstWordAway) <= 2;

        return homeMatch && awayMatch;
    }

    /**
     * Normaliza nome do time: remove acentos, espaços, lowercase
     */
    private String normalizeTeamName(String name) {
        if (name == null) return "";

        return name
                .toLowerCase()
                .trim()
                // Remove acentos: é, á, ã, etc
                .replaceAll("[áàâãä]", "a")
                .replaceAll("[éèêë]", "e")
                .replaceAll("[íìîï]", "i")
                .replaceAll("[óòôõö]", "o")
                .replaceAll("[úùûü]", "u")
                .replaceAll("[ç]", "c");
    }

    /**
     * Levenshtein Distance: mede diferença entre strings
     * Útil para typos: "Alavés" vs "Alaves"
     */
    private int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i-1][j] + 1, dp[i][j-1] + 1), dp[i-1][j-1] + cost);
            }
        }

        return dp[a.length()][b.length()];
    }
}
