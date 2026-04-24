package com.swedbank.bankingapi.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * HTTP implementation of the external logging client.
 *
 * @author Ants-Erik Noormagi (AEN)
 * @since v1.0
 */
@Component
public class HttpExternalLoggingClient implements ExternalLoggingClient {

    /**
     * Spring HTTP client used for the outbound logging request.
     */
    private final RestClient restClient;

    /**
     * Configured target URL for the external logging endpoint.
     */
    private final String loggingUrl;

    /**
     * Creates the HTTP-based logging client.
     *
     * @param restClientBuilder Spring REST client builder
     * @param loggingUrl configured external logging URL
     */
    public HttpExternalLoggingClient(RestClient.Builder restClientBuilder,
                                     @Value("${external.logging-url:https://httpstat.us/200}") String loggingUrl) {
        this.restClient = restClientBuilder.build();
        this.loggingUrl = Objects.requireNonNull(loggingUrl);
    }

    /**
     * Executes the configured external logging call for a debit attempt.
     *
     * @param accountId account identifier
     * @param amount debit amount being logged
     */
    @Override
    public void logDebitAttempt(UUID accountId, BigDecimal amount) {
        try {
            HttpStatusCode statusCode = restClient.get()
                .uri(Objects.requireNonNull(loggingUrl))
                .retrieve()
                .toBodilessEntity()
                .getStatusCode();

            if (statusCode.isError()) {
                throw new ExternalLoggingException(
                    "External logging failed for account " + accountId + " with amount " + amount + ". HTTP status: " + statusCode
                );
            }
        } catch (RestClientException ex) {
            throw new ExternalLoggingException(
                "External logging call failed for account " + accountId + " with amount " + amount,
                ex
            );
        }
    }
}
