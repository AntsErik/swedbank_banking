package com.swedbank.bankingapi.api.dto;

import com.swedbank.bankingapi.domain.CurrencyCode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * API request containing a monetary amount and target currency.
 *
 * @param amount positive amount to process
 * @param currency currency code (EUR, USD, SEK, GBP)
 * @author Ants-Erik Noormagi (AEN)
 * @since v1.0
 */
public record MoneyRequest(
        @NotNull
        @DecimalMin(value = "0.01", inclusive = true)
        BigDecimal amount,

        @NotNull
        CurrencyCode currency
) {
}
