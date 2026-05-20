package com.betanalyzer.application.mapper;

import com.betanalyzer.domain.model.League;
import com.betanalyzer.infrastructure.client.dto.FixtureDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LeagueMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "apiId", source = "id")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "matches", ignore = true)
    League mapApiDtoToEntity(FixtureDTO.LeagueInfo leagueInfo);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "apiId", source = "id")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "matches", ignore = true)
    void updateEntity(FixtureDTO.LeagueInfo leagueInfo, @MappingTarget League league);
}