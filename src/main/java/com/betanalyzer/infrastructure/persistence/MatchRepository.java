package com.betanalyzer.infrastructure.persistence;

import com.betanalyzer.domain.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface MatchRepository extends JpaRepository<Match, UUID> {
    List<Match> findByMatchDateBetween(LocalDateTime start, LocalDateTime end);
    List<Match> findByLeague(String league);
}
