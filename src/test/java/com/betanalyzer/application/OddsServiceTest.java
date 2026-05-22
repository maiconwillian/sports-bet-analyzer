package com.betanalyzer.application;

import com.betanalyzer.application.dto.OddsResponseDTO;
import com.betanalyzer.application.dto.request.OddsRequestDTO;
import com.betanalyzer.application.mapper.OddsMapper;
import com.betanalyzer.domain.model.Match;
import com.betanalyzer.domain.model.Odds;
import com.betanalyzer.infrastructure.persistence.MatchRepository;
import com.betanalyzer.infrastructure.persistence.OddsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OddsServiceTest {

    @Mock
    private OddsRepository oddsRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private OddsMapper oddsMapper;

    @InjectMocks
    private OddsService oddsService;

    @Test
    void shouldSaveOddsSuccessfully() {
        // given
        UUID matchId = UUID.randomUUID();
        OddsRequestDTO request = new OddsRequestDTO(
                matchId, "Bet365", new BigDecimal("2.10"), new BigDecimal("3.50"), new BigDecimal("3.20")
        );
        
        Match match = Match.builder().id(matchId).build();
        Odds odds = Odds.builder().bookmaker("Bet365").build();
        Odds savedOdds = Odds.builder().id(UUID.randomUUID()).match(match).bookmaker("Bet365").build();
        
        OddsResponseDTO responseDTO = new OddsResponseDTO(
                savedOdds.getId(),
                matchId,
                "Bet365",
                "bet365",
                "hda",
                new BigDecimal("2.10"),
                null,
                null
        );

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(oddsMapper.toEntity(request)).thenReturn(odds);
        when(oddsRepository.save(any(Odds.class))).thenReturn(savedOdds);
        when(oddsMapper.toResponseDTO(savedOdds)).thenReturn(responseDTO);

        // when
        OddsResponseDTO result = oddsService.saveOdds(matchId, request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.bookmaker()).isEqualTo("Bet365");
    }
}