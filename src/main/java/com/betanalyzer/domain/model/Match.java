package com.betanalyzer.domain.model;

import com.betanalyzer.domain.enums.MatchStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "matches")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String homeTeam;

    @Column(nullable = false)
    private String awayTeam;

    @Column(nullable = false)
    private LocalDateTime matchDate;

    @Column(nullable = false)
    private String league;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
