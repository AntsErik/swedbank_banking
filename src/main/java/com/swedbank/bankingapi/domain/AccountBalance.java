package com.swedbank.bankingapi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents the persisted balance for a single account and currency pair.
 *
 * @author Ants-Erik Noormagi (AEN)
 * @since v1.0
 */
@Entity
@Table(name = "account_balances", uniqueConstraints = {
        @UniqueConstraint(name = "uk_account_currency", columnNames = {"account_id", "currency"})
})
public class AccountBalance {

    /**
     * Surrogate primary key of the balance row.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique identifier of the account that owns this balance.
     */
    @Column(name = "account_id", nullable = false, updatable = false)
    private UUID accountId;

    /**
     * Currency for which the balance is tracked.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private CurrencyCode currency;

    /**
     * Current balance amount for the account and currency.
     */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    /**
     * Optimistic locking version used by JPA.
     */
    @Version
    private Long version;

    /**
     * Protected no-args constructor required by JPA.
     */
    protected AccountBalance() {
    }

    /**
     * Creates a new balance aggregate for an account and currency.
     *
     * @param accountId account identifier
     * @param currency currency of the stored balance
     * @param balance initial balance amount
     */
    public AccountBalance(UUID accountId, CurrencyCode currency, BigDecimal balance) {
        this.accountId = accountId;
        this.currency = currency;
        this.balance = balance;
    }

    /**
     * Returns the database identifier of the balance row.
     *
     * @return balance identifier
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the owning account identifier.
     *
     * @return account identifier
     */
    public UUID getAccountId() {
        return accountId;
    }

    /**
     * Returns the balance currency.
     *
     * @return currency code
     */
    public CurrencyCode getCurrency() {
        return currency;
    }

    /**
     * Returns the current account balance.
     *
     * @return current balance amount
     */
    public BigDecimal getBalance() {
        return balance;
    }

    /**
     * Updates the current balance amount.
     *
     * @param balance new balance amount
     */
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    /**
     * Returns the optimistic locking version.
     *
     * @return entity version
     */
    public Long getVersion() {
        return version;
    }
}
