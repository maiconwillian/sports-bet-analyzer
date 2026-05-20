package com.betanalyzer.domain.model;

import com.betanalyzer.domain.enums.SuggestionStatus;
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
@Table(name = "bet_suggestions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BetSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @Column(nullable = false)
    private String market;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pickedOdd;

    @Column(nullable = false)
    private String pickedBookmaker;

    @Column(nullable = false)
    private Double confidence;

    @Column(nullable = false)
    private Double expectedValue;

    @Column(precision = 10, scale = 2)
    private BigDecimal stake;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SuggestionStatus status;

    private String actualResult;

    private Double roi;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
