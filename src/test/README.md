# Testing Guide

This folder contains the automated tests for the current first implementation slice of the banking API.

## Scope covered
The current tests cover all implemented functionality:
- Multi-currency deposits (EUR, USD, SEK, GBP)
- Multi-currency debits (EUR, USD, SEK, GBP)
- Multi-currency balance lookups with currency query parameter
- Currency exchange operations (transfers between account currency balances)
- Exchange rate conversions with Swedbank rates
- pre-debit external logging behavior
- validation and exception mapping
- repository lookup for account balances by currency
- basic Spring context startup

## Test structure
- `BankingApiApplicationTests`
  Verifies that the Spring application context starts successfully.

- `service/AccountBalanceServiceTest`
  Unit tests for multi-currency deposit and debit business logic, including insufficient funds behavior, currency handling, and amount normalization.

- `api/AccountBalanceControllerTest`
  MVC tests for multi-currency deposit and debit endpoints, currency query parameter handling, request validation, and exception-to-response mapping.

- `api/ApiExceptionHandlerTest`
  Unit tests for RFC 7807 style error mapping.

- `api/MockExternalLogControllerTest`
  Verifies the local mock endpoint used for external logging simulation.

- `repository/AccountBalanceRepositoryTest`
  JPA repository test for loading balances by account and currency (supports all 4 currencies).

- `client/HttpExternalLoggingClientTest`
  Unit tests for successful and failing outbound logging calls.

- `client/SwedbankExchangeRateClientTest`
  Verifies CSV resource loading, data parsing, and base currency (EUR) protection logic.

- `domain/ModelTypesTest`
  Lightweight tests for entity, DTO, enum, and exception classes, including CurrencyCode enum variants and exchange rate DTOs.

## How to run
Run the full test suite from project root:
```bash
./mvnw test
# or with system Maven:
mvn test
```

Run a single test class:
```bash
./mvnw -Dtest=AccountBalanceServiceTest test
# or:
mvn -Dtest=AccountBalanceServiceTest test
```

Run multiple specific tests:
```bash
./mvnw -Dtest=AccountBalanceServiceTest,AccountBalanceControllerTest test
# or:
mvn -Dtest=AccountBalanceServiceTest,AccountBalanceControllerTest test
```

## Notes
- The tests use the CurrencyCode enum for all multi-currency operations (EUR, USD, SEK, GBP).
- The HTTP client tests use mocking rather than real external calls.
- The repository tests use the H2 test database provided by Spring Boot test support.
- Exchange rate conversions are tested via the ExchangeRateService integration in model tests.
