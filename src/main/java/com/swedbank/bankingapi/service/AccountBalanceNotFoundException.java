package com.swedbank.bankingapi.service;

import java.util.UUID;

import com.swedbank.bankingapi.domain.CurrencyCode;

/**
 * Exception thrown when an account exists but does not have a balance for
 * the specifically requested currency.
 *
 * @author Ants-Erik Noormagi (AEN)
 * @since v1.0
 */
public class AccountBalanceNotFoundException extends RuntimeException {

    /**
     * Creates a new exception for a missing specific currency balance.
     *
     * @param accountId account identifier
     * @param currency  missing currency code
     */
    public AccountBalanceNotFoundException(UUID accountId, CurrencyCode currency) {
        super(String.format("Balance for %s not found for account %s", currency, accountId));
    }
}
