package com.swedbank.bankingapi.client;

import com.swedbank.bankingapi.domain.CurrencyCode;
import com.swedbank.bankingapi.domain.ExchangeRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Exchange rate client that fetches current rates from Swedbank.
 * Provides rates for USD, SEK, GBP, and other supported currencies against EUR.
 *
 * This implementation uses hardcoded rates as a fallback but can be extended
 * to fetch from Swedbank's API or web pages dynamically.
 *
 * Exchange rate source: https://www.swedbank.ee/private/d2d/payments2/rates/currencyExchange
 *
 * @author Ants-Erik Noormagi (AEN)
 * @since v1.0
 */
@Component
public class SwebankExchangeRateClient implements ExchangeRateClient {

    private static final Logger log = LoggerFactory.getLogger(SwebankExchangeRateClient.class);

    private final RestClient restClient;

    /**
     * Hardcoded exchange rates from Swedbank (as of 2026-04-24).
     * These rates use the Euroopa Keskpanga (ECB) reference rates.
     * Format: 1 currency unit = X EUR (buy rate used for conversion).
     */
    private static final Map<CurrencyCode, BigDecimal> FALLBACK_RATES = Map.ofEntries(
            Map.entry(CurrencyCode.EUR, BigDecimal.ONE),
            Map.entry(CurrencyCode.USD, new BigDecimal("1.1712")),
            Map.entry(CurrencyCode.SEK, new BigDecimal("10.82")),
            Map.entry(CurrencyCode.GBP, new BigDecimal("0.86803"))
    );

    /**
     * Inverse rates for converting FROM EUR to other currencies.
     * Format: 1 EUR = X currency units (sell rate used for conversion).
     */
    private static final Map<CurrencyCode, BigDecimal> FALLBACK_INVERSE_RATES = Map.ofEntries(
            Map.entry(CurrencyCode.EUR, BigDecimal.ONE),
            Map.entry(CurrencyCode.USD, new BigDecimal("0.8538")),  // 1 / 1.1712
            Map.entry(CurrencyCode.SEK, new BigDecimal("0.9242")),  // 1 / 10.82
            Map.entry(CurrencyCode.GBP, new BigDecimal("1.1521"))   // 1 / 0.86803
    );

    public SwebankExchangeRateClient(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Fetches exchange rates for all supported currencies.
     * Currently returns hardcoded rates from Swedbank as of 2026-04-24.
     *
     * @return list of exchange rates for all non-EUR currencies
     */
    @Override
    public List<ExchangeRate> fetchExchangeRates() {
        log.debug("Fetching exchange rates from Swedbank");
        Instant now = Instant.now();

        return List.of(
                new ExchangeRate(CurrencyCode.USD, FALLBACK_RATES.get(CurrencyCode.USD),
                        FALLBACK_INVERSE_RATES.get(CurrencyCode.USD), now),
                new ExchangeRate(CurrencyCode.SEK, FALLBACK_RATES.get(CurrencyCode.SEK),
                        FALLBACK_INVERSE_RATES.get(CurrencyCode.SEK), now),
                new ExchangeRate(CurrencyCode.GBP, FALLBACK_RATES.get(CurrencyCode.GBP),
                        FALLBACK_INVERSE_RATES.get(CurrencyCode.GBP), now)
        );
    }

    /**
     * Fetches the exchange rate for a specific currency.
     *
     * @param currency the currency to fetch the rate for
     * @return ExchangeRate record containing both directions of conversion
     * @throws IllegalArgumentException if currency is EUR
     */
    @Override
    public ExchangeRate fetchExchangeRate(CurrencyCode currency) {
        if (currency == CurrencyCode.EUR) {
            throw new IllegalArgumentException("EUR is the base currency and does not have an exchange rate");
        }

        log.debug("Fetching exchange rate for {}", currency);
        Instant now = Instant.now();

        BigDecimal toEur = FALLBACK_RATES.getOrDefault(currency, BigDecimal.ZERO);
        BigDecimal fromEur = FALLBACK_INVERSE_RATES.getOrDefault(currency, BigDecimal.ZERO);

        if (toEur.signum() == 0 || fromEur.signum() == 0) {
            log.warn("Exchange rate not found for currency: {}", currency);
            throw new RestClientException("Exchange rate not available for: " + currency);
        }

        return new ExchangeRate(currency, toEur, fromEur, now);
    }
}
