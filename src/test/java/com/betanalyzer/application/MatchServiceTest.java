package com.betanalyzer.application;

import com.betanalyzer.application.dto.MatchResponseDTO;
import com.betanalyzer.application.dto.request.CreateMatchRequest;
import com.betanalyzer.application.mapper.MatchMapper;
import com.betanalyzer.domain.enums.MatchStatus;
import com.betanalyzer.domain.model.Match;
import com.betanalyzer.infrastructure.persistence.BetSuggestionRepository;
import com.betanalyzer.infrastructure.persistence.MatchRepository;
import com.betanalyzer.infrastructure.persistence.MatchStatsRepository;
import com.betanalyzer.infrastructure.persistence.OddsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private BetSuggestionRepository suggestionRepository;

    @Mock
    private OddsRepository oddsRepository;

    @Mock
    private MatchStatsRepository matchStatsRepository;

    @Mock
    private MatchMapper matchMapper;

    @InjectMocks
    private MatchService matchService;

    @Test
    void shouldCreateMatchSuccessfully() {
        // given
        LocalDateTime matchDate = LocalDateTime.now().plusDays(1);
        CreateMatchRequest request = new CreateMatchRequest(
                "Flamengo", "Vasco", matchDate, "Brasileirão"
        );
        
        Match match = Match.builder()
                .matchDate(matchDate)
                .status(MatchStatus.NS)
                .build();
        
        Match savedMatch = Match.builder()
                .id(UUID.randomUUID())
                .matchDate(matchDate)
                .status(MatchStatus.NS)
                .build();
        
        MatchResponseDTO responseDTO = new MatchResponseDTO();
        responseDTO.setId(savedMatch.getId());
        responseDTO.setHomeTeamName("Flamengo");
        responseDTO.setAwayTeamName("Vasco");
        responseDTO.setMatchDate(matchDate);
        responseDTO.setLeagueName("Brasileirão");
        responseDTO.setStatus(MatchStatus.NS);

        when(matchMapper.toEntity(request)).thenReturn(match);
        when(matchRepository.save(any(Match.class))).thenReturn(savedMatch);
        when(matchMapper.toResponseDTO(savedMatch)).thenReturn(responseDTO);

        // when
        MatchResponseDTO result = matchService.createMatch(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getHomeTeamName()).isEqualTo("Flamengo");
        assertThat(result.getAwayTeamName()).isEqualTo("Vasco");
        assertThat(result.getId()).isEqualTo(savedMatch.getId());
    }

    @Test
    void shouldDeleteMatchSuccessfully() {
        // given
        UUID matchId = UUID.randomUUID();
        Match match = Match.builder().id(matchId).build();

        when(matchRepository.findById(matchId)).thenReturn(java.util.Optional.of(match));

        // when
        matchService.deleteMatch(matchId);

        // then
        org.mockito.Mockito.verify(suggestionRepository).deleteByMatchId(matchId);
        org.mockito.Mockito.verify(oddsRepository).deleteByMatchId(matchId);
        org.mockito.Mockito.verify(matchStatsRepository).deleteByMatchId(matchId);
        org.mockito.Mockito.verify(matchRepository).delete(match);
    }
}