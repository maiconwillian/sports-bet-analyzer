package com.betanalyzer.application;

import com.betanalyzer.application.dto.MatchFeatureContextDTO;
import com.betanalyzer.domain.model.Match;
import com.betanalyzer.domain.model.MatchAnalysisSnapshot;
import com.betanalyzer.infrastructure.persistence.MatchAnalysisSnapshotRepository;
import com.betanalyzer.infrastructure.persistence.MatchRepository;
import com.betanalyzer.shared.exception.MatchNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisSnapshotService {

    private final MatchAnalysisSnapshotRepository snapshotRepository;
    private final MatchRepository matchRepository;
    private final ObjectMapper objectMapper;

    public void saveAnalysisSnapshot(
            UUID matchId,
            MatchFeatureContextDTO features,
            String strategyVersion,
            Double confidence,
            String reasoning
    ) {
        log.info("Saving analysis snapshot for match: {} and strategy: {}", matchId, strategyVersion);

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException("Match not found with id: " + matchId));

        try {
            // ✅ MUDANÇA: Converter para JsonNode em vez de String
            JsonNode featuresNode = objectMapper.valueToTree(features);

            MatchAnalysisSnapshot snapshot = MatchAnalysisSnapshot.builder()
                    .match(match)
                    .strategyVersion(strategyVersion)
                    .features(featuresNode)  // ✅ JsonNode
                    .confidence(confidence)
                    .reasoning(reasoning)
                    .build();

            snapshotRepository.save(snapshot);
            log.info("Analysis snapshot saved successfully for match: {}", matchId);
        } catch (Exception e) {
            log.error("Error serializing features to JSON for match: {}", matchId, e);
            throw new RuntimeException("Error serializing analysis features", e);
        }
    }

    @Transactional(readOnly = true)
    public MatchAnalysisSnapshot getAnalysisSnapshot(UUID matchId, String strategyVersion) {
        log.info("Retrieving analysis snapshot for match: {} and strategy: {}", matchId, strategyVersion);
        
        return snapshotRepository.findByMatchIdAndStrategyVersion(matchId, strategyVersion)
                .orElseThrow(() -> new RuntimeException("Analysis snapshot not found for match " + matchId + " and version " + strategyVersion));
    }

    public MatchFeatureContextDTO deserializeFeatures(String featuresJson) {
        try {
            return objectMapper.readValue(featuresJson, MatchFeatureContextDTO.class);
        } catch (JsonProcessingException e) {
            log.error("Error deserializing features from JSON", e);
            throw new RuntimeException("Error deserializing analysis features", e);
        }
    }

    @Transactional(readOnly = true)
    public List<MatchAnalysisSnapshot> getAnalysisHistory(UUID matchId) {
        log.info("Listing analysis history for match: {}", matchId);
        return snapshotRepository.findByMatchIdOrderByGeneratedAtDesc(matchId);
    }
}
