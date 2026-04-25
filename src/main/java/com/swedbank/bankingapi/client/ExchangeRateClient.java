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

    /**
     * Fetches the exchange rate for a specific currency.
     *
     * @param currency the currency to fetch the rate for
     * @return ExchangeRate for the given currency
     * @throws ExternalFetchException if fetching fails
     * @throws IllegalArgumentException if currency is EUR (it's always 1.0)
     */
    ExchangeRate fetchExchangeRate(CurrencyCode currency);
}
