package com.swedbank.bankingapi.domain;

/**
 * Supported currency codes for account balances.
 * Includes major European and international currencies with exchange rate support.
 *
 * @author Ants-Erik Noormagi (AEN)
 * @since v1.0
 */
public enum CurrencyCode {
    /**
     * Euro currency (base currency for this system).
     */
    EUR,

    /**
     * United States Dollar.
     */
    USD,

    /**
     * Swedish Krona.
     */
    SEK,

    /**
     * British Pound Sterling.
     */
    GBP
}
