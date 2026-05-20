package com.betanalyzer.application;

import com.betanalyzer.application.dto.RoiReportDTO;
import com.betanalyzer.domain.enums.SuggestionStatus;
import com.betanalyzer.domain.model.BetSuggestion;
import com.betanalyzer.infrastructure.persistence.BetSuggestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final BetSuggestionRepository suggestionRepository;

    @Transactional(readOnly = true)
    public RoiReportDTO getROIReport(LocalDate from, LocalDate to) {
        log.info("Generating ROI report from {} to {}", from, to);
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);
        List<BetSuggestion> suggestions = suggestionRepository.findByCreatedAtBetween(start, end);
        
        return calculateReport(suggestions, from + " to " + to);
    }

    @Transactional(readOnly = true)
    public RoiReportDTO getDailyROI(LocalDate date) {
        return getROIReport(date, date);
    }

    @Transactional(readOnly = true)
    public RoiReportDTO getMonthlyROI(YearMonth month) {
        return getROIReport(month.atDay(1), month.atEndOfMonth());
    }

    @Transactional(readOnly = true)
    public Map<SuggestionStatus, Long> getStatsByStatus() {
        return suggestionRepository.findAll().stream()
                .collect(Collectors.groupingBy(BetSuggestion::getStatus, Collectors.counting()));
    }

    private RoiReportDTO calculateReport(List<BetSuggestion> suggestions, String period) {
        long totalCount = suggestions.size();
        if (totalCount == 0) {
            return RoiReportDTO.builder()
                    .totalROI(0.0)
                    .totalLucro(BigDecimal.ZERO)
                    .winrate(0.0)
                    .totalSuggestions(0L)
                    .wonCount(0L)
                    .lostCount(0L)
                    .voidCount(0L)
                    .period(period)
                    .build();
        }

        long wonCount = suggestions.stream().filter(s -> s.getStatus() == SuggestionStatus.WON).count();
        long lostCount = suggestions.stream().filter(s -> s.getStatus() == SuggestionStatus.LOST).count();
        long voidCount = suggestions.stream().filter(s -> s.getStatus() == SuggestionStatus.VOID).count();

        BigDecimal totalProfit = BigDecimal.ZERO;
        BigDecimal totalStake = BigDecimal.ZERO;

        for (BetSuggestion s : suggestions) {
            BigDecimal stake = s.getStake() != null ? s.getStake() : new BigDecimal("100.00");
            if (s.getStatus() == SuggestionStatus.WON) {
                BigDecimal profit = stake.multiply(s.getPickedOdd()).subtract(stake);
                totalProfit = totalProfit.add(profit);
                totalStake = totalStake.add(stake);
            } else if (s.getStatus() == SuggestionStatus.LOST) {
                totalProfit = totalProfit.subtract(stake);
                totalStake = totalStake.add(stake);
            } else if (s.getStatus() == SuggestionStatus.VOID) {
                // Void não conta para ROI no denominador geralmente ou conta neutro. 
                // Seguindo a lógica do ROI = lucro/stake_total.
                totalStake = totalStake.add(stake);
            }
        }

        Double roi = totalStake.compareTo(BigDecimal.ZERO) > 0 
                ? totalProfit.divide(totalStake, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).doubleValue()
                : 0.0;
        
        long nonVoidCount = totalCount - voidCount;
        Double winrate = nonVoidCount > 0 ? (double) wonCount / nonVoidCount * 100 : 0.0;

        return RoiReportDTO.builder()
                .totalROI(roi)
                .totalLucro(totalProfit.setScale(2, RoundingMode.HALF_UP))
                .winrate(winrate)
                .totalSuggestions(totalCount)
                .wonCount(wonCount)
                .lostCount(lostCount)
                .voidCount(voidCount)
                .period(period)
                .build();
    }
}