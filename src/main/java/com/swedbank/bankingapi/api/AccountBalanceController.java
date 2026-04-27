package com.swedbank.bankingapi.api;

import com.swedbank.bankingapi.api.dto.BalanceResponse;
import com.swedbank.bankingapi.api.dto.MoneyRequest;
import com.swedbank.bankingapi.domain.CurrencyCode;
import com.swedbank.bankingapi.service.AccountBalanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller exposing account deposit and debit operations.
 *
 * @author Ants-Erik Noormagi (AEN)
 * @since v1.0
 */
@RestController
@RequestMapping("/api/v1/accounts/{accountId}")
@Tag(name = "Account Balance", description = "Operations for managing account balances in multiple currencies")
public class AccountBalanceController {

    /**
     * Service layer used to process balance operations.
     */
    private final AccountBalanceService accountBalanceService;

    /**
     * Creates the controller with its service dependency.
     *
     * @param accountBalanceService service handling account balance operations
     */
    public AccountBalanceController(AccountBalanceService accountBalanceService) {
        this.accountBalanceService = accountBalanceService;
    }

    /**
     * Deposits the requested amount into the account's specified currency balance.
     *
     * @param accountId account identifier
     * @param request deposit request body with amount and currency
     * @return updated balance response
     */
    @PostMapping("/deposits")
    @Operation(summary = "Deposit money to account", 
               description = "Add money to an account in a specific currency. Creates the balance if it doesn't exist.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Deposit successful"),
        @ApiResponse(responseCode = "400", description = "Invalid request (negative amount, unknown currency, etc.)")
    })
    public BalanceResponse deposit(@PathVariable UUID accountId, @Valid @RequestBody MoneyRequest request) {
        return accountBalanceService.addMoney(accountId, request.amount(), request.currency());
    }

    /**
     * Debits the requested amount from the account's specified currency balance.
     *
     * @param accountId account identifier
     * @param request debit request body with amount and currency
     * @return updated balance response
     */
    @PostMapping("/debits")
    @Operation(summary = "Debit money from account", 
               description = "Debit money from an account in a specific currency. External logging endpoint is called before processing.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Debit successful"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "Account balance not found for currency"),
        @ApiResponse(responseCode = "422", description = "Insufficient funds"),
        @ApiResponse(responseCode = "502", description = "External logging endpoint failed")
    })
    public BalanceResponse debit(@PathVariable UUID accountId, @Valid @RequestBody MoneyRequest request) {
        return accountBalanceService.debitMoney(accountId, request.amount(), request.currency());
    }

    /**
     * Retrieves the current balance for the given account and currency.
     *
     * @param accountId account identifier
     * @param currency currency code string (EUR, USD, SEK, GBP)
     * @return current balance response
     */
    @GetMapping
    @Operation(summary = "Get account balance", 
               description = "Retrieve the current balance for an account in a specific currency (default: EUR)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Balance retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Account balance not found for the specified currency")
    })
    public BalanceResponse getBalance(@PathVariable UUID accountId,
                                      @RequestParam(defaultValue = "EUR") String currency) {
        CurrencyCode currencyCode = CurrencyCode.valueOf(currency.toUpperCase());
        return accountBalanceService.getBalance(accountId, currencyCode);
    }
}
