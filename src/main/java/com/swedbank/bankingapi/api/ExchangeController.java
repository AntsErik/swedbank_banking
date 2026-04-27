package com.swedbank.bankingapi.api;

import com.swedbank.bankingapi.api.dto.ConversionRequest;
import com.swedbank.bankingapi.api.dto.ConversionResponse;
import com.swedbank.bankingapi.service.AccountBalanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for currency exchange operations within an account.
 * Transfers money from one currency balance to another using Swedbank exchange rates.
 *
 * @author Ants-Erik Noormagi (AEN)
 * @since v1.0
 */
@RestController
@RequestMapping("/api/v1/exchange")
@Tag(name = "Currency Exchange", description = "Operations for currency conversion within an account using Swedbank exchange rates")
public class ExchangeController {

    /**
     * Service for account balance operations including currency exchange.
     */
    private final AccountBalanceService accountBalanceService;

    /**
     * Creates the exchange controller with its service dependency.
     *
     * @param accountBalanceService service for balance and exchange operations
     */
    public ExchangeController(AccountBalanceService accountBalanceService) {
        this.accountBalanceService = accountBalanceService;
    }

    /**
     * Exchanges currencies within an account.
     * Debits the source currency and credits the target currency using current Swedbank rates.
     * Both operations occur within a single transaction.
     *
     * @param request conversion request with accountId, amount, and source/target currencies
     * @return conversion response with updated balances for both currencies
     */
    @PostMapping
    @Operation(summary = "Exchange currencies within an account", 
               description = "Transfer money from one currency balance to another using current Swedbank exchange rates")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Exchange successful, balances updated"),
        @ApiResponse(responseCode = "400", description = "Invalid request (unknown currency, negative amount, etc.)"),
        @ApiResponse(responseCode = "404", description = "Source currency balance not found"),
        @ApiResponse(responseCode = "422", description = "Insufficient funds in source currency")
    })
    public ConversionResponse exchange(@Valid @RequestBody ConversionRequest request) {
        return accountBalanceService.exchangeCurrencies(
                request.accountId(),
                request.amount(),
                request.fromCurrency(),
                request.toCurrency()
        );
    }
}
