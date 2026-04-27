package com.swedbank.bankingapi.service;

/**
 * Exception thrown when the specified account (UUID) does not exist in the
 * system.
 * This covers cases where the account ID is invalid or has been deleted.
 *
 * @author Ants-Erik Noormagi (AEN)
 * @since v1.0
 */
public class AccountNotFoundException extends RuntimeException {

    /**
     * Creates a new exception with the given failure message.
     *
     * @param message exception detail message
     */
    public AccountNotFoundException(String message) {
        super(message);
    }
}
