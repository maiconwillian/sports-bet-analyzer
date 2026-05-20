package com.betanalyzer.infrastructure.persistence;

import com.betanalyzer.domain.model.MatchAnalysisSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MatchAnalysisSnapshotRepository extends JpaRepository<MatchAnalysisSnapshot, UUID> {
    
    List<MatchAnalysisSnapshot> findByMatchId(UUID matchId);
    
    List<MatchAnalysisSnapshot> findByStrategyVersion(String strategyVersion);
    
    Optional<MatchAnalysisSnapshot> findByMatchIdAndStrategyVersion(UUID matchId, String strategyVersion);
    
    void deleteByMatchId(UUID matchId);
    
    List<MatchAnalysisSnapshot> findByMatchIdOrderByGeneratedAtDesc(UUID matchId);
}
