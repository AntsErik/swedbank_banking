package com.swedbank.bankingapi.client;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Contract for simulating the required external logging call before a debit is executed.
 *
 * @author Ants-Erik Noormagi (AEN)
 * @since v1.0
 */
public interface ExternalLoggingClient {

    /**
     * Sends a debit attempt log to an external HTTP endpoint.
     *
     * @param accountId account identifier
     * @param amount requested debit amount
     */
    void logDebitAttempt(UUID accountId, BigDecimal amount);
}
