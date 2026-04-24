# Testing Guide

This folder contains the automated tests for the current first implementation slice of the banking API.

## Scope covered
The current tests focus on the functionality implemented so far:
- EUR deposits
- EUR debits
- pre-debit external logging behavior
- validation and exception mapping
- repository lookup
- basic Spring context startup

## Test structure
- `BankingApiApplicationTests`
  Verifies that the Spring application context starts successfully.

- `service/AccountBalanceServiceTest`
  Unit tests for deposit and debit business logic, including insufficient funds behavior and amount normalization.

- `api/AccountBalanceControllerTest`
  MVC tests for deposit and debit endpoints, request validation, and exception-to-response mapping.

- `api/ApiExceptionHandlerTest`
  Unit tests for RFC 7807 style error mapping.

- `api/MockExternalLogControllerTest`
  Verifies the local mock endpoint used for external logging simulation.

- `repository/AccountBalanceRepositoryTest`
  JPA repository test for loading balances by account and currency.

- `client/HttpExternalLoggingClientTest`
  Unit tests for successful and failing outbound logging calls.

- `domain/ModelTypesTest`
  Lightweight tests for entity, DTO, enum, and exception classes.

## How to run
Run the full test suite from project root:
```bash
mvn test
```

Run a single test class:
```bash
mvn -Dtest=AccountBalanceServiceTest test
```

Run multiple specific tests:
```bash
mvn -Dtest=AccountBalanceServiceTest,AccountBalanceControllerTest test
```

## Notes
- The tests are intentionally focused on the currently implemented scope and do not yet cover multi-currency support or exchange operations.
- The HTTP client tests use mocking rather than real external calls.
- The repository tests use the H2 test database provided by Spring Boot test support.
