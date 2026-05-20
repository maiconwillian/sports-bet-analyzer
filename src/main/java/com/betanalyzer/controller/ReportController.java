package com.betanalyzer.controller;

import com.betanalyzer.application.ReportService;
import com.betanalyzer.application.dto.RoiReportDTO;
import com.betanalyzer.domain.enums.SuggestionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/roi")
    public ResponseEntity<RoiReportDTO> getROIReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportService.getROIReport(from, to));
    }

    @GetMapping("/roi/daily")
    public ResponseEntity<RoiReportDTO> getDailyROI(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(reportService.getDailyROI(date));
    }

    @GetMapping("/roi/monthly")
    public ResponseEntity<RoiReportDTO> getMonthlyROI(
            @RequestParam String month) {
        return ResponseEntity.ok(reportService.getMonthlyROI(YearMonth.parse(month)));
    }

    @GetMapping("/status-summary")
    public ResponseEntity<Map<SuggestionStatus, Long>> getStatsByStatus() {
        return ResponseEntity.ok(reportService.getStatsByStatus());
    }
}
