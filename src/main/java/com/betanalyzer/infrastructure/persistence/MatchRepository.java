package com.betanalyzer.infrastructure.persistence;

import com.betanalyzer.domain.enums.MatchStatus;
import com.betanalyzer.domain.model.League;
import com.betanalyzer.domain.model.Match;
import com.betanalyzer.domain.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MatchRepository extends JpaRepository<Match, UUID> {
    Optional<Match> findByApiId(Long apiId);

    @Query("SELECT m FROM Match m "
            + "JOIN FETCH m.league "
            + "JOIN FETCH m.homeTeam "
            + "JOIN FETCH m.awayTeam "
            + "WHERE m.id = :id")
    Optional<Match> findDetailedById(@Param("id") UUID id);

    List<Match> findByMatchDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT DISTINCT m FROM Match m "
            + "JOIN FETCH m.league "
            + "JOIN FETCH m.homeTeam "
            + "JOIN FETCH m.awayTeam "
            + "WHERE m.matchDate BETWEEN :start AND :end")
    List<Match> findDetailedByMatchDateBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    List<Match> findByLeagueAndStatus(League league, MatchStatus status);
    List<Match> findByHomeTeamAndStatus(Team team, MatchStatus status);
    List<Match> findByAwayTeamAndStatus(Team team, MatchStatus status);
    void deleteByApiId(Long apiId);
}
