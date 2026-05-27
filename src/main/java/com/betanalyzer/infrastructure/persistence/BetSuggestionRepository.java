package com.betanalyzer.infrastructure.persistence;

import com.betanalyzer.domain.enums.SuggestionStatus;
import com.betanalyzer.domain.model.BetSuggestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BetSuggestionRepository extends JpaRepository<BetSuggestion, UUID> {

    @EntityGraph(attributePaths = {"match", "match.homeTeam", "match.awayTeam", "match.league"})
    Page<BetSuggestion> findByStatusAndCreatedAtBetween(
            SuggestionStatus status, LocalDateTime start, LocalDateTime end, Pageable pageable);

    @EntityGraph(attributePaths = {"match", "match.homeTeam", "match.awayTeam", "match.league"})
    Page<BetSuggestion> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    @EntityGraph(attributePaths = {"match", "match.homeTeam", "match.awayTeam", "match.league"})
    List<BetSuggestion> findByMatchId(UUID matchId);

    List<BetSuggestion> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    @EntityGraph(attributePaths = {"match", "match.homeTeam", "match.awayTeam", "match.league"})
    List<BetSuggestion> findByStatus(SuggestionStatus status);

    @EntityGraph(attributePaths = {"match", "match.homeTeam", "match.awayTeam", "match.league"})
    List<BetSuggestion> findByStatusAndCreatedAtBetweenOrderByCreatedAtDesc(
            SuggestionStatus status, LocalDateTime start, LocalDateTime end);

    @EntityGraph(attributePaths = {"match", "match.homeTeam", "match.awayTeam", "match.league"})
    Page<BetSuggestion> findByStatusInAndCreatedAtBetween(
            List<SuggestionStatus> statuses, LocalDateTime start, LocalDateTime end, Pageable pageable);

    long countByStatusAndCreatedAtBetween(SuggestionStatus status, LocalDateTime start, LocalDateTime end);

    boolean existsByMatch_IdAndMarketAndStatusInAndCreatedAtBetween(
            UUID matchId,
            String market,
            List<SuggestionStatus> statuses,
            LocalDateTime start,
            LocalDateTime end);

    void deleteByMatchId(UUID matchId);
}
