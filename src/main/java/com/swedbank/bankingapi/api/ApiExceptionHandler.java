package com.swedbank.bankingapi.api;

import com.swedbank.bankingapi.client.ExternalLoggingException;
import com.swedbank.bankingapi.service.AccountBalanceNotFoundException;
import com.swedbank.bankingapi.service.AccountNotFoundException;
import com.swedbank.bankingapi.service.InsufficientFundsException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralized REST exception handler for translating backend failures into API
 * responses.
 *
 * @author Ants-Erik Noormagi (AEN)
 * @since v1.0
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /**
     * Maps external logging failures to a bad gateway response.
     *
     * @param ex runtime exception describing the external failure
     * @return RFC 7807 problem detail response
     */
    @ExceptionHandler({ ExternalLoggingException.class, IllegalStateException.class })
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ProblemDetail handleExternalFailure(RuntimeException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, ex.getMessage());
        problemDetail.setTitle("External logging failure");
        return problemDetail;
    }

    /**
     * Maps insufficient funds failures to an unprocessable entity response.
     *
     * @param ex insufficient funds exception
     * @return RFC 7807 problem detail response
     */
    @ExceptionHandler(InsufficientFundsException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ProblemDetail handleInsufficientFunds(InsufficientFundsException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY,
                ex.getMessage());
        problemDetail.setTitle("Insufficient funds");
        return problemDetail;
    }

    /**
     * Maps account not found failures to a not found response.
     *
     * @param ex account not found exception
     * @return RFC 7807 problem detail response
     */
    @ExceptionHandler(AccountNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ProblemDetail handleAccountNotFound(AccountNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Account not found");
        return problemDetail;
    }

    /**
     * Maps validation failures to a bad request response.
     *
     * @param ex validation-related exception
     * @return RFC 7807 problem detail response
     */
    @ExceptionHandler({ MethodArgumentNotValidException.class, ConstraintViolationException.class,
            IllegalArgumentException.class })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleValidationError(Exception ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Validation failed");
        return problemDetail;
    }

    /**
     * Maps account balance not found failures to a not found response.
     *
     * @param ex account balance not found exception
     * @return RFC 7807 problem detail response
     */
    @ExceptionHandler(AccountBalanceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ProblemDetail handleAccountBalanceNotFound(AccountBalanceNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Account balance not found");
        return problemDetail;
    }
}
