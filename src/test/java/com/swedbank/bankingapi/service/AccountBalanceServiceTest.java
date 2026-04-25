package com.swedbank.bankingapi.service;

import com.swedbank.bankingapi.api.dto.BalanceResponse;
import com.swedbank.bankingapi.client.ExternalLoggingClient;
import com.swedbank.bankingapi.domain.AccountBalance;
import com.swedbank.bankingapi.domain.CurrencyCode;
import com.swedbank.bankingapi.repository.AccountBalanceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link AccountBalanceService}.
 *
 * @author Ants-Erik Noormagi (AEN)
 * @since v1.0
 */
@ExtendWith(MockitoExtension.class)
class AccountBalanceServiceTest {

    @Mock
    private AccountBalanceRepository accountBalanceRepository;

    @Mock
    private ExternalLoggingClient externalLoggingClient;

    @InjectMocks
    private AccountBalanceService accountBalanceService;

    @Test
    void addMoneyCreatesNewEurBalanceWhenAccountHasNoExistingBalance() {
        UUID accountId = UUID.randomUUID();
        when(accountBalanceRepository.findByAccountIdAndCurrency(accountId, CurrencyCode.EUR)).thenReturn(Optional.empty());
        when(accountBalanceRepository.save(any(AccountBalance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BalanceResponse response = accountBalanceService.addMoney(accountId, new BigDecimal("100"), CurrencyCode.EUR);

        assertThat(response.accountId()).isEqualTo(accountId);
        assertThat(response.currency()).isEqualTo("EUR");
        assertThat(response.balance()).isEqualByComparingTo("100.00");
        verify(externalLoggingClient, never()).logDebitAttempt(any(), any());
    }

    @Test
    void addMoneyUpdatesExistingBalance() {
        UUID accountId = UUID.randomUUID();
        AccountBalance accountBalance = new AccountBalance(accountId, CurrencyCode.EUR, scaled("25.00"));
        when(accountBalanceRepository.findByAccountIdAndCurrency(accountId, CurrencyCode.EUR)).thenReturn(Optional.of(accountBalance));
        when(accountBalanceRepository.save(accountBalance)).thenReturn(accountBalance);

        BalanceResponse response = accountBalanceService.addMoney(accountId, new BigDecimal("10.235"), CurrencyCode.EUR);

        assertThat(response.balance()).isEqualByComparingTo("35.24");
    }

    @Test
    void debitMoneyLogsExternallyAndUpdatesBalance() {
        UUID accountId = UUID.randomUUID();
        AccountBalance accountBalance = new AccountBalance(accountId, CurrencyCode.EUR, scaled("100.00"));
        when(accountBalanceRepository.findByAccountIdAndCurrency(accountId, CurrencyCode.EUR)).thenReturn(Optional.of(accountBalance));
        when(accountBalanceRepository.save(accountBalance)).thenReturn(accountBalance);

        BalanceResponse response = accountBalanceService.debitMoney(accountId, new BigDecimal("25"), CurrencyCode.EUR);

        assertThat(response.balance()).isEqualByComparingTo("75.00");
        verify(externalLoggingClient).logDebitAttempt(accountId, scaled("25.00"));
    }

    @Test
    void debitMoneyFailsWhenBalanceDoesNotExist() {
        UUID accountId = UUID.randomUUID();
        when(accountBalanceRepository.findByAccountIdAndCurrency(accountId, CurrencyCode.EUR)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountBalanceService.debitMoney(accountId, new BigDecimal("25.00"), CurrencyCode.EUR))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessageContaining("No EUR balance found");

        verify(externalLoggingClient).logDebitAttempt(accountId, scaled("25.00"));
    }

    @Test
    void debitMoneyFailsWhenBalanceIsInsufficient() {
        UUID accountId = UUID.randomUUID();
        AccountBalance accountBalance = new AccountBalance(accountId, CurrencyCode.EUR, scaled("10.00"));
        when(accountBalanceRepository.findByAccountIdAndCurrency(accountId, CurrencyCode.EUR)).thenReturn(Optional.of(accountBalance));

        assertThatThrownBy(() -> accountBalanceService.debitMoney(accountId, new BigDecimal("25.00"), CurrencyCode.EUR))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessageContaining("Insufficient EUR funds");

        verify(externalLoggingClient).logDebitAttempt(accountId, scaled("25.00"));
    }

    @Test
    void getBalanceReturnsCurrentBalance() {
        UUID accountId = UUID.randomUUID();
        AccountBalance accountBalance = new AccountBalance(accountId, CurrencyCode.EUR, scaled("50.75"));
        when(accountBalanceRepository.findByAccountIdAndCurrency(accountId, CurrencyCode.EUR)).thenReturn(Optional.of(accountBalance));

        BalanceResponse response = accountBalanceService.getBalance(accountId, CurrencyCode.EUR);

        assertThat(response.accountId()).isEqualTo(accountId);
        assertThat(response.currency()).isEqualTo("EUR");
        assertThat(response.balance()).isEqualByComparingTo("50.75");
    }

    @Test
    void getBalanceThrowsAccountNotFoundExceptionWhenBalanceDoesNotExist() {
        UUID accountId = UUID.randomUUID();
        when(accountBalanceRepository.findByAccountIdAndCurrency(accountId, CurrencyCode.EUR)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountBalanceService.getBalance(accountId, CurrencyCode.EUR))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("No EUR balance found");
    }

    private BigDecimal scaled(String value) {
        return new BigDecimal(value).setScale(2, RoundingMode.HALF_EVEN);
    }
}