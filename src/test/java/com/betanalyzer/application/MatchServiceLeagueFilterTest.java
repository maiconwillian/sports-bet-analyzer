package com.betanalyzer.application;

import com.betanalyzer.application.dto.MatchResponseDTO;
import com.betanalyzer.application.mapper.MatchMapper;
import com.betanalyzer.domain.enums.SupportedLeague;
import com.betanalyzer.domain.model.League;
import com.betanalyzer.domain.model.Match;
import com.betanalyzer.infrastructure.persistence.MatchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchServiceLeagueFilterTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private MatchMapper matchMapper;

    @InjectMocks
    private MatchService matchService;

    @Test
    void shouldFilterByLeagueAndCountryAutomatically() {
        // given
        String leagueName = "Premier League"; // In enum it is England (EN)
        
        League englandLeague = League.builder().name("Premier League").country("EN").build();
        League kazakhstanLeague = League.builder().name("Premier League").country("Kazakhstan").build();
        
        Match englandMatch = Match.builder().league(englandLeague).build();
        Match kazakhstanMatch = Match.builder().league(kazakhstanLeague).build();
        
        MatchResponseDTO responseDTO = new MatchResponseDTO();
        responseDTO.setLeagueName("Premier League");
        
        when(matchRepository.findAll()).thenReturn(List.of(englandMatch, kazakhstanMatch));
        when(matchMapper.toResponseDTO(englandMatch)).thenReturn(responseDTO);

        // when
        List<MatchResponseDTO> result = matchService.getMatchesByLeague(leagueName);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLeagueName()).isEqualTo("Premier League");
    }

    @Test
    void shouldFilterSerieAForBrazil() {
        // given
        String leagueName = "Série A"; // In enum it is Brazil (BR)
        
        League brazilLeague = League.builder().name("Série A").country("BR").build();
        League italyLeague = League.builder().name("Série A").country("Italy").build();
        
        Match brazilMatch = Match.builder().league(brazilLeague).build();
        Match italyMatch = Match.builder().league(italyLeague).build();
        
        MatchResponseDTO responseDTO = new MatchResponseDTO();
        responseDTO.setLeagueName("Série A");
        
        when(matchRepository.findAll()).thenReturn(List.of(brazilMatch, italyMatch));
        when(matchMapper.toResponseDTO(brazilMatch)).thenReturn(responseDTO);

        // when
        List<MatchResponseDTO> result = matchService.getMatchesByLeague(leagueName);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLeagueName()).isEqualTo("Série A");
    }

    @Test
    void shouldReturnEmptyListForUnknownLeague() {
        // given
        String leagueName = "Unknown League";

        // when
        List<MatchResponseDTO> result = matchService.getMatchesByLeague(leagueName);

        // then
        assertThat(result).isEmpty();
    }
}
