package com.betanalyzer.application.mapper;

import com.betanalyzer.application.dto.OddsResponseDTO;
import com.betanalyzer.application.dto.request.OddsRequestDTO;
import com.betanalyzer.domain.model.Odds;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OddsMapper {

    @Mapping(target = "matchId", source = "match.id")
    OddsResponseDTO toResponseDTO(Odds odds);

    List<OddsResponseDTO> toResponseDTOList(List<Odds> odds);

    @Mapping(target = "match", ignore = true)
    @Mapping(target = "capturedAt", expression = "java(java.time.LocalDateTime.now())")
    Odds toEntity(OddsRequestDTO request);
}