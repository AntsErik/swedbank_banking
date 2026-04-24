package com.swedbank.bankingapi.repository;

import com.swedbank.bankingapi.domain.AccountBalance;
import com.swedbank.bankingapi.domain.CurrencyCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests for {@link AccountBalanceRepository}.
 *
 * @author Ants-Erik Noormagi (AEN)
 * @since v1.0
 */
@DataJpaTest
class AccountBalanceRepositoryTest {

    @Autowired
    private AccountBalanceRepository accountBalanceRepository;

    @Test
    void findByAccountIdAndCurrencyReturnsMatchingBalance() {
        UUID accountId = UUID.randomUUID();
        accountBalanceRepository.save(new AccountBalance(accountId, CurrencyCode.EUR, new BigDecimal("55.00")));

        Optional<AccountBalance> result = accountBalanceRepository.findByAccountIdAndCurrency(accountId, CurrencyCode.EUR);

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().getBalance()).isEqualByComparingTo("55.00");
    }
}