package com.swedbank.bankingapi.client;

import com.swedbank.bankingapi.domain.CurrencyCode;
import com.swedbank.bankingapi.domain.ExchangeRate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withinPercentage;

/**
 * Tests for {@link SwedbankExchangeRateClient}.
 * Verifies that currency rates are correctly loaded from the CSV resource.
 *
 * @author Ants-Erik Noormagi (AEN)
 * @since v1.0
 */
class SwedbankExchangeRateClientTest {

    private SwedbankExchangeRateClient client;

    @BeforeEach
    void setUp() {
        RestClient restClient = Mockito.mock(RestClient.class);
        client = new SwedbankExchangeRateClient(restClient);
    }

    @Test
    void fetchExchangeRatesShouldLoadAndParseRequiredCurrenciesFromCsv() {
        // Act
        List<ExchangeRate> rates = client.fetchExchangeRates();

        // Assert: Filter list to only the ones we care about
        List<CurrencyCode> foundCodes = rates.stream()
                .map(ExchangeRate::currency)
                .filter(code -> code == CurrencyCode.USD || code == CurrencyCode.SEK || code == CurrencyCode.GBP)
                .toList();

        assertThat(foundCodes).containsExactlyInAnyOrder(
                CurrencyCode.USD,
                CurrencyCode.SEK,
                CurrencyCode.GBP);

        // Assert: Verify USD logic (ECB Quote 1.1712 -> Inverse ~0.8538)
        // This proves the 1/x math is happening on the CSV data
        ExchangeRate usdRate = findRate(rates, CurrencyCode.USD);
        assertThat(usdRate.fromEurRate()).isEqualByComparingTo("1.1712");
        assertThat(usdRate.toEurRate()).isCloseTo(new BigDecimal("0.853825"), withinPercentage(0.01));

        // Assert: Verify SEK logic (ECB Quote 10.82 -> Inverse ~0.09242)
        // This proves the semicolon/comma parsing and decimal precision are correct
        ExchangeRate sekRate = findRate(rates, CurrencyCode.SEK);
        assertThat(sekRate.fromEurRate()).isEqualByComparingTo("10.82");
        assertThat(sekRate.toEurRate()).isCloseTo(new BigDecimal("0.092421"), withinPercentage(0.01));
    }

    private ExchangeRate findRate(List<ExchangeRate> rates, CurrencyCode code) {
        return rates.stream()
                .filter(r -> r.currency() == code)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Required rate not found: " + code));
    }
}