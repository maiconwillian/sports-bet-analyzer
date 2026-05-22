package com.betanalyzer.controller;

import com.betanalyzer.application.backtesting.BacktestingService;
import com.betanalyzer.application.dto.backtesting.BacktestRequest;
import com.betanalyzer.application.dto.backtesting.BacktestResultDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/backtesting")
@RequiredArgsConstructor
public class BacktestingController {

    private final BacktestingService backtestingService;

    @PostMapping("/run")
    public ResponseEntity<BacktestResultDTO> runBacktest(@RequestBody @Valid BacktestRequest request) {
        return ResponseEntity.ok(backtestingService.runBacktest(request));
    }
}
