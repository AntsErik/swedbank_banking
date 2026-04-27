package com.swedbank.bankingapi.api.dto;

import com.swedbank.bankingapi.domain.CurrencyCode;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO for currency conversion operations.
 * Contains the exchange details and the updated account balances after conversion.
 *
 * @author Ants-Erik Noormagi (AEN)
 * @since v1.0
 *
 * @param accountId the account where the exchange was performed
 * @param originalAmount the amount that was converted
 * @param originalCurrency source currency code
 * @param convertedAmount result of the conversion
 * @param targetCurrency target currency code
 * @param exchangeRate the rate used for the conversion
 * @param sourceBalanceAfter balance in source currency after exchange
 * @param targetBalanceAfter balance in target currency after exchange
 */
@Schema(description = "Result of a currency exchange operation within an account")
public record ConversionResponse(
    @Schema(description = "Account identifier", example = "11111111-1111-1111-1111-111111111111")
    UUID accountId,
    
    @Schema(description = "Original amount before conversion", example = "100.00")
    BigDecimal originalAmount,
    
    @Schema(description = "Source currency code", example = "USD")
    CurrencyCode originalCurrency,
    
    @Schema(description = "Amount after conversion", example = "92.84")
    BigDecimal convertedAmount,
    
    @Schema(description = "Target currency code", example = "EUR")
    CurrencyCode targetCurrency,
    
    @Schema(description = "Exchange rate used (target units per source unit)", example = "0.9284")
    BigDecimal exchangeRate,
    
    @Schema(description = "Balance in source currency after exchange", example = "0.00")
    BigDecimal sourceBalanceAfter,
    
    @Schema(description = "Balance in target currency after exchange", example = "92.84")
    BigDecimal targetBalanceAfter
) {
}
