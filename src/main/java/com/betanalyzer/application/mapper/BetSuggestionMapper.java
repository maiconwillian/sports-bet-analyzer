package com.betanalyzer.application.mapper;

import com.betanalyzer.application.dto.SuggestionResponseDTO;
import com.betanalyzer.application.dto.request.CreateSuggestionRequest;
import com.betanalyzer.domain.model.BetSuggestion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BetSuggestionMapper {

    @Mapping(target = "matchId", source = "match.id")
    SuggestionResponseDTO toResponseDTO(BetSuggestion suggestion);

    @Mapping(target = "match", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    BetSuggestion toEntity(CreateSuggestionRequest request);
}