package com.swedbank.bankingapi.client;

import com.swedbank.bankingapi.domain.CurrencyCode;
import com.swedbank.bankingapi.domain.ExchangeRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Exchange rate client that fetches current rates from Swedbank.
 * Provides rates for USD, SEK, GBP, and other supported currencies against EUR.
 *
 * This implementation uses hardcoded rates as a fallback but can be extended
 * to fetch from Swedbank's API or web pages dynamically.
 *
 * Exchange rate source:
 * https://www.swedbank.ee/private/d2d/payments2/rates/currencyExchange
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
            Map.entry(CurrencyCode.GBP, new BigDecimal("0.86803")));

    /**
     * Inverse rates for converting FROM EUR to other currencies.
     * Format: 1 EUR = X currency units (sell rate used for conversion).
     */
    private static final Map<CurrencyCode, BigDecimal> FALLBACK_INVERSE_RATES = Map.ofEntries(
            Map.entry(CurrencyCode.EUR, BigDecimal.ONE),
            Map.entry(CurrencyCode.USD, new BigDecimal("0.8538")), // 1 / 1.1712
            Map.entry(CurrencyCode.SEK, new BigDecimal("0.9242")), // 1 / 10.82
            Map.entry(CurrencyCode.GBP, new BigDecimal("1.1521")) // 1 / 0.86803
    );

    public SwebankExchangeRateClient(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Fetches exchange rates for all supported currencies from a CSV file.
     * Falls back to hardcoded constants if the file cannot be read.
     *
     * @return list of exchange rates for all non-EUR currencies
     */
    @Override
    public List<ExchangeRate> fetchExchangeRates() {
        log.info("Fetching exchange rates from local CSV file");
        List<ExchangeRate> rates = new ArrayList<>();
        Instant now = Instant.now();
        String fileName = "currency-exchange-rates-2026-04-24.csv";

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            if (is == null) {
                log.error("CSV file not found in resources: {}", fileName);
                return getFallbackRates(now);
            }

            rates = reader.lines()
                    .skip(1)
                    .map(line -> parseLine(line, now))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();

        } catch (Exception e) {
            log.error("Error reading CSV file, falling back to hardcoded rates", e);
            return getFallbackRates(now);
        }

        return rates;
    }

    /**
     * Parses a single CSV line into an ExchangeRate object.
     * Expected format: CurrencyCode, ToEurRate, FromEurRate
     *
     * @param line the raw CSV line
     * @param now  the timestamp to associate with the rate
     * @return Optional containing the ExchangeRate, or empty if parsing fails
     */
    private Optional<ExchangeRate> parseLine(String line, Instant now) {
        try {
            String[] columns = line.split(",");
            CurrencyCode code = CurrencyCode.valueOf(columns[0].trim());
            BigDecimal toEur = new BigDecimal(columns[1].trim());
            BigDecimal fromEur = new BigDecimal(columns[2].trim());

            return Optional.of(new ExchangeRate(code, toEur, fromEur, now));
        } catch (Exception e) {
            log.warn("Failed to parse CSV line: {}", line);
            return Optional.empty();
        }
    }

    /**
     * Provides hardcoded exchange rates as a safety net.
     *
     * @param now the timestamp to associate with the rates
     * @return static list of default exchange rates
     */
    private List<ExchangeRate> getFallbackRates(Instant now) {
        log.warn("Using hardcoded fallback rates as CSV or API was unavailable");
        return List.of(
                new ExchangeRate(CurrencyCode.USD, FALLBACK_RATES.get(CurrencyCode.USD),
                        FALLBACK_INVERSE_RATES.get(CurrencyCode.USD), now),
                new ExchangeRate(CurrencyCode.SEK, FALLBACK_RATES.get(CurrencyCode.SEK),
                        FALLBACK_INVERSE_RATES.get(CurrencyCode.SEK), now),
                new ExchangeRate(CurrencyCode.GBP, FALLBACK_RATES.get(CurrencyCode.GBP),
                        FALLBACK_INVERSE_RATES.get(CurrencyCode.GBP), now));
    }
}
