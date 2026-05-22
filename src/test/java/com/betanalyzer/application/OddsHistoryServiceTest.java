package com.betanalyzer.application;

import com.betanalyzer.domain.model.Match;
import com.betanalyzer.domain.model.Odds;
import com.betanalyzer.domain.model.Team;
import com.betanalyzer.infrastructure.client.TheOddsApiClient;
import com.betanalyzer.infrastructure.client.dto.TheOddsResponseDTO;
import com.betanalyzer.infrastructure.persistence.OddsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OddsHistoryServiceTest {

    @Mock
    private TheOddsApiClient theOddsApiClient;

    @Mock
    private OddsRepository oddsRepository;

    @InjectMocks
    private OddsHistoryService oddsHistoryService;

    @Test
    void shouldCaptureAndSaveOdds() {
        // given
        Team homeTeam = Team.builder().name("Flamengo").build();
        Team awayTeam = Team.builder().name("Vasco").build();
        Match match = Match.builder()
                .id(UUID.randomUUID())
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .matchDate(LocalDateTime.now())
                .build();

        TheOddsResponseDTO.OutcomeDTO outcome = new TheOddsResponseDTO.OutcomeDTO("Over", 2.5, 1.95);
        TheOddsResponseDTO.MarketDTO market = new TheOddsResponseDTO.MarketDTO("totals", List.of(outcome));
        TheOddsResponseDTO.BookmakerDTO bookmaker = new TheOddsResponseDTO.BookmakerDTO("bet365", "Bet365", List.of(market));
        TheOddsResponseDTO event = new TheOddsResponseDTO(
                "match-1", "soccer_brazil", "Soccer", "2026-05-20T19:00:00Z",
                "Flamengo RJ", "Vasco da Gama", List.of(bookmaker)
        );

        when(theOddsApiClient.getOddsForMatch(anyString(), any())).thenReturn(List.of(event));
        when(oddsRepository.save(any(Odds.class))).thenAnswer(i -> i.getArguments()[0]);

        // when
        List<Odds> result = oddsHistoryService.captureAndSaveOdds(match, "soccer_brazil");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBookmaker()).isEqualTo("Bet365");
        assertThat(result.get(0).getOddsValue()).isEqualByComparingTo(BigDecimal.valueOf(1.95));
        verify(oddsRepository, times(1)).save(any(Odds.class));
    }

    @Test
    void shouldCalculateCLVCorrectly() {
        // given
        Odds pickedOdd = Odds.builder().oddsValue(BigDecimal.valueOf(2.0)).build();
        Odds finalOdd = Odds.builder().oddsValue(BigDecimal.valueOf(1.8)).build();

        // when
        BigDecimal clv = oddsHistoryService.calculateCLV(pickedOdd, finalOdd);

        // then
        // CLV = (2.0 - 1.8) / 1.8 * 100 = 0.2 / 1.8 * 100 = 11.111...
        assertThat(clv).isCloseTo(BigDecimal.valueOf(11.11), within(BigDecimal.valueOf(0.01)));
    }

    @Test
    void shouldReturnOddsHistory() {
        // given
        UUID matchId = UUID.randomUUID();
        when(oddsRepository.findByMatchIdOrderByCapturedAtDesc(matchId)).thenReturn(List.of(new Odds()));

        // when
        List<Odds> history = oddsHistoryService.getOddsHistory(matchId);

        // then
        assertThat(history).hasSize(1);
    }
}
