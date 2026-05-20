package com.betanalyzer.application.mapper;

import com.betanalyzer.application.dto.OddsResponseDTO;
import com.betanalyzer.application.dto.request.OddsRequestDTO;
import com.betanalyzer.domain.model.Odds;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OddsMapper {

    OddsResponseDTO toResponseDTO(Odds odds);

    @Mapping(target = "match", ignore = true)
    @Mapping(target = "capturedAt", expression = "java(java.time.LocalDateTime.now())")
    Odds toEntity(OddsRequestDTO request);
}