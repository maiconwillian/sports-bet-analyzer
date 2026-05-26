package com.betanalyzer.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FixturesListApiDTO(
        @JsonProperty("response")
        List<FixtureDTO> response
) {}
