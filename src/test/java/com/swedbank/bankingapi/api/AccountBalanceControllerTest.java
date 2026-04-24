package com.swedbank.bankingapi.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swedbank.bankingapi.api.dto.BalanceResponse;
import com.swedbank.bankingapi.service.AccountBalanceService;
import com.swedbank.bankingapi.service.AccountNotFoundException;
import com.swedbank.bankingapi.service.InsufficientFundsException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MVC tests for {@link AccountBalanceController}.
 *
 * @author Ants-Erik Noormagi (AEN)
 * @since v1.0
 */
@WebMvcTest(AccountBalanceController.class)
@Import(ApiExceptionHandler.class)
class AccountBalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountBalanceService accountBalanceService;

    @Test
    void depositReturnsUpdatedBalance() throws Exception {
        UUID accountId = UUID.randomUUID();
        BalanceResponse response = new BalanceResponse(accountId, "EUR", new BigDecimal("100.00"));
        when(accountBalanceService.addMoney(eq(accountId), eq(new BigDecimal("100.00")))).thenReturn(response);

        mockMvc.perform(post("/api/v1/accounts/{accountId}/deposits", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DepositPayload("100.00"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountId.toString()))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.balance").value(100.00));
    }

    @Test
    void debitReturnsUpdatedBalance() throws Exception {
        UUID accountId = UUID.randomUUID();
        BalanceResponse response = new BalanceResponse(accountId, "EUR", new BigDecimal("75.00"));
        when(accountBalanceService.debitMoney(eq(accountId), eq(new BigDecimal("25.00")))).thenReturn(response);

        mockMvc.perform(post("/api/v1/accounts/{accountId}/debits", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DepositPayload("25.00"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(75.00));
    }

    @Test
    void debitReturnsUnprocessableEntityWhenServiceThrowsInsufficientFunds() throws Exception {
        UUID accountId = UUID.randomUUID();
        when(accountBalanceService.debitMoney(eq(accountId), eq(new BigDecimal("25.00"))))
                .thenThrow(new InsufficientFundsException("Insufficient EUR funds"));

        mockMvc.perform(post("/api/v1/accounts/{accountId}/debits", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DepositPayload("25.00"))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Insufficient funds"));
    }

    @Test
    void depositReturnsBadRequestWhenAmountIsMissing() throws Exception {
        UUID accountId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/accounts/{accountId}/deposits", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
    }

    @Test
    void getBalanceReturnsCurrentBalance() throws Exception {
        UUID accountId = UUID.randomUUID();
        BalanceResponse response = new BalanceResponse(accountId, "EUR", new BigDecimal("50.75"));
        when(accountBalanceService.getBalance(eq(accountId))).thenReturn(response);

        mockMvc.perform(get("/api/v1/accounts/{accountId}", accountId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountId.toString()))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.balance").value(50.75));
    }

    @Test
    void getBalanceReturnsNotFoundWhenAccountNotFound() throws Exception {
        UUID accountId = UUID.randomUUID();
        when(accountBalanceService.getBalance(eq(accountId)))
                .thenThrow(new AccountNotFoundException("No EUR balance found for account " + accountId));

        mockMvc.perform(get("/api/v1/accounts/{accountId}", accountId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Account not found"));
    }

    private record DepositPayload(String amount) {
    }
}