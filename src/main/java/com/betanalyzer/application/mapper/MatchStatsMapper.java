package com.betanalyzer.application.mapper;

import com.betanalyzer.application.dto.MatchStatsResponseDTO;
import com.betanalyzer.application.dto.request.MatchStatsRequestDTO;
import com.betanalyzer.domain.model.MatchStats;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MatchStatsMapper {

    @Mapping(target = "matchId", source = "match.id")
    MatchStatsResponseDTO toResponseDTO(MatchStats stats);

    @Mapping(target = "match", ignore = true)
    @Mapping(target = "lastUpdate", expression = "java(java.time.LocalDateTime.now())")
    MatchStats toEntity(MatchStatsRequestDTO request);

    @Mapping(target = "match", ignore = true)
    @Mapping(target = "lastUpdate", expression = "java(java.time.LocalDateTime.now())")
    void updateEntityFromRequest(MatchStatsRequestDTO request, @MappingTarget MatchStats stats);
}