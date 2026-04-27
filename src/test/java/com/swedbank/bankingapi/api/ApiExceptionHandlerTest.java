package com.swedbank.bankingapi.api;

import com.swedbank.bankingapi.client.ExternalLoggingException;
import com.swedbank.bankingapi.domain.CurrencyCode;
import com.swedbank.bankingapi.service.AccountBalanceNotFoundException;
import com.swedbank.bankingapi.service.AccountNotFoundException;
import com.swedbank.bankingapi.service.InsufficientFundsException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

/**
 * Unit tests for {@link ApiExceptionHandler}.
 *
 * @author Ants-Erik Noormagi (AEN)
 * @since v1.0
 */
class ApiExceptionHandlerTest {

    private final ApiExceptionHandler apiExceptionHandler = new ApiExceptionHandler();

    @Test
    void handleExternalFailureReturnsBadGatewayProblem() {
        ProblemDetail problemDetail = apiExceptionHandler
                .handleExternalFailure(new ExternalLoggingException("external failed"));

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY.value());
        assertThat(problemDetail.getTitle()).isEqualTo("External logging failure");
    }

    @Test
    void handleInsufficientFundsReturnsUnprocessableEntityProblem() {
        ProblemDetail problemDetail = apiExceptionHandler
                .handleInsufficientFunds(new InsufficientFundsException("not enough money"));

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
        assertThat(problemDetail.getTitle()).isEqualTo("Insufficient funds");
    }

    @Test
    void handleAccountNotFoundReturnsNotFoundProblem() {
        ProblemDetail problemDetail = apiExceptionHandler
                .handleAccountNotFound(new AccountNotFoundException("account not found"));

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(problemDetail.getTitle()).isEqualTo("Account not found");
    }

    @Test
    void handleValidationErrorReturnsBadRequestProblem() {
        ProblemDetail problemDetail = apiExceptionHandler
                .handleValidationError(new IllegalArgumentException("invalid"));

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problemDetail.getTitle()).isEqualTo("Validation failed");
    }

    @Test
    void handleAccountBalanceNotFoundReturnsNotFoundProblem() {
        UUID accountId = UUID.randomUUID();
        CurrencyCode currency = CurrencyCode.USD;

        // Create the exception
        AccountBalanceNotFoundException exception = new AccountBalanceNotFoundException(accountId, currency);

        // Call the handler method directly (Unit Test style)
        ProblemDetail problemDetail = apiExceptionHandler.handleAccountBalanceNotFound(exception);

        // Assertions
        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(problemDetail.getTitle()).isEqualTo("Account balance not found");
        assertThat(problemDetail.getDetail()).contains("Balance for USD not found");
    }
}