package com.swedbank.bankingapi.client;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link HttpExternalLoggingClient}.
 *
 * @author Ants-Erik Noormagi (AEN)
 * @since v1.0
 */
class HttpExternalLoggingClientTest {

    @Test
    void logDebitAttemptSucceedsWhenExternalCallReturnsSuccess() {
        RestClient.Builder builder = mock(RestClient.Builder.class);
        RestClient restClient = mock(RestClient.class, Answers.RETURNS_DEEP_STUBS);
        when(builder.build()).thenReturn(restClient);
        when(restClient.get().uri(anyString()).retrieve().toBodilessEntity()).thenReturn(ResponseEntity.ok().build());

        HttpExternalLoggingClient client = new HttpExternalLoggingClient(builder, "http://localhost/mock");

        assertThatCode(() -> client.logDebitAttempt(UUID.randomUUID(), new BigDecimal("25.00")))
                .doesNotThrowAnyException();
    }

    @Test
    void logDebitAttemptWrapsRestClientExceptions() {
        RestClient.Builder builder = mock(RestClient.Builder.class);
        RestClient restClient = mock(RestClient.class, Answers.RETURNS_DEEP_STUBS);
        when(builder.build()).thenReturn(restClient);
        when(restClient.get().uri(anyString()).retrieve().toBodilessEntity())
                .thenThrow(new RestClientException("boom"));

        HttpExternalLoggingClient client = new HttpExternalLoggingClient(builder, "http://localhost/mock");

        assertThatThrownBy(() -> client.logDebitAttempt(UUID.randomUUID(), new BigDecimal("25.00")))
                .isInstanceOf(ExternalLoggingException.class)
                .hasMessageContaining("External logging call failed");
    }
}