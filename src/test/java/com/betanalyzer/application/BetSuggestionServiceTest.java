package com.betanalyzer.application;

import com.betanalyzer.application.dto.SuggestionResponseDTO;
import com.betanalyzer.application.dto.request.CreateSuggestionRequest;
import com.betanalyzer.application.dto.request.SuggestionResultRequest;
import com.betanalyzer.application.mapper.BetSuggestionMapper;
import com.betanalyzer.domain.enums.SuggestionStatus;
import com.betanalyzer.domain.model.BetSuggestion;
import com.betanalyzer.domain.model.Match;
import com.betanalyzer.infrastructure.persistence.BetSuggestionRepository;
import com.betanalyzer.infrastructure.persistence.MatchRepository;
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
class BetSuggestionServiceTest {

    @Mock
    private BetSuggestionRepository suggestionRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private BetSuggestionMapper suggestionMapper;

    @InjectMocks
    private BetSuggestionService suggestionService;

    @Test
    void shouldCreateSuggestionSuccessfully() {
        // given
        UUID matchId = UUID.randomUUID();
        CreateSuggestionRequest request = new CreateSuggestionRequest(
                matchId, "Home Win", new BigDecimal("2.00"), "Bet365", 75.0, 10.0, new BigDecimal("100.00")
        );
        
        Match match = Match.builder().id(matchId).build();
        BetSuggestion suggestion = BetSuggestion.builder().market("Home Win").build();
        BetSuggestion savedSuggestion = BetSuggestion.builder().id(UUID.randomUUID()).match(match).market("Home Win").build();
        
        SuggestionResponseDTO responseDTO = new SuggestionResponseDTO();
        responseDTO.setId(savedSuggestion.getId());
        responseDTO.setMarket("Home Win");

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(suggestionMapper.toEntity(request)).thenReturn(suggestion);
        when(suggestionRepository.save(any(BetSuggestion.class))).thenReturn(savedSuggestion);
        when(suggestionMapper.toResponseDTO(savedSuggestion)).thenReturn(responseDTO);

        // when
        SuggestionResponseDTO result = suggestionService.createSuggestion(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getMarket()).isEqualTo("Home Win");
    }

    @Test
    void shouldUpdateResultAndCalculateROISuccessfully() {
        // given
        UUID suggestionId = UUID.randomUUID();
        SuggestionResultRequest request = new SuggestionResultRequest(SuggestionStatus.WON);
        
        BetSuggestion suggestion = BetSuggestion.builder()
                .id(suggestionId)
                .stake(new BigDecimal("100.00"))
                .pickedOdd(new BigDecimal("2.50"))
                .status(SuggestionStatus.PENDING)
                .build();
        
        when(suggestionRepository.findById(suggestionId)).thenReturn(Optional.of(suggestion));
        when(suggestionRepository.save(any(BetSuggestion.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(suggestionMapper.toResponseDTO(any(BetSuggestion.class))).thenAnswer(invocation -> {
            BetSuggestion s = invocation.getArgument(0);
            SuggestionResponseDTO dto = new SuggestionResponseDTO();
            dto.setId(s.getId());
            dto.setStatus(s.getStatus());
            dto.setRoi(s.getRoi());
            return dto;
        });

        // when
        SuggestionResponseDTO result = suggestionService.updateSuggestionResult(suggestionId, request);

        // then
        assertThat(result.getStatus()).isEqualTo(SuggestionStatus.WON);
        assertThat(result.getRoi()).isEqualTo(150.0); // (100 * 2.5 - 100) / 100 * 100 = 150%
    }
}