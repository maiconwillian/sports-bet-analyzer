package com.betanalyzer.application;

import com.betanalyzer.application.dto.SettlePendingResultDTO;
import com.betanalyzer.domain.enums.MatchStatus;
import com.betanalyzer.domain.enums.SuggestionStatus;
import com.betanalyzer.domain.model.BetSuggestion;
import com.betanalyzer.domain.model.League;
import com.betanalyzer.domain.model.Match;
import com.betanalyzer.domain.model.Team;
import com.betanalyzer.infrastructure.persistence.BetSuggestionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuggestionSettlementServiceTest {

    @Mock
    private BetSuggestionRepository suggestionRepository;

    @InjectMocks
    private SuggestionSettlementService settlementService;

    @Test
    void shouldSettleOver25WinWhenScoreIs3x2() {
        BetSuggestion suggestion = pendingOver25(match(MatchStatus.FT, 3, 2));
        when(suggestionRepository.findByStatus(SuggestionStatus.PENDING)).thenReturn(List.of(suggestion));
        when(suggestionRepository.save(any(BetSuggestion.class))).thenAnswer(inv -> inv.getArgument(0));

        SettlePendingResultDTO result = settlementService.settlePendingSuggestions();

        assertThat(result.getSettled()).isEqualTo(1);
        assertThat(result.getWon()).isEqualTo(1);
        assertThat(suggestion.getStatus()).isEqualTo(SuggestionStatus.WON);
        verify(suggestionRepository).save(suggestion);
    }

    @Test
    void shouldSettleOver25LossWhenScoreIs1x0() {
        BetSuggestion suggestion = pendingOver25(match(MatchStatus.FT, 1, 0));
        when(suggestionRepository.findByStatus(SuggestionStatus.PENDING)).thenReturn(List.of(suggestion));
        when(suggestionRepository.save(any(BetSuggestion.class))).thenAnswer(inv -> inv.getArgument(0));

        SettlePendingResultDTO result = settlementService.settlePendingSuggestions();

        assertThat(result.getLost()).isEqualTo(1);
        assertThat(suggestion.getStatus()).isEqualTo(SuggestionStatus.LOST);
    }

    @Test
    void shouldVoidWhenMatchPostponed() {
        BetSuggestion suggestion = pendingOver25(match(MatchStatus.PST, null, null));
        when(suggestionRepository.findByStatus(SuggestionStatus.PENDING)).thenReturn(List.of(suggestion));
        when(suggestionRepository.save(any(BetSuggestion.class))).thenAnswer(inv -> inv.getArgument(0));

        SettlePendingResultDTO result = settlementService.settlePendingSuggestions();

        assertThat(result.getVoided()).isEqualTo(1);
        assertThat(suggestion.getStatus()).isEqualTo(SuggestionStatus.VOID);
    }

    private static BetSuggestion pendingOver25(Match match) {
        return BetSuggestion.builder()
                .id(UUID.randomUUID())
                .match(match)
                .market("OVER_2_5")
                .pickedOdd(new BigDecimal("2.15"))
                .pickedBookmaker("Pinnacle")
                .confidence(70.0)
                .expectedValue(0.1)
                .stake(new BigDecimal("100"))
                .status(SuggestionStatus.PENDING)
                .build();
    }

    private static Match match(MatchStatus status, Integer home, Integer away) {
        League league = League.builder().name("Serie A").country("Brazil").build();
        Team homeTeam = Team.builder().name("Home").build();
        Team awayTeam = Team.builder().name("Away").build();
        return Match.builder()
                .id(UUID.randomUUID())
                .league(league)
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .status(status)
                .homeGoals(home)
                .awayGoals(away)
                .build();
    }
}
