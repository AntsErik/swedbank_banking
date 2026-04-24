package com.swedbank.bankingapi.client;

/**
 * Exception thrown when the external logging call fails.
 *
 * @author Ants-Erik Noormagi (AEN)
 * @since v1.0
 */
public class ExternalLoggingException extends RuntimeException {

    /**
     * Creates a new exception with a message and cause.
     *
     * @param message exception detail message
     * @param cause underlying cause of the failure
     */
    public ExternalLoggingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new exception with a message only.
     *
     * @param message exception detail message
     */
    public ExternalLoggingException(String message) {
        super(message);
    }
}
