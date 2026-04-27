package com.swedbank.bankingapi.service;

import com.swedbank.bankingapi.api.dto.BalanceResponse;
import com.swedbank.bankingapi.api.dto.ConversionResponse;
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
 * Coordinates deposit, debit, and exchange operations for account balances.
 * Supports multiple currencies per account with independent balance tracking.
 *
 * @author Ants-Erik Noormagi (AEN)
 * @since v1.0
 */
@Service
public class AccountBalanceService {

    /**
     * Repository used for balance persistence.
     */
    private final AccountBalanceRepository accountBalanceRepository;

    /**
     * External client used to simulate the required pre-debit logging call.
     */
    private final ExternalLoggingClient externalLoggingClient;

    /**
     * Service for currency exchange rate calculations.
     */
    private final ExchangeRateService exchangeRateService;

    /**
     * Creates the balance service with its persistence and external dependencies.
     *
     * @param accountBalanceRepository balance repository
     * @param externalLoggingClient external logging client
     * @param exchangeRateService exchange rate service for currency conversions
     */
    public AccountBalanceService(AccountBalanceRepository accountBalanceRepository,
                                 ExternalLoggingClient externalLoggingClient,
                                 ExchangeRateService exchangeRateService) {
        this.accountBalanceRepository = accountBalanceRepository;
        this.externalLoggingClient = externalLoggingClient;
        this.exchangeRateService = exchangeRateService;
    }

    /**
     * Deposits money into the specified currency balance of the given account.
     * Creates a new balance entry if one does not exist for the currency.
     *
     * @param accountId account identifier
     * @param amount amount to deposit
     * @param currency currency to deposit in (EUR, USD, SEK, GBP)
     * @return updated balance response
     */
    @Transactional
    public BalanceResponse addMoney(UUID accountId, BigDecimal amount, CurrencyCode currency) {
        BigDecimal normalizedAmount = normalizeAmount(amount);
        AccountBalance accountBalance = accountBalanceRepository
                .findByAccountIdAndCurrency(accountId, currency)
                .orElseGet(() -> new AccountBalance(accountId, currency, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN)));

        BigDecimal newBalance = accountBalance.getBalance().add(normalizedAmount).setScale(2, RoundingMode.HALF_EVEN);
        accountBalance.setBalance(newBalance);
        AccountBalance saved = accountBalanceRepository.save(accountBalance);

        return toResponse(saved);
    }

    /**
     * Debits money from the specified currency balance of the given account.
     * The external logging call must succeed before the debit is processed.
     *
     * @param accountId account identifier
     * @param amount amount to debit
     * @param currency currency to debit from (EUR, USD, SEK, GBP)
     * @return updated balance response
     * @throws InsufficientFundsException if balance is not found or is insufficient
     */
    @Transactional
    public BalanceResponse debitMoney(UUID accountId, BigDecimal amount, CurrencyCode currency) {
        BigDecimal normalizedAmount = normalizeAmount(amount);
        externalLoggingClient.logDebitAttempt(accountId, normalizedAmount);

        AccountBalance accountBalance = accountBalanceRepository
                .findByAccountIdAndCurrency(accountId, currency)
                .orElseThrow(() -> new InsufficientFundsException("No " + currency + " balance found for account " + accountId));

        if (accountBalance.getBalance().compareTo(normalizedAmount) < 0) {
            throw new InsufficientFundsException(
                    "Insufficient " + currency + " funds. Current balance: " + accountBalance.getBalance() + ", requested: " + normalizedAmount
            );
        }

        BigDecimal newBalance = accountBalance.getBalance().subtract(normalizedAmount).setScale(2, RoundingMode.HALF_EVEN);
        accountBalance.setBalance(newBalance);
        AccountBalance saved = accountBalanceRepository.save(accountBalance);

        return toResponse(saved);
    }

    /**
     * Retrieves the current balance for the given account and currency.
     *
     * @param accountId account identifier
     * @param currency currency to retrieve balance for (EUR, USD, SEK, GBP)
     * @return current balance response
     * @throws AccountNotFoundException if no balance exists for the account/currency pair
     */
    public BalanceResponse getBalance(UUID accountId, CurrencyCode currency) {
        AccountBalance accountBalance = accountBalanceRepository
                .findByAccountIdAndCurrency(accountId, currency)
                .orElseThrow(() -> new AccountNotFoundException("No " + currency + " balance found for account " + accountId));

        return toResponse(accountBalance);
    }

    /**
     * Exchanges currency within an account.
     * Debits the source currency balance and credits the target currency balance.
     *
     * @param accountId account identifier
     * @param amount amount to exchange in source currency
     * @param fromCurrency source currency (EUR, USD, SEK, GBP)
     * @param toCurrency target currency (EUR, USD, SEK, GBP)
     * @return conversion response with updated balances
     * @throws InsufficientFundsException if source currency balance is insufficient
     * @throws AccountNotFoundException if source currency balance doesn't exist
     */
    @Transactional
    public ConversionResponse exchangeCurrencies(UUID accountId, BigDecimal amount, 
                                                  CurrencyCode fromCurrency, CurrencyCode toCurrency) {
        BigDecimal normalizedAmount = normalizeAmount(amount);

        // Retrieve source currency balance
        AccountBalance sourceBalance = accountBalanceRepository
                .findByAccountIdAndCurrency(accountId, fromCurrency)
                .orElseThrow(() -> new AccountNotFoundException("No " + fromCurrency + " balance found for account " + accountId));

        // Check sufficient funds
        if (sourceBalance.getBalance().compareTo(normalizedAmount) < 0) {
            throw new InsufficientFundsException(
                    "Insufficient " + fromCurrency + " funds for exchange. Current balance: " + sourceBalance.getBalance() + ", requested: " + normalizedAmount
            );
        }

        // Calculate converted amount
        BigDecimal convertedAmount = exchangeRateService.convert(normalizedAmount, fromCurrency, toCurrency);

        // Debit source currency
        BigDecimal sourceNewBalance = sourceBalance.getBalance().subtract(normalizedAmount).setScale(2, RoundingMode.HALF_EVEN);
        sourceBalance.setBalance(sourceNewBalance);
        accountBalanceRepository.save(sourceBalance);

        // Credit target currency (create if doesn't exist)
        AccountBalance targetBalance = accountBalanceRepository
                .findByAccountIdAndCurrency(accountId, toCurrency)
                .orElseGet(() -> new AccountBalance(accountId, toCurrency, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN)));

        BigDecimal targetNewBalance = targetBalance.getBalance().add(convertedAmount).setScale(2, RoundingMode.HALF_EVEN);
        targetBalance.setBalance(targetNewBalance);
        accountBalanceRepository.save(targetBalance);

        // Calculate exchange rate
        BigDecimal exchangeRate = normalizedAmount.signum() == 0
                ? BigDecimal.ONE
                : convertedAmount.divide(normalizedAmount, 4, RoundingMode.HALF_EVEN);

        return new ConversionResponse(
                accountId,
                normalizedAmount,
                fromCurrency,
                convertedAmount,
                toCurrency,
                exchangeRate,
                sourceNewBalance,
                targetNewBalance
        );
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
