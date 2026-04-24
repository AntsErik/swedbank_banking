package com.swedbank.bankingapi.api.dto;

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
public record BalanceResponse(
        UUID accountId,
        String currency,
        BigDecimal balance
) {
}
