package com.swedbank.bankingapi.service;

import com.swedbank.bankingapi.api.dto.BalanceResponse;
import com.swedbank.bankingapi.client.ExternalLoggingClient;
import com.swedbank.bankingapi.domain.AccountBalance;
import com.swedbank.bankingapi.domain.CurrencyCode;
import com.swedbank.bankingapi.repository.AccountBalanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * Coordinates deposit and debit operations for account balances.
 *
 * @author Ants-Erik Noormagi (AEN)
 * @since v1.0
 */
@Service
public class AccountBalanceService {

    /**
     * Single currency currently supported by this first iteration of the service.
     */
    private static final CurrencyCode ONLY_SUPPORTED_CURRENCY = CurrencyCode.EUR;

    /**
     * Repository used for balance persistence.
     */
    private final AccountBalanceRepository accountBalanceRepository;

    /**
     * External client used to simulate the required pre-debit logging call.
     */
    private final ExternalLoggingClient externalLoggingClient;

    /**
     * Creates the balance service with its persistence and external dependencies.
     *
     * @param accountBalanceRepository balance repository
     * @param externalLoggingClient external logging client
     */
    public AccountBalanceService(AccountBalanceRepository accountBalanceRepository,
                                 ExternalLoggingClient externalLoggingClient) {
        this.accountBalanceRepository = accountBalanceRepository;
        this.externalLoggingClient = externalLoggingClient;
    }

    /**
     * Deposits money into the EUR balance of the given account.
     *
     * @param accountId account identifier
     * @param amount amount to deposit
     * @return updated balance response
     */
    @Transactional
    public BalanceResponse addMoney(UUID accountId, BigDecimal amount) {
        BigDecimal normalizedAmount = normalizeAmount(amount);
        AccountBalance accountBalance = accountBalanceRepository
                .findByAccountIdAndCurrency(accountId, ONLY_SUPPORTED_CURRENCY)
                .orElseGet(() -> new AccountBalance(accountId, ONLY_SUPPORTED_CURRENCY, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN)));

        BigDecimal newBalance = accountBalance.getBalance().add(normalizedAmount).setScale(2, RoundingMode.HALF_EVEN);
        accountBalance.setBalance(newBalance);
        AccountBalance saved = accountBalanceRepository.save(accountBalance);

        return toResponse(saved);
    }

    /**
     * Debits money from the EUR balance of the given account after the external logging call succeeds.
     *
     * @param accountId account identifier
     * @param amount amount to debit
     * @return updated balance response
     */
    @Transactional
    public BalanceResponse debitMoney(UUID accountId, BigDecimal amount) {
        BigDecimal normalizedAmount = normalizeAmount(amount);
        externalLoggingClient.logDebitAttempt(accountId, normalizedAmount);

        AccountBalance accountBalance = accountBalanceRepository
                .findByAccountIdAndCurrency(accountId, ONLY_SUPPORTED_CURRENCY)
                .orElseThrow(() -> new InsufficientFundsException("No EUR balance found for account " + accountId));

        if (accountBalance.getBalance().compareTo(normalizedAmount) < 0) {
            throw new InsufficientFundsException(
                    "Insufficient EUR funds. Current balance: " + accountBalance.getBalance() + ", requested: " + normalizedAmount
            );
        }

        BigDecimal newBalance = accountBalance.getBalance().subtract(normalizedAmount).setScale(2, RoundingMode.HALF_EVEN);
        accountBalance.setBalance(newBalance);
        AccountBalance saved = accountBalanceRepository.save(accountBalance);

        return toResponse(saved);
    }

    /**
     * Retrieves the current EUR balance for the given account.
     *
     * @param accountId account identifier
     * @return current balance response
     * @throws AccountNotFoundException if no EUR balance exists for the account
     */
    public BalanceResponse getBalance(UUID accountId) {
        AccountBalance accountBalance = accountBalanceRepository
                .findByAccountIdAndCurrency(accountId, ONLY_SUPPORTED_CURRENCY)
                .orElseThrow(() -> new AccountNotFoundException("No EUR balance found for account " + accountId));

        return toResponse(accountBalance);
    }

    /**
     * Normalizes monetary values to two decimal places using bankers rounding.
     *
     * @param amount amount to normalize
     * @return normalized amount
     */
    private BigDecimal normalizeAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_EVEN);
    }

    /**
     * Converts the entity model to the API response model.
     *
     * @param accountBalance persisted balance entity
     * @return API response payload
     */
    private BalanceResponse toResponse(AccountBalance accountBalance) {
        return new BalanceResponse(
                accountBalance.getAccountId(),
                accountBalance.getCurrency().name(),
                accountBalance.getBalance()
        );
    }
}
