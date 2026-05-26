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
        String leagueName = "Premier League";

        League englandLeague = League.builder().name("Premier League").country("England").build();
        League kazakhstanLeague = League.builder().name("Premier League").country("Kazakhstan").build();

        Match englandMatch = Match.builder().league(englandLeague).build();
        Match kazakhstanMatch = Match.builder().league(kazakhstanLeague).build();

        MatchResponseDTO responseDTO = new MatchResponseDTO();
        responseDTO.setLeagueName("Premier League");

        when(matchRepository.findAll()).thenReturn(List.of(englandMatch, kazakhstanMatch));
        when(matchMapper.toResponseDTO(englandMatch)).thenReturn(responseDTO);

        // when
        List<MatchResponseDTO> result = matchService.getMatchesBySupportedLeague(SupportedLeague.PREMIER_LEAGUE);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLeagueName()).isEqualTo("Premier League");
    }

    @Test
    void shouldFilterSerieAForBrazil() {
        // given
        League brazilLeague = League.builder().name("Serie A").country("Brazil").build();
        League italyLeague = League.builder().name("Serie A").country("Italy").build();

        Match brazilMatch = Match.builder().league(brazilLeague).build();
        Match italyMatch = Match.builder().league(italyLeague).build();

        MatchResponseDTO responseDTO = new MatchResponseDTO();
        responseDTO.setLeagueName("Serie A");

        when(matchRepository.findAll()).thenReturn(List.of(brazilMatch, italyMatch));
        when(matchMapper.toResponseDTO(brazilMatch)).thenReturn(responseDTO);

        // when
        List<MatchResponseDTO> result = matchService.getMatchesBySupportedLeague(SupportedLeague.BRASILEIRAO);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLeagueName()).isEqualTo("Serie A");
    }

    @Test
    void shouldFilterSerieAForItaly() {
        League brazilLeague = League.builder().name("Serie A").country("Brazil").build();
        League italyLeague = League.builder().name("Serie A").country("Italy").build();

        Match brazilMatch = Match.builder().league(brazilLeague).build();
        Match italyMatch = Match.builder().league(italyLeague).build();

        MatchResponseDTO responseDTO = new MatchResponseDTO();
        responseDTO.setLeagueName("Serie A");

        when(matchRepository.findAll()).thenReturn(List.of(brazilMatch, italyMatch));
        when(matchMapper.toResponseDTO(italyMatch)).thenReturn(responseDTO);

        List<MatchResponseDTO> result = matchService.getMatchesBySupportedLeague(SupportedLeague.SERIE_A);

        assertThat(result).hasSize(1);
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
