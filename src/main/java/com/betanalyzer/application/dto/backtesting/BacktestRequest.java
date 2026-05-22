package com.betanalyzer.application.dto.backtesting;

import com.betanalyzer.application.backtesting.SimulationMode;
import com.betanalyzer.domain.enums.SupportedLeague;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BacktestRequest {

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @NotNull
    private SupportedLeague league;

    @NotBlank
    private String strategyVersion;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal stake;

    @Min(0)
    @Max(100)
    private Double minimumConfidence;

    @NotNull
    private SimulationMode simulationMode;
}
