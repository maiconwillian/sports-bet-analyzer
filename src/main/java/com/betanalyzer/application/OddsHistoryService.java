package com.betanalyzer.application;

import com.betanalyzer.domain.enums.SupportedLeague;
import com.betanalyzer.domain.model.Match;
import com.betanalyzer.domain.model.Odds;
import com.betanalyzer.infrastructure.client.TheOddsApiClient;
import com.betanalyzer.infrastructure.client.dto.TheOddsResponseDTO;
import com.betanalyzer.infrastructure.persistence.OddsRepository;
import com.betanalyzer.shared.exception.ApiIntegrationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OddsHistoryService {

    private final TheOddsApiClient theOddsApiClient;
    private final OddsRepository oddsRepository;

    @Transactional
    public List<Odds> captureAndSaveOdds(Match match) {
        log.info("Capturing odds for match: {} vs {} (League: {})", 
                match.getHomeTeam().getName(), match.getAwayTeam().getName(), match.getLeague().getName());

        // 1. Validar Data
        LocalDate matchDate = match.getMatchDate().toLocalDate();
        if (matchDate.isBefore(LocalDate.now().minusDays(3))) {
            log.warn("Match date {} is outside free tier range", matchDate);
            return List.of();
        }

        // 2. ← NOVO: Validar se a liga é suportada
        String sportKey = getSportKeyForLeague(match.getLeague());
        if (sportKey == null || sportKey.isBlank()) {
            log.warn("League '{}' is not supported for odds capture. Supported leagues: {}",
                match.getLeague().getName(),
                Arrays.stream(SupportedLeague.values())
                      .map(SupportedLeague::getApiName)
                      .collect(Collectors.joining(", ")));
            return List.of();
        }

        // 3. Buscar Odds
        List<TheOddsResponseDTO> events = theOddsApiClient.getOddsForMatch(sportKey, matchDate);

        // 4. Filtrar eventos que combinam com os times do match
        List<Odds> capturedOdds = events.stream()
                .filter(event -> matchesTeams(event, match.getHomeTeam().getName(), match.getAwayTeam().getName()))
                .flatMap(event -> event.bookmakers().stream()
                        .flatMap(bookmaker -> bookmaker.markets().stream()
                                .flatMap(market -> market.outcomes().stream()
                                        .map(outcome -> buildOdds(match, bookmaker, market, outcome)))))
                .toList();

        // 5. Salvar no BD
        if (!capturedOdds.isEmpty()) {
            oddsRepository.saveAll(capturedOdds);
            log.info("Successfully captured and saved {} odds for match: {} vs {}", 
                    capturedOdds.size(), match.getHomeTeam().getName(), match.getAwayTeam().getName());
        } else {
            log.warn("No matching odds found for match: {} vs {}", 
                    match.getHomeTeam().getName(), match.getAwayTeam().getName());
        }

        return capturedOdds;
    }

    private String getSportKeyForLeague(com.betanalyzer.domain.model.League league) {
        log.info("Mapping league: '{}' (apiId: {}) to sport key", league.getName(), league.getApiId());

        for (SupportedLeague supported : SupportedLeague.values()) {
            if (supported.getApiName().equalsIgnoreCase(league.getName())) {
                log.info("✅ Found mapping: {} -> {}", league.getName(), supported.getTheOddsSportKey());
                return supported.getTheOddsSportKey();
            }
        }

        // ← MELHORAR AQUI: Ser mais específico
        log.warn("❌ League '{}' (apiId: {}) is NOT in SupportedLeague enum. " +
                        "Only these leagues are supported: {}",
                league.getName(),
                league.getApiId(),
                Arrays.stream(SupportedLeague.values())
                        .map(SupportedLeague::getApiName)
                        .collect(Collectors.joining(", ")));

        return null;
    }

    private boolean matchesTeams(TheOddsResponseDTO event, String home, String away) {
        String eventHome = event.homeTeam().toLowerCase();
        String eventAway = event.awayTeam().toLowerCase();
        String targetHome = home.toLowerCase();
        String targetAway = away.toLowerCase();

        // Exact match
        if (eventHome.equals(targetHome) && eventAway.equals(targetAway)) return true;

        // Fuzzy matching simples (contém o primeiro nome)
        String firstWordHome = targetHome.split(" ")[0];
        String firstWordAway = targetAway.split(" ")[0];

        return (eventHome.contains(firstWordHome) && eventAway.contains(firstWordAway));
    }

    private Odds buildOdds(Match match, TheOddsResponseDTO.BookmakerDTO bookmaker, 
                          TheOddsResponseDTO.MarketDTO market, TheOddsResponseDTO.OutcomeDTO outcome) {
        
        // Formato esperado: TOTALS_OVER_2_5
        String marketKey = "TOTALS_" + outcome.name().toUpperCase() + "_" + outcome.point().toString().replace(".", "_");

        return Odds.builder()
                .match(match)
                .bookmaker(bookmaker.title())
                .bookmakerKey(bookmaker.key())
                .market(marketKey)
                .oddsValue(BigDecimal.valueOf(outcome.price()))
                .capturedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Buscar Odds Históricas
     */
    public List<Odds> getOddsHistory(UUID matchId) {
        return oddsRepository.findByMatchIdOrderByCapturedAtDesc(matchId);
    }

    /**
     * Calcular CLV (Closing Line Value)
     * CLV = (pickedOdd.price - finalOdd.price) / finalOdd.price * 100
     */
    public BigDecimal calculateCLV(Odds pickedOdd, Odds finalOdd) {
        if (pickedOdd == null || finalOdd == null || 
            finalOdd.getOddsValue() == null || finalOdd.getOddsValue().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal pickedPrice = pickedOdd.getOddsValue();
        BigDecimal finalPrice = finalOdd.getOddsValue();

        return pickedPrice.subtract(finalPrice)
                .divide(finalPrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}
