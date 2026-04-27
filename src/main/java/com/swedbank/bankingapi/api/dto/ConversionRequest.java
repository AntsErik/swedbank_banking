package com.swedbank.bankingapi.api.dto;

import com.swedbank.bankingapi.domain.CurrencyCode;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Request DTO for currency conversion operations.
 * Specifies the source/target currencies and amount to convert.
 *
 * @author Ants-Erik Noormagi (AEN)
 * @since v1.0
 *
 * @param amount the amount to convert (must be positive)
 * @param fromCurrency source currency code
 * @param toCurrency target currency code
 */
@Schema(description = "Request to convert an amount between two currencies")
public record ConversionRequest(
    @Schema(description = "Amount to convert", example = "100.00")
    BigDecimal amount,
    
    @Schema(description = "Source currency code", example = "USD")
    CurrencyCode fromCurrency,
    
    @Schema(description = "Target currency code", example = "EUR")
    CurrencyCode toCurrency
) {
    /**
     * Validates the conversion request.
     *
     * @throws IllegalArgumentException if amount is negative or currencies are invalid
     */
    public ConversionRequest {
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException("Amount must be non-negative");
        }
        if (fromCurrency == null || toCurrency == null) {
            throw new IllegalArgumentException("Currencies cannot be null");
        }
    }
}
