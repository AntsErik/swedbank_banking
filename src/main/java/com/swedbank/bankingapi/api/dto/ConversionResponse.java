package com.swedbank.bankingapi.api.dto;

import com.swedbank.bankingapi.domain.CurrencyCode;
import io.swagger.v3.oas.annotations.media.Schema;

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
@Schema(description = "Result of a currency conversion operation")
public record ConversionResponse(
    @Schema(description = "Original amount before conversion", example = "100.00")
    BigDecimal originalAmount,
    
    @Schema(description = "Source currency code", example = "USD")
    CurrencyCode originalCurrency,
    
    @Schema(description = "Amount after conversion", example = "92.84")
    BigDecimal convertedAmount,
    
    @Schema(description = "Target currency code", example = "EUR")
    CurrencyCode targetCurrency,
    
    @Schema(description = "Exchange rate used (target units per source unit)", example = "0.9284")
    BigDecimal exchangeRate
) {
}
