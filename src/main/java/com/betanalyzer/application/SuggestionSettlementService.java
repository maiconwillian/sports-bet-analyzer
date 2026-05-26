package com.betanalyzer.application;

import com.betanalyzer.application.dto.SettlePendingResultDTO;
import com.betanalyzer.domain.enums.MatchStatus;
import com.betanalyzer.domain.enums.SuggestionStatus;
import com.betanalyzer.domain.model.BetSuggestion;
import com.betanalyzer.domain.model.Match;
import com.betanalyzer.infrastructure.persistence.BetSuggestionRepository;
import com.betanalyzer.shared.util.MarketUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SuggestionSettlementService {

    private static final Set<MatchStatus> VOID_MATCH_STATUSES = EnumSet.of(
            MatchStatus.PST, MatchStatus.CANC, MatchStatus.ABD, MatchStatus.SUSP
    );

    private static final Set<MatchStatus> FINISHED_MATCH_STATUSES = EnumSet.of(
            MatchStatus.FT, MatchStatus.AET
    );

    private final BetSuggestionRepository suggestionRepository;

    @Transactional
    public SettlePendingResultDTO settlePendingSuggestions() {
        List<BetSuggestion> pending = suggestionRepository.findByStatus(SuggestionStatus.PENDING);
        int settled = 0;
        int won = 0;
        int lost = 0;
        int voided = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<>();

        for (BetSuggestion suggestion : pending) {
            try {
                Outcome outcome = resolveOutcome(suggestion);
                switch (outcome) {
                    case SKIP -> skipped++;
                    case WON -> {
                        applyResult(suggestion, SuggestionStatus.WON);
                        settled++;
                        won++;
                    }
                    case LOST -> {
                        applyResult(suggestion, SuggestionStatus.LOST);
                        settled++;
                        lost++;
                    }
                    case VOID -> {
                        applyResult(suggestion, SuggestionStatus.VOID);
                        settled++;
                        voided++;
                    }
                }
            } catch (Exception e) {
                log.error("Failed to settle suggestion {}: {}", suggestion.getId(), e.getMessage());
                errors.add(suggestion.getId() + ": " + e.getMessage());
            }
        }

        log.info("Settlement done: settled={}, won={}, lost={}, voided={}, skipped={}",
                settled, won, lost, voided, skipped);

        return SettlePendingResultDTO.builder()
                .settled(settled)
                .won(won)
                .lost(lost)
                .voided(voided)
                .skipped(skipped)
                .errors(errors)
                .build();
    }

    private enum Outcome { SKIP, WON, LOST, VOID }

    private Outcome resolveOutcome(BetSuggestion suggestion) {
        Match match = suggestion.getMatch();
        if (match == null || !MarketUtils.isOver25Market(suggestion.getMarket())) {
            return Outcome.SKIP;
        }

        MatchStatus status = match.getStatus();
        if (status != null && VOID_MATCH_STATUSES.contains(status)) {
            return Outcome.VOID;
        }

        if (status == null || !FINISHED_MATCH_STATUSES.contains(status)) {
            return Outcome.SKIP;
        }

        Integer home = match.getHomeGoals();
        Integer away = match.getAwayGoals();
        if (home == null || away == null) {
            return Outcome.SKIP;
        }

        return (home + away) > 2 ? Outcome.WON : Outcome.LOST;
    }

    private void applyResult(BetSuggestion suggestion, SuggestionStatus status) {
        suggestion.setStatus(status);
        suggestion.setActualResult(status.name());
        suggestion.setRoi(calculateRoi(suggestion));
        suggestionRepository.save(suggestion);
    }

    private static Double calculateRoi(BetSuggestion suggestion) {
        if (suggestion.getStatus() == SuggestionStatus.PENDING || suggestion.getStatus() == null) {
            return 0.0;
        }
        if (suggestion.getStatus() == SuggestionStatus.VOID) {
            return 0.0;
        }
        BigDecimal stake = suggestion.getStake() != null ? suggestion.getStake() : new BigDecimal("100.00");
        BigDecimal odd = suggestion.getPickedOdd();
        if (suggestion.getStatus() == SuggestionStatus.WON) {
            BigDecimal profit = stake.multiply(odd).subtract(stake);
            return profit.divide(stake, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).doubleValue();
        }
        if (suggestion.getStatus() == SuggestionStatus.LOST) {
            return -100.0;
        }
        return 0.0;
    }

}
