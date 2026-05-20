package com.betanalyzer.application.mapper;

import com.betanalyzer.application.dto.MatchResponseDTO;
import com.betanalyzer.application.dto.request.CreateMatchRequest;
import com.betanalyzer.application.dto.request.UpdateMatchRequest;
import com.betanalyzer.domain.model.Match;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MatchMapper {

    MatchResponseDTO toResponseDTO(Match match);

    @Mapping(target = "status", constant = "SCHEDULED")
    Match toEntity(CreateMatchRequest request);

    void updateEntityFromRequest(UpdateMatchRequest request, @MappingTarget Match match);
}