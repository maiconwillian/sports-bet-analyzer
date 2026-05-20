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
import com.betanalyzer.shared.exception.MatchNotFoundException;
import com.betanalyzer.shared.exception.SuggestionNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BetSuggestionService {

    private static final Logger log = LoggerFactory.getLogger(BetSuggestionService.class);

    private final BetSuggestionRepository suggestionRepository;
    private final MatchRepository matchRepository;
    private final BetSuggestionMapper suggestionMapper;

    @Transactional(readOnly = true)
    public Page<SuggestionResponseDTO> getSuggestions(SuggestionStatus status, LocalDate date, Pageable pageable) {
        LocalDateTime start = (date != null) ? date.atStartOfDay() : LocalDateTime.of(1970, 1, 1, 0, 0);
        LocalDateTime end = (date != null) ? date.atTime(LocalTime.MAX) : LocalDateTime.of(2099, 12, 31, 23, 59);

        if (status != null) {
            return suggestionRepository.findByStatusAndCreatedAtBetween(status, start, end, pageable)
                    .map(suggestionMapper::toResponseDTO);
        } else {
            return suggestionRepository.findByCreatedAtBetween(start, end, pageable)
                    .map(suggestionMapper::toResponseDTO);
        }
    }

    @Transactional(readOnly = true)
    public SuggestionResponseDTO getSuggestionById(UUID id) {
        return suggestionRepository.findById(id)
                .map(suggestionMapper::toResponseDTO)
                .orElseThrow(() -> new SuggestionNotFoundException("Suggestion not found with id: " + id));
    }

    @Transactional
    public SuggestionResponseDTO createSuggestion(CreateSuggestionRequest request) {
        log.info("Creating suggestion for match: {} on market: {}", request.matchId(), request.market());
        Match match = matchRepository.findById(request.matchId())
                .orElseThrow(() -> new MatchNotFoundException("Match not found with id: " + request.matchId()));

        BetSuggestion suggestion = suggestionMapper.toEntity(request);
        suggestion.setMatch(match);
        return suggestionMapper.toResponseDTO(suggestionRepository.save(suggestion));
    }

    @Transactional
    public SuggestionResponseDTO updateSuggestionResult(UUID id, SuggestionResultRequest request) {
        log.info("Updating suggestion result for id: {} to {}", id, request.actualResult());
        BetSuggestion suggestion = suggestionRepository.findById(id)
                .orElseThrow(() -> new SuggestionNotFoundException("Suggestion not found with id: " + id));

        suggestion.setStatus(request.actualResult());
        suggestion.setActualResult(request.actualResult().name());
        suggestion.setRoi(calculateROIValue(suggestion));

        return suggestionMapper.toResponseDTO(suggestionRepository.save(suggestion));
    }

    public Double calculateROI(UUID id) {
        BetSuggestion suggestion = suggestionRepository.findById(id)
                .orElseThrow(() -> new SuggestionNotFoundException("Suggestion not found with id: " + id));
        return calculateROIValue(suggestion);
    }

    private Double calculateROIValue(BetSuggestion suggestion) {
        if (suggestion.getStatus() == SuggestionStatus.PENDING || suggestion.getStatus() == null) {
            return 0.0;
        }

        if (suggestion.getStatus() == SuggestionStatus.VOID) {
            return 0.0;
        }

        BigDecimal stake = suggestion.getStake() != null ? suggestion.getStake() : new BigDecimal("100.00");
        BigDecimal odd = suggestion.getPickedOdd();

        if (suggestion.getStatus() == SuggestionStatus.WON) {
            // ROI = (stake * odd - stake) / stake * 100
            BigDecimal profit = stake.multiply(odd).subtract(stake);
            return profit.divide(stake, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).doubleValue();
        } else if (suggestion.getStatus() == SuggestionStatus.LOST) {
            return -100.0;
        }

        return 0.0;
    }

    @Transactional(readOnly = true)
    public List<SuggestionResponseDTO> getSuggestionsByMatch(UUID matchId) {
        return suggestionRepository.findByMatchId(matchId).stream()
                .map(suggestionMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}