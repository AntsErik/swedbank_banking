package com.swedbank.bankingapi.api;

import com.swedbank.bankingapi.api.dto.BalanceResponse;
import com.swedbank.bankingapi.api.dto.MoneyRequest;
import com.swedbank.bankingapi.service.AccountBalanceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
     * Deposits the requested amount into the account's EUR balance.
     *
     * @param accountId account identifier
     * @param request deposit request body
     * @return updated balance response
     */
    @PostMapping("/deposits")
    public BalanceResponse deposit(@PathVariable UUID accountId, @Valid @RequestBody MoneyRequest request) {
        return accountBalanceService.addMoney(accountId, request.amount());
    }

    /**
     * Debits the requested amount from the account's EUR balance.
     *
     * @param accountId account identifier
     * @param request debit request body
     * @return updated balance response
     */
    @PostMapping("/debits")
    public BalanceResponse debit(@PathVariable UUID accountId, @Valid @RequestBody MoneyRequest request) {
        return accountBalanceService.debitMoney(accountId, request.amount());
    }
}
