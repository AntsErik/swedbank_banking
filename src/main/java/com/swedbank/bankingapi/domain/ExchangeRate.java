package com.swedbank.bankingapi.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Represents the exchange rate between a currency and EUR (base currency).
 * Exchange rates are cached and updated periodically from Swedbank.
 *
 * @author Ants-Erik Noormagi (AEN)
 * @since v1.0
 */
public record ExchangeRate(
    /**
     * Currency code for which the rate is defined.
     */
    CurrencyCode currency,

    /**
     * Exchange rate from this currency to EUR (1 unit of currency = rate EUR).
     */
    BigDecimal toEurRate,

    /**
     * Exchange rate from EUR to this currency (1 EUR = rate of this currency).
     */
    BigDecimal fromEurRate,

    /**
     * Timestamp when the rate was fetched from Swedbank.
     */
    Instant lastUpdated
) {
    /**
     * Validates that the exchange rates are positive and reasonable.
     *
     * @throws IllegalArgumentException if rates are invalid
     */
    public ExchangeRate {
        if (toEurRate == null || fromEurRate == null) {
            throw new IllegalArgumentException("Exchange rates cannot be null");
        }
        if (toEurRate.signum() <= 0 || fromEurRate.signum() <= 0) {
            throw new IllegalArgumentException("Exchange rates must be positive");
        }
    }
}
