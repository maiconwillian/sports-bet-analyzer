package com.betanalyzer.application.feature.extractor;

import com.betanalyzer.application.dto.MatchFeatureContextDTO;
import com.betanalyzer.domain.model.Match;
import com.betanalyzer.domain.model.MatchStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class Over25FeatureExtractor {

    /**
     * Extrai features Over 2.5 a partir de MatchStats enriquecidos (Phase 1.75).
     * Confiança para exibição segue {@link com.betanalyzer.application.strategy.Over25Strategy}.
     */
    public MatchFeatureContextDTO extractOver25Features(Match match, MatchStats stats) {
        double homeScored = safe(stats.getHomeTeamGoalsAvg());
        double awayScored = safe(stats.getAwayTeamGoalsAvg());
        double homeConceded = safe(stats.getHomeTeamGoalsConcededAvg());
        double awayConceded = safe(stats.getAwayTeamGoalsConcededAvg());

        double combinedGoalAverage = (homeScored + awayScored) / 2.0;
        double expectedGoalPressure = homeScored + awayScored + homeConceded + awayConceded;

        String reasoning = stats.hasUsableEnrichment()
                ? String.format(
                "Médias: casa %.2f marcados / %.2f sofridos, fora %.2f / %.2f. Over25%% casa %.0f fora %.0f. Comb. %.2f gols.",
                homeScored, homeConceded, awayScored, awayConceded,
                safe(stats.getHomeOver25Rate()), safe(stats.getAwayOver25Rate()),
                combinedGoalAverage)
                : "Stats não enriquecidos — execute enrich antes do modelo.";

        return MatchFeatureContextDTO.builder()
                .homeTeamName(match.getHomeTeam().getName())
                .awayTeamName(match.getAwayTeam().getName())
                .leagueName(match.getLeague().getName())
                .matchDate(match.getMatchDate())
                .homeAvgGoalsScored(homeScored)
                .homeAvgGoalsConceded(homeConceded)
                .awayAvgGoalsScored(awayScored)
                .awayAvgGoalsConceded(awayConceded)
                .homeOver25Rate(stats.getHomeOver25Rate())
                .awayOver25Rate(stats.getAwayOver25Rate())
                .combinedGoalAverage(combinedGoalAverage)
                .expectedGoalPressure(expectedGoalPressure)
                .homeAttackScore(homeScored)
                .awayDefensiveWeakness(awayConceded)
                .reasoning(reasoning)
                .build();
    }

    private static double safe(Double value) {
        return value != null ? value : 0.0;
    }
}
