package com.betanalyzer.infrastructure.persistence;

import com.betanalyzer.domain.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamRepository extends JpaRepository<Team, UUID> {
    Optional<Team> findByApiId(Long apiId);
    Optional<Team> findByName(String name);
}