package com.betanalyzer.application;

import com.betanalyzer.application.dto.RoiReportDTO;
import com.betanalyzer.domain.enums.SuggestionStatus;
import com.betanalyzer.domain.model.BetSuggestion;
import com.betanalyzer.infrastructure.persistence.BetSuggestionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private BetSuggestionRepository suggestionRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    void shouldCalculateROICorrectly() {
        // given
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();
        
        BetSuggestion s1 = BetSuggestion.builder()
                .status(SuggestionStatus.WON)
                .stake(new BigDecimal("100.00"))
                .pickedOdd(new BigDecimal("2.00"))
                .build(); // Lucro 100
        
        BetSuggestion s2 = BetSuggestion.builder()
                .status(SuggestionStatus.LOST)
                .stake(new BigDecimal("100.00"))
                .pickedOdd(new BigDecimal("1.50"))
                .build(); // Lucro -100
        
        BetSuggestion s3 = BetSuggestion.builder()
                .status(SuggestionStatus.WON)
                .stake(new BigDecimal("200.00"))
                .pickedOdd(new BigDecimal("1.50"))
                .build(); // Lucro 100
        
        when(suggestionRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of(s1, s2, s3));

        // when
        RoiReportDTO result = reportService.getROIReport(from, to);

        // then
        // Total Profit = 100 - 100 + 100 = 100
        // Total Stake = 100 + 100 + 200 = 400
        // ROI = 100 / 400 * 100 = 25%
        // Winrate = 2 won / 3 non-void = 66.67%
        
        assertThat(result.getTotalLucro()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(result.getTotalROI()).isEqualTo(25.0);
        assertThat(result.getWinrate()).isCloseTo(66.67, org.assertj.core.data.Offset.offset(0.01));
        assertThat(result.getTotalSuggestions()).isEqualTo(3L);
    }
}