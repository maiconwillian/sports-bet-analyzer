package com.betanalyzer.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiFootballResponseDTO(
    @JsonProperty("response")
    List<FixtureDTO> fixtures,

    @JsonProperty("errors")
    List<String> errors
) {}
