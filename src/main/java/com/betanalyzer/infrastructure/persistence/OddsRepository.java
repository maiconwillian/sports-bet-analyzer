package com.betanalyzer.infrastructure.persistence;

import com.betanalyzer.domain.model.Odds;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OddsRepository extends JpaRepository<Odds, UUID> {
    List<Odds> findByMatchId(UUID matchId);
    Optional<Odds> findFirstByMatchIdOrderByCapturedAtDesc(UUID matchId);
    List<Odds> findByBookmaker(String bookmaker);
    List<Odds> findByMatchIdOrderByCapturedAtAsc(UUID matchId);
}
