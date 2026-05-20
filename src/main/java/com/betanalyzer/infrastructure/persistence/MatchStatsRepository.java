package com.betanalyzer.infrastructure.persistence;

import com.betanalyzer.domain.model.MatchStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MatchStatsRepository extends JpaRepository<MatchStats, UUID> {
    Optional<MatchStats> findByMatchId(UUID matchId);
    void deleteByMatchId(UUID matchId);
}
