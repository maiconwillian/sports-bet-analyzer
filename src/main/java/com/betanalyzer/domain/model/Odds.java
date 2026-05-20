package com.betanalyzer.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "odds")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Odds {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @Column(nullable = false)
    private String bookmaker;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal homeWinOdd;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal drawOdd;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal awayWinOdd;

    @Column(nullable = false)
    private LocalDateTime capturedAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
