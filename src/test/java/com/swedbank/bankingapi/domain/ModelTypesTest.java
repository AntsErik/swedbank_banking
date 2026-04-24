package com.swedbank.bankingapi.domain;

import com.swedbank.bankingapi.api.dto.BalanceResponse;
import com.swedbank.bankingapi.api.dto.MoneyRequest;
import com.swedbank.bankingapi.client.ExternalLoggingException;
import com.swedbank.bankingapi.service.InsufficientFundsException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lightweight tests for domain and DTO types.
 *
 * @author Ants-Erik Noormagi (AEN)
 * @since v1.0
 */
class ModelTypesTest {

    @Test
    void accountBalanceExposesMutableBalanceState() {
        UUID accountId = UUID.randomUUID();
        AccountBalance accountBalance = new AccountBalance(accountId, CurrencyCode.EUR, new BigDecimal("10.00"));

        accountBalance.setBalance(new BigDecimal("15.00"));

        assertThat(accountBalance.getAccountId()).isEqualTo(accountId);
        assertThat(accountBalance.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(accountBalance.getBalance()).isEqualByComparingTo("15.00");
    }

    @Test
    void dtoRecordsExposeAssignedValues() {
        UUID accountId = UUID.randomUUID();
        BalanceResponse balanceResponse = new BalanceResponse(accountId, "EUR", new BigDecimal("25.00"));
        MoneyRequest moneyRequest = new MoneyRequest(new BigDecimal("30.00"));

        assertThat(balanceResponse.accountId()).isEqualTo(accountId);
        assertThat(balanceResponse.currency()).isEqualTo("EUR");
        assertThat(balanceResponse.balance()).isEqualByComparingTo("25.00");
        assertThat(moneyRequest.amount()).isEqualByComparingTo("30.00");
        assertThat(CurrencyCode.valueOf("EUR")).isEqualTo(CurrencyCode.EUR);
    }

    @Test
    void customExceptionsPreserveProvidedMessages() {
        ExternalLoggingException externalLoggingException = new ExternalLoggingException("external failed");
        InsufficientFundsException insufficientFundsException = new InsufficientFundsException("not enough");

        assertThat(externalLoggingException).hasMessage("external failed");
        assertThat(insufficientFundsException).hasMessage("not enough");
    }
}