package com.betanalyzer.infrastructure.client.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TeamStatisticsApiDTOTest {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void shouldDeserializeResponseAsSingleObject() throws Exception {
        String json = """
                {
                  "get": "teams/statistics",
                  "response": {
                    "league": {
                      "id": 2,
                      "name": "UEFA Champions League",
                      "country": "World",
                      "season": 2025
                    },
                    "team": {
                      "id": 85,
                      "name": "Paris Saint Germain"
                    },
                    "form": "WDWWW",
                    "goals": {
                      "for": {
                        "average": {
                          "home": "2.1",
                          "away": "1.8",
                          "total": "1.95"
                        }
                      },
                      "against": {
                        "average": {
                          "home": "0.9",
                          "away": "1.1",
                          "total": "1.00"
                        }
                      }
                    }
                  }
                }
                """;

        TeamStatisticsApiDTO dto = mapper.readValue(json, TeamStatisticsApiDTO.class);

        assertThat(dto.response()).isNotNull();
        assertThat(dto.response().form()).isEqualTo("WDWWW");
        assertThat(dto.response().goals().forGoals().average().total()).isEqualTo("1.95");
        assertThat(dto.response().goals().against().average().total()).isEqualTo("1.00");
    }
}
