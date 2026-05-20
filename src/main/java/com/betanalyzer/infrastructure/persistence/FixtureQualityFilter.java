package com.betanalyzer.infrastructure.persistence;

import com.betanalyzer.domain.enums.SupportedLeague;
import com.betanalyzer.domain.enums.SupportedMarket;
import com.betanalyzer.domain.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface FixtureQualityFilter extends JpaRepository<Match, UUID> {

    @Query("SELECT m FROM Match m WHERE m.league.name = :#{#league.apiName} AND :threshold <= :#{#league.dataQualityScore}")
    List<Match> findByLeagueAndQualityScore(@Param("league") SupportedLeague league, @Param("threshold") int threshold);

    @Query("SELECT m FROM Match m WHERE :threshold <= :#{#market.modelingScore}")
    List<Match> findByMarketAndQualityScore(@Param("market") SupportedMarket market, @Param("threshold") int threshold);

    @Query("SELECT m FROM Match m WHERE m.matchDate BETWEEN :start AND :end AND m.league.name IN :leagueNames")
    List<Match> findHighQualityMatches(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("leagueNames") List<String> leagueNames);
}
