package com.swedbank.bankingapi.api.dto;

import com.swedbank.bankingapi.domain.CurrencyCode;

import java.math.BigDecimal;

/**
 * Response DTO for currency conversion operations.
 * Contains the original amount/currencies and the converted result.
 *
 * @author Ants-Erik Noormagi (AEN)
 * @since v1.0
 *
 * @param originalAmount the amount that was converted
 * @param originalCurrency source currency code
 * @param convertedAmount result of the conversion
 * @param targetCurrency target currency code
 * @param exchangeRate the rate used for the conversion (how many target units per source unit)
 */
public record ConversionResponse(
    BigDecimal originalAmount,
    CurrencyCode originalCurrency,
    BigDecimal convertedAmount,
    CurrencyCode targetCurrency,
    BigDecimal exchangeRate
) {
}
