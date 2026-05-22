package com.betanalyzer.application;

import com.betanalyzer.domain.model.Match;
import com.betanalyzer.domain.model.Odds;
import com.betanalyzer.infrastructure.client.TheOddsApiClient;
import com.betanalyzer.infrastructure.client.dto.TheOddsResponseDTO;
import com.betanalyzer.infrastructure.persistence.OddsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OddsHistoryService {

    private final TheOddsApiClient theOddsApiClient;
    private final OddsRepository oddsRepository;

    /**
     * Captura e Salva Odds
     */
    @Transactional
    public List<Odds> captureAndSaveOdds(Match match, String sport) {
        log.info("Capturing odds for match: {} vs {} (Sport: {})", 
                match.getHomeTeam().getName(), match.getAwayTeam().getName(), sport);

        List<TheOddsResponseDTO> events = theOddsApiClient.getOddsForMatch(sport, match.getMatchDate().toLocalDate());
        
        String homeTeamName = match.getHomeTeam().getName();
        String awayTeamName = match.getAwayTeam().getName();

        List<Odds> savedOdds = new ArrayList<>();

        events.stream()
                .filter(e -> matchesTeams(e, homeTeamName, awayTeamName))
                .findFirst()
                .ifPresent(event -> {
                    for (TheOddsResponseDTO.BookmakerDTO bookmaker : event.bookmakers()) {
                        for (TheOddsResponseDTO.MarketDTO market : bookmaker.markets()) {
                            for (TheOddsResponseDTO.OutcomeDTO outcome : market.outcomes()) {
                                Odds odds = buildOdds(match, bookmaker, market, outcome);
                                savedOdds.add(oddsRepository.save(odds));
                            }
                        }
                    }
                });

        log.info("Saved {} odds for match {}", savedOdds.size(), match.getId());
        return savedOdds;
    }

    private boolean matchesTeams(TheOddsResponseDTO event, String home, String away) {
        return (event.homeTeam().equalsIgnoreCase(home) && event.awayTeam().equalsIgnoreCase(away)) ||
               (event.homeTeam().contains(home.split(" ")[0]) && event.awayTeam().contains(away.split(" ")[0]));
    }

    private Odds buildOdds(Match match, TheOddsResponseDTO.BookmakerDTO bookmaker, 
                          TheOddsResponseDTO.MarketDTO market, TheOddsResponseDTO.OutcomeDTO outcome) {
        String marketKey = market.key().toUpperCase() + "_" + outcome.name().toUpperCase();
        if (outcome.point() != null) {
            marketKey += "_" + outcome.point().toString().replace(".", "_");
        }

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
