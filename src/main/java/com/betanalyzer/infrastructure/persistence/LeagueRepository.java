package com.betanalyzer.infrastructure.persistence;

import com.betanalyzer.domain.model.League;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeagueRepository extends JpaRepository<League, UUID> {
    Optional<League> findByApiId(Long apiId);
    Optional<League> findByNameAndSeason(String name, Integer season);
}