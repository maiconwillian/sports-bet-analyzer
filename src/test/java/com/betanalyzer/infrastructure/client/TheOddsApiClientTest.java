package com.betanalyzer.infrastructure.client;

import com.betanalyzer.config.TheOddsApiProperties;
import com.betanalyzer.infrastructure.client.dto.TheOddsResponseDTO;
import com.betanalyzer.shared.exception.ApiIntegrationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TheOddsApiClientTest {

    private TheOddsApiClient theOddsApiClient;
    private TheOddsApiProperties properties;
    private WebClient webClient;
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    private WebClient.ResponseSpec responseSpec;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        properties = new TheOddsApiProperties();
        properties.setBaseUrl("http://api.test");
        properties.setKey("test-key");

        webClient = mock(WebClient.class);
        requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(java.util.function.Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

        theOddsApiClient = new TheOddsApiClient(webClient, properties);
    }

    @Test
    void shouldFetchOddsForMatch() {
        // given
        TheOddsResponseDTO responseDTO = new TheOddsResponseDTO(
                "match-1", "soccer_brazil", "Soccer", "2026-05-20T19:00:00Z",
                "Flamengo", "Vasco", List.of()
        );

        when(responseSpec.bodyToFlux(TheOddsResponseDTO.class)).thenReturn(Flux.just(responseDTO));

        // when
        List<TheOddsResponseDTO> result = theOddsApiClient.getOddsForMatch("soccer_brazil", LocalDate.parse("2026-05-20"));

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).homeTeam()).isEqualTo("Flamengo");
    }

    @Test
    void shouldParseOver25Correctly() {
        // given
        TheOddsResponseDTO.OutcomeDTO outcome = new TheOddsResponseDTO.OutcomeDTO("Over", 2.5, 1.95);
        TheOddsResponseDTO.MarketDTO market = new TheOddsResponseDTO.MarketDTO("totals", List.of(outcome));
        TheOddsResponseDTO.BookmakerDTO bookmaker = new TheOddsResponseDTO.BookmakerDTO("bet365", "Bet365", List.of(market));
        TheOddsResponseDTO responseDTO = new TheOddsResponseDTO(
                "match-1", "soccer_brazil", "Soccer", "2026-05-20T19:00:00Z",
                "Flamengo RJ", "Vasco da Gama", List.of(bookmaker)
        );

        when(responseSpec.bodyToFlux(TheOddsResponseDTO.class)).thenReturn(Flux.just(responseDTO));

        // when
        Double odd = theOddsApiClient.getOver25Odd("soccer_brazil", LocalDate.parse("2026-05-20"), "Flamengo", "Vasco", "bet365");

        // then
        assertThat(odd).isEqualTo(1.95);
    }

    @Test
    void shouldThrowExceptionWhenApiFails() {
        // given
        when(requestHeadersSpec.retrieve()).thenThrow(new RuntimeException("API error"));

        // when & then
        assertThatThrownBy(() -> theOddsApiClient.getOddsForMatch("soccer_brazil", LocalDate.parse("2026-05-20")))
                .isInstanceOf(ApiIntegrationException.class)
                .hasMessageContaining("Failed to fetch odds");
    }
}
