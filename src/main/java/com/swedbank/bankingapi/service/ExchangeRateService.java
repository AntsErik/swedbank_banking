package com.swedbank.bankingapi.service;

import com.swedbank.bankingapi.client.ExchangeRateClient;
import com.swedbank.bankingapi.domain.CurrencyCode;
import com.swedbank.bankingapi.domain.ExchangeRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing exchange rates and performing currency conversions.
 * Provides conversions between any supported currencies via EUR as the base currency.
 *
 * Conversion rates are fetched from Swedbank and cached in memory.
 * All conversions use the ECB reference rates with 2-decimal precision (HALF_EVEN rounding).
 *
 * @author Ants-Erik Noormagi (AEN)
 * @since v1.0
 */
@Service
public class ExchangeRateService {

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateService.class);

    private final ExchangeRateClient exchangeRateClient;
    private Map<CurrencyCode, ExchangeRate> rateCache;

    /**
     * Creates a new exchange rate service.
     *
     * @param exchangeRateClient the client to fetch rates from Swedbank
     */
    public ExchangeRateService(ExchangeRateClient exchangeRateClient) {
        this.exchangeRateClient = exchangeRateClient;
        this.rateCache = refreshRates();
    }

    /**
     * Refreshes the exchange rate cache from Swedbank.
     *
     * @return map of currency codes to exchange rates
     */
    public Map<CurrencyCode, ExchangeRate> refreshRates() {
        log.debug("Refreshing exchange rates cache");
        List<ExchangeRate> rates = exchangeRateClient.fetchExchangeRates();
        this.rateCache = rates.stream()
                .collect(Collectors.toMap(ExchangeRate::currency, rate -> rate));
        log.info("Exchange rates updated: {} currencies cached", rateCache.size());
        return this.rateCache;
    }

    /**
     * Gets the cached exchange rate for a currency.
     *
     * @param currency the currency to get the rate for
     * @return ExchangeRate record
     * @throws IllegalArgumentException if currency not found in cache
     */
    public ExchangeRate getExchangeRate(CurrencyCode currency) {
        if (currency == CurrencyCode.EUR) {
            // EUR is always 1:1 to itself
            return new ExchangeRate(CurrencyCode.EUR, BigDecimal.ONE, BigDecimal.ONE,
                    java.time.Instant.now());
        }
        ExchangeRate rate = rateCache.get(currency);
        if (rate == null) {
            log.warn("Exchange rate not found for: {}", currency);
            throw new IllegalArgumentException("Exchange rate not available for: " + currency);
        }
        return rate;
    }

    /**
     * Converts an amount from one currency to another.
     * Performs conversion via EUR as the intermediate currency.
     *
     * Conversion formula:
     * - If from/to both non-EUR: convert to EUR, then from EUR to target
     * - If from is EUR: multiply by target's fromEurRate
     * - If to is EUR: divide amount by source's toEurRate
     *
     * @param amount the amount to convert
     * @param fromCurrency source currency
     * @param toCurrency target currency
     * @return converted amount with 2-decimal precision (HALF_EVEN rounding)
     * @throws IllegalArgumentException if currencies not supported or amount is negative
     */
    public BigDecimal convert(BigDecimal amount, CurrencyCode fromCurrency, CurrencyCode toCurrency) {
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException("Amount must be non-negative");
        }
        if (fromCurrency == toCurrency) {
            return amount;
        }

        log.debug("Converting {} {} to {}", amount, fromCurrency, toCurrency);

        // If converting from EUR
        if (fromCurrency == CurrencyCode.EUR) {
            ExchangeRate targetRate = getExchangeRate(toCurrency);
            BigDecimal converted = amount.multiply(targetRate.fromEurRate());
            return normalizeAmount(converted);
        }

        // If converting to EUR
        if (toCurrency == CurrencyCode.EUR) {
            ExchangeRate sourceRate = getExchangeRate(fromCurrency);
            BigDecimal converted = amount.multiply(sourceRate.toEurRate());
            return normalizeAmount(converted);
        }

        // Converting between two non-EUR currencies via EUR
        ExchangeRate sourceRate = getExchangeRate(fromCurrency);
        ExchangeRate targetRate = getExchangeRate(toCurrency);

        // Convert source currency to EUR
        BigDecimal inEur = amount.multiply(sourceRate.toEurRate());

        // Convert EUR to target currency
        BigDecimal result = inEur.multiply(targetRate.fromEurRate());

        return normalizeAmount(result);
    }

    /**
     * Normalizes an amount to 2 decimal places using HALF_EVEN rounding.
     * This ensures consistent precision for financial calculations.
     *
     * @param amount the amount to normalize
     * @return amount with exactly 2 decimal places
     */
    private BigDecimal normalizeAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_EVEN);
    }
}
