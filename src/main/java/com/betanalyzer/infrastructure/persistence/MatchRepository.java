package com.betanalyzer.infrastructure.persistence;

import com.betanalyzer.domain.enums.MatchStatus;
import com.betanalyzer.domain.model.League;
import com.betanalyzer.domain.model.Match;
import com.betanalyzer.domain.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MatchRepository extends JpaRepository<Match, UUID> {
    Optional<Match> findByApiId(Long apiId);
    List<Match> findByMatchDateBetween(LocalDateTime start, LocalDateTime end);
    List<Match> findByLeagueAndStatus(League league, MatchStatus status);
    List<Match> findByHomeTeamAndStatus(Team team, MatchStatus status);
    List<Match> findByAwayTeamAndStatus(Team team, MatchStatus status);
    void deleteByApiId(Long apiId);
}
