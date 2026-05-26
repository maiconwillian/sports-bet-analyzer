package com.betanalyzer.application;

import com.betanalyzer.application.dto.MatchResponseDTO;
import com.betanalyzer.application.dto.request.CreateMatchRequest;
import com.betanalyzer.application.dto.request.UpdateMatchRequest;
import com.betanalyzer.application.mapper.MatchMapper;
import com.betanalyzer.application.service.DataQualityValidator;
import com.betanalyzer.domain.enums.SupportedLeague;
import com.betanalyzer.domain.model.League;
import com.betanalyzer.domain.model.Match;
import com.betanalyzer.domain.model.Team;
import com.betanalyzer.infrastructure.client.ApiFootballClient;
import com.betanalyzer.infrastructure.client.dto.FixtureDTO;
import com.betanalyzer.infrastructure.persistence.*;
import com.betanalyzer.shared.exception.MatchNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {

    private final MatchRepository matchRepository;
    private final BetSuggestionRepository suggestionRepository;
    private final OddsRepository oddsRepository;
    private final MatchStatsRepository matchStatsRepository;
    private final MatchMapper matchMapper;
    private final ApiFootballClient apiFootballClient;
    private final LeagueRepository leagueRepository;
    private final TeamRepository teamRepository;
    private final DataQualityValidator dataQualityValidator;

    @Transactional(readOnly = true)
    public Page<MatchResponseDTO> getAllMatches(Pageable pageable) {
        return matchRepository.findAll(pageable).map(matchMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public MatchResponseDTO getMatchById(UUID id) {
        return matchRepository.findById(id)
                .map(matchMapper::toResponseDTO)
                .orElseThrow(() -> new MatchNotFoundException("Match not found with id: " + id));
    }

    @Transactional
    public MatchResponseDTO createMatch(CreateMatchRequest request) {
        log.info("Creating match: {} vs {}", request.homeTeam(), request.awayTeam());
        Match match = matchMapper.toEntity(request);
        return matchMapper.toResponseDTO(matchRepository.save(match));
    }

    @Transactional
    public MatchResponseDTO updateMatch(UUID id, UpdateMatchRequest request) {
        log.info("Updating match status for id: {}", id);
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new MatchNotFoundException("Match not found with id: " + id));
        matchMapper.updateEntityFromRequest(request, match);
        return matchMapper.toResponseDTO(matchRepository.save(match));
    }

    @Transactional
    public void deleteMatch(UUID id) {
        log.info("Deleting match with id: {}", id);
        
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new MatchNotFoundException("Match not found with id: " + id));
        
        // Deleta em ordem (respeita FK constraints)
        // 1. Deletar sugestões
        suggestionRepository.deleteByMatchId(id);
        
        // 2. Deletar odds
        oddsRepository.deleteByMatchId(id);
        
        // 3. Deletar stats
        matchStatsRepository.deleteByMatchId(id);
        
        // 4. Agora pode deletar o match
        matchRepository.delete(match);
        
        log.info("Match {} deleted successfully with all dependencies", id);
    }

    @Transactional(readOnly = true)
    public List<MatchResponseDTO> getMatchesByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        return matchRepository.findByMatchDateBetween(startOfDay, endOfDay).stream()
                .map(matchMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MatchResponseDTO> getMatchesBySupportedLeague(SupportedLeague supportedLeague) {
        log.info("Fetching matches for supported league: {} ({})", supportedLeague, supportedLeague.getCountry());
        return matchRepository.findAll().stream()
                .filter(m -> supportedLeague.matches(m.getLeague().getName(), m.getLeague().getCountry()))
                .map(matchMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MatchResponseDTO> getMatchesByLeague(String leagueName) {
        log.info("Fetching matches for league: {}", leagueName);

        Optional<SupportedLeague> supportedLeague = SupportedLeague.fromEnumName(leagueName)
                .or(() -> SupportedLeague.findByApiName(leagueName));

        if (supportedLeague.isEmpty()) {
            log.warn("League '{}' is not in SupportedLeague enum. Supported: {}",
                leagueName,
                Arrays.stream(SupportedLeague.values())
                    .map(SupportedLeague::name)
                    .collect(Collectors.joining(", ")));
            return List.of();
        }

        return getMatchesBySupportedLeague(supportedLeague.get());
    }

    @Transactional
    public void syncQualityFixtures(LocalDate date, SupportedLeague supportedLeague) {
        log.info("Syncing quality fixtures for league: {} on date: {}", supportedLeague, date);
        
        List<FixtureDTO> qualityFixtures = apiFootballClient.getQualityFixtures(date, supportedLeague);
        int totalFetched = qualityFixtures.size();
        int savedCount = 0;
        int filteredCount = 0;

        for (FixtureDTO dto : qualityFixtures) {
            // Re-validate using the entity mapper quality check
            // First we need the League and Team entities
            League league = leagueRepository.findByApiId(dto.league().id())
                    .orElseGet(() -> leagueRepository.save(League.builder()
                            .apiId(dto.league().id())
                            .name(dto.league().name())
                            .country(dto.league().country())
                            .season(dto.league().season())
                            .build()));

            Team homeTeam = teamRepository.findByApiId(dto.teams().home().id())
                    .orElseGet(() -> teamRepository.save(Team.builder()
                            .apiId(dto.teams().home().id())
                            .name(dto.teams().home().name())
                            .logo(dto.teams().home().logo())
                            .build()));

            Team awayTeam = teamRepository.findByApiId(dto.teams().away().id())
                    .orElseGet(() -> teamRepository.save(Team.builder()
                            .apiId(dto.teams().away().id())
                            .name(dto.teams().away().name())
                            .logo(dto.teams().away().logo())
                            .build()));

            matchMapper.mapWithQualityCheck(dto, league, homeTeam, awayTeam, supportedLeague, dataQualityValidator)
                    .ifPresentOrElse(match -> {
                        matchRepository.findByApiId(match.getApiId())
                                .ifPresentOrElse(
                                        existing -> {
                                            matchMapper.updateEntity(dto, league, homeTeam, awayTeam, existing);
                                            matchRepository.save(existing);
                                        },
                                        () -> matchRepository.save(match)
                                );
                    }, () -> {
                        // This shouldn't normally happen as apiFootballClient.getQualityFixtures already filters,
                        // but it's good for double safety and matching the requirement.
                    });
            savedCount++;
        }
        
        // Note: The totalFetched from getQualityFixtures is already filtered.
        // To get the "filtered" count as requested, we'd need the count from allFixtures.
        // Let's adjust to reflect the requirement "Fetched 50 fixtures, kept 32, filtered 18"
        
        List<FixtureDTO> allFixtures = apiFootballClient.getFixturesByDate(date);
        int totalRaw = (int) allFixtures.stream()
                .filter(f -> f.league().name().equalsIgnoreCase(supportedLeague.getApiName()))
                .count();
        filteredCount = totalRaw - savedCount;
        double noisePercent = totalRaw > 0 ? (filteredCount * 100.0 / totalRaw) : 0;

        log.info("Fetched {} fixtures, kept {}, filtered {} ({}% ruído)", totalRaw, savedCount, filteredCount, String.format("%.0f", noisePercent));
    }
}