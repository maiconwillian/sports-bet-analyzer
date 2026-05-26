package com.betanalyzer.infrastructure.client;

import com.betanalyzer.application.dto.TeamEnrichmentSnapshot;
import com.betanalyzer.infrastructure.client.dto.FixtureDTO;
import com.betanalyzer.infrastructure.client.dto.FixturesListApiDTO;
import com.betanalyzer.infrastructure.client.dto.StandingsApiDTO;
import com.betanalyzer.infrastructure.client.dto.TeamStatisticsApiDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class ApiFootballEnrichmentClient {

    private final WebClient webClient;

    public ApiFootballEnrichmentClient(@Qualifier("footballWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Optional<TeamEnrichmentSnapshot> fetchTeamEnrichment(
            Long teamApiId,
            Long leagueApiId,
            int season,
            int lastFixtures,
            Map<Long, Integer> standingsRankByTeamId
    ) {
        try {
            Optional<TeamStatisticsApiDTO.TeamStatisticsEntry> stats =
                    fetchStatisticsEntry(teamApiId, leagueApiId, season);
            String form = stats
                    .map(TeamStatisticsApiDTO.TeamStatisticsEntry::form)
                    .filter(f -> f != null && !f.isBlank())
                    .map(String::trim)
                    .orElse("N/A");
            double[] goalAvgs = stats.map(this::extractGoalAverages).orElse(new double[] {0.0, 0.0});
            double over25Rate = computeOver25Rate(teamApiId, lastFixtures);
            Integer position = standingsRankByTeamId.get(teamApiId);

            return Optional.of(TeamEnrichmentSnapshot.builder()
                    .form(form)
                    .goalsScoredAvg(goalAvgs[0])
                    .goalsConcededAvg(goalAvgs[1])
                    .over25Rate(over25Rate)
                    .leaguePosition(position)
                    .build());
        } catch (Exception e) {
            log.warn("Failed to enrich team {}: {}", teamApiId, e.getMessage());
            return Optional.empty();
        }
    }

    public Map<Long, Integer> fetchStandingsRanks(Long leagueApiId, int season) {
        Map<Long, Integer> ranks = new HashMap<>();
        try {
            StandingsApiDTO response = webClient.get()
                    .uri(uri -> uri.path("/standings")
                            .queryParam("league", leagueApiId)
                            .queryParam("season", season)
                            .build())
                    .retrieve()
                    .bodyToMono(StandingsApiDTO.class)
                    .block();

            if (response == null || response.response() == null || response.response().isEmpty()) {
                return ranks;
            }
            List<StandingsApiDTO.StandingRow> rows = response.response().get(0).standings() != null
                    && !response.response().get(0).standings().isEmpty()
                    ? response.response().get(0).standings().get(0)
                    : List.of();

            for (StandingsApiDTO.StandingRow row : rows) {
                if (row.team() != null && row.team().id() != null && row.rank() != null) {
                    ranks.put(row.team().id(), row.rank());
                }
            }
        } catch (Exception e) {
            log.warn("Standings unavailable for league {} season {}: {}", leagueApiId, season, e.getMessage());
        }
        return ranks;
    }

    private Optional<TeamStatisticsApiDTO.TeamStatisticsEntry> fetchStatisticsEntry(
            Long teamApiId, Long leagueApiId, int season) {
        TeamStatisticsApiDTO dto = webClient.get()
                .uri(uri -> uri.path("/teams/statistics")
                        .queryParam("team", teamApiId)
                        .queryParam("league", leagueApiId)
                        .queryParam("season", season)
                        .build())
                .retrieve()
                .bodyToMono(TeamStatisticsApiDTO.class)
                .block();

        if (dto == null || dto.response() == null) {
            return Optional.empty();
        }
        return Optional.of(dto.response());
    }

    private double[] extractGoalAverages(TeamStatisticsApiDTO.TeamStatisticsEntry entry) {
        var goals = entry.goals();
        if (goals == null) {
            return new double[] {0.0, 0.0};
        }
        return new double[] {
                parseAverage(goals.forGoals()),
                parseAverage(goals.against())
        };
    }

    private double parseAverage(TeamStatisticsApiDTO.GoalSide side) {
        if (side == null || side.average() == null || side.average().total() == null) {
            return 0.0;
        }
        try {
            return Double.parseDouble(side.average().total().trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private double computeOver25Rate(Long teamApiId, int lastN) {
        FixturesListApiDTO dto = webClient.get()
                .uri(uri -> uri.path("/fixtures")
                        .queryParam("team", teamApiId)
                        .queryParam("last", lastN)
                        .queryParam("status", "FT")
                        .build())
                .retrieve()
                .bodyToMono(FixturesListApiDTO.class)
                .block();

        if (dto == null || dto.response() == null || dto.response().isEmpty()) {
            return 0.0;
        }

        int total = 0;
        int over25 = 0;
        for (FixtureDTO fixture : dto.response()) {
            if (fixture.goals() == null || fixture.goals().home() == null || fixture.goals().away() == null) {
                continue;
            }
            int sum = fixture.goals().home() + fixture.goals().away();
            total++;
            if (sum > 2) {
                over25++;
            }
        }
        if (total == 0) {
            return 0.0;
        }
        return (over25 * 100.0) / total;
    }
}
