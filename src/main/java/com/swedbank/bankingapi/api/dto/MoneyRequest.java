package com.swedbank.bankingapi.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * API request containing a single monetary amount.
 *
 * @param amount positive amount to process
 * @author Ants-Erik Noormagi (AEN)
 * @since v1.0
 */
public record MoneyRequest(
        @NotNull
        @DecimalMin(value = "0.01", inclusive = true)
        BigDecimal amount
) {
}
