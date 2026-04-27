package com.swedbank.bankingapi.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * API response describing the balance of a single account in a single currency.
 *
 * @param accountId account identifier
 * @param currency currency code
 * @param balance current balance amount
 * @author Ants-Erik Noormagi (AEN)
 * @since v1.0
 */
@Schema(description = "Account balance in a specific currency")
public record BalanceResponse(
        @Schema(description = "Unique account identifier (UUID)")
        UUID accountId,
        
        @Schema(description = "Currency code (EUR, USD, SEK, GBP)")
        String currency,
        
        @Schema(description = "Current balance amount", example = "100.50")
        BigDecimal balance
) {
}
