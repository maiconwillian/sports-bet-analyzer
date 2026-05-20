package com.betanalyzer.application.mapper;

import com.betanalyzer.domain.model.Team;
import com.betanalyzer.infrastructure.client.dto.FixtureDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TeamMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "apiId", source = "id")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "homeMatches", ignore = true)
    @Mapping(target = "awayMatches", ignore = true)
    Team mapApiDtoToEntity(FixtureDTO.TeamInfo teamInfo);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "apiId", source = "id")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "homeMatches", ignore = true)
    @Mapping(target = "awayMatches", ignore = true)
    void updateEntity(FixtureDTO.TeamInfo teamInfo, @MappingTarget Team team);
}