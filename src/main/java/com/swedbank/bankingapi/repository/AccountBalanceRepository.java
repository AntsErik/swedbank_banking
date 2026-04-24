package com.swedbank.bankingapi.repository;

import com.swedbank.bankingapi.domain.AccountBalance;
import com.swedbank.bankingapi.domain.CurrencyCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for reading and persisting account balance entities.
 *
 * @author Ants-Erik Noormagi (AEN)
 * @since v1.0
 */
public interface AccountBalanceRepository extends JpaRepository<AccountBalance, Long> {

    /**
     * Finds a balance by account identifier and currency.
     *
     * @param accountId account identifier
     * @param currency currency code
     * @return optional matching account balance
     */
    Optional<AccountBalance> findByAccountIdAndCurrency(UUID accountId, CurrencyCode currency);
}
