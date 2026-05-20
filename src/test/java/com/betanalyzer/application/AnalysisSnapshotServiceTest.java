package com.betanalyzer.application;

import com.betanalyzer.application.dto.MatchFeatureContextDTO;
import com.betanalyzer.domain.model.Match;
import com.betanalyzer.domain.model.MatchAnalysisSnapshot;
import com.betanalyzer.infrastructure.persistence.MatchAnalysisSnapshotRepository;
import com.betanalyzer.infrastructure.persistence.MatchRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalysisSnapshotServiceTest {

    @Mock
    private MatchAnalysisSnapshotRepository snapshotRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AnalysisSnapshotService snapshotService;

    @Test
    void shouldSaveAnalysisSnapshotSuccessfully() throws JsonProcessingException {
        // given
        UUID matchId = UUID.randomUUID();
        MatchFeatureContextDTO features = MatchFeatureContextDTO.builder()
                .homeTeamName("Flamengo")
                .awayTeamName("Vasco")
                .confidence(75.0)
                .reasoning("Strong attack")
                .build();
        
        Match match = Match.builder().id(matchId).build();
        String featuresJson = "{\"homeTeamName\":\"Flamengo\"}";

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(objectMapper.writeValueAsString(features)).thenReturn(featuresJson);

        // when
        snapshotService.saveAnalysisSnapshot(matchId, features, "V1", 75.0, "Reasoning");

        // then
        verify(snapshotRepository).save(any(MatchAnalysisSnapshot.class));
    }

    @Test
    void shouldGetAnalysisSnapshotSuccessfully() {
        // given
        UUID matchId = UUID.randomUUID();
        String version = "V1";
        MatchAnalysisSnapshot snapshot = MatchAnalysisSnapshot.builder()
                .strategyVersion(version)
                .features("json")
                .build();

        when(snapshotRepository.findByMatchIdAndStrategyVersion(matchId, version))
                .thenReturn(Optional.of(snapshot));

        // when
        MatchAnalysisSnapshot result = snapshotService.getAnalysisSnapshot(matchId, version);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getStrategyVersion()).isEqualTo(version);
    }

    @Test
    void shouldGetAnalysisHistory() {
        // given
        UUID matchId = UUID.randomUUID();
        List<MatchAnalysisSnapshot> history = List.of(
                MatchAnalysisSnapshot.builder().strategyVersion("V2").build(),
                MatchAnalysisSnapshot.builder().strategyVersion("V1").build()
        );

        when(snapshotRepository.findByMatchIdOrderByGeneratedAtDesc(matchId)).thenReturn(history);

        // when
        List<MatchAnalysisSnapshot> result = snapshotService.getAnalysisHistory(matchId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getStrategyVersion()).isEqualTo("V2");
    }

    @Test
    void shouldDeserializeFeaturesSuccessfully() throws JsonProcessingException {
        // given
        String json = "{\"homeTeamName\":\"Flamengo\"}";
        MatchFeatureContextDTO features = MatchFeatureContextDTO.builder().homeTeamName("Flamengo").build();
        
        when(objectMapper.readValue(json, MatchFeatureContextDTO.class)).thenReturn(features);

        // when
        MatchFeatureContextDTO result = snapshotService.deserializeFeatures(json);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getHomeTeamName()).isEqualTo("Flamengo");
    }
}
