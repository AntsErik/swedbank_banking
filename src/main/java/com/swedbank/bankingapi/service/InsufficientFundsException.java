package com.swedbank.bankingapi.service;

/**
 * Exception thrown when a debit request exceeds the available balance.
 *
 * @author Ants-Erik Noormagi (AEN)
 * @since v1.0
 */
public class InsufficientFundsException extends RuntimeException {

    /**
     * Creates a new exception with the given failure message.
     *
     * @param message exception detail message
     */
    public InsufficientFundsException(String message) {
        super(message);
    }
}
