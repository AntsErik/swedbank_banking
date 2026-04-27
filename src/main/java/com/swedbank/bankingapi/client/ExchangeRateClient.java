package com.swedbank.bankingapi.client;

import com.swedbank.bankingapi.domain.CurrencyCode;
import com.swedbank.bankingapi.domain.ExchangeRate;

import java.util.List;

/**
 * Client interface for fetching exchange rates from external sources.
 * Implementations may fetch from APIs, CSV files, or other data sources.
 *
 * @author Ants-Erik Noormagi (AEN)
 * @since v1.0
 */
public interface ExchangeRateClient {
    /**
     * Fetches current exchange rates for all supported currencies against EUR.
     * EUR is the base currency (1 EUR = 1 EUR rate).
     *
     * @return list of ExchangeRate records, one for each supported currency
     * @throws ExternalFetchException if fetching rates fails
     */
    List<ExchangeRate> fetchExchangeRates();
}
