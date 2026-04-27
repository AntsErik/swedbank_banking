package com.swedbank.bankingapi.api.dto;

import com.swedbank.bankingapi.domain.CurrencyCode;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Request to deposit or debit money from an account")
public record MoneyRequest(
        @NotNull
        @DecimalMin(value = "0.01", inclusive = true)
        @Schema(description = "Amount to process (minimum 0.01)", example = "100.50")
        BigDecimal amount,

        @NotNull
        @Schema(description = "Currency code", example = "USD")
        CurrencyCode currency
) {
}
