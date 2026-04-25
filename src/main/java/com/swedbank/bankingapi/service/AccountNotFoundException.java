package com.swedbank.bankingapi.service;

/**
 * Exception thrown when a requested account balance is not found.
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
