# Banking API (Java 17, Spring Boot)

Minimal first step for the homework assignment with:
- Add money to account (EUR only)
- Debit money from account (EUR only)
- Simulated external call before debit (httpstat.us)
- Controller -> Service -> Repository structure
- H2 database persistence

## Tech stack
- Java 17
- Spring Boot 3.3.4
- Spring Web
- Spring Data JPA
- H2
- Maven

## Requirements
- Java 17+
- Maven 3.9+

## Run locally
From project root:
```powershell
mvn clean package
java -jar .\target\banking-api-0.0.1-SNAPSHOT.jar
```

Alternative (works on many setups):
```powershell
mvn clean spring-boot:run
```

## Run from Bash / WSL
This project also runs from bash, but the shell itself must have Java 17+ and Maven 3.9+ available.

Once Java 17 is available in bash:
```bash
mvn clean package
chmod +x ./run-local.sh
./run-local.sh
```

The script starts the service on port `8080` and points debit logging to the built-in local mock endpoint so the demo does not depend on external internet access.

If you want a different port:
```bash
PORT=8081 ./run-local.sh
```

Service starts on:
- http://localhost:8080

External logging URL used before debit (default):
- http://httpstat.us/200

If your network blocks external pages, use the built-in local mock endpoint:
```powershell
java -jar .\target\banking-api-0.0.1-SNAPSHOT.jar --external.logging-url=http://localhost:8080/mock/external-log
```

H2 console:
- http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:mem:bankingdb
- user: sa
- password: (empty)

## API endpoints (EUR only)
Base path:
- /api/v1/accounts/{accountId}

### Deposit
POST /api/v1/accounts/{accountId}/deposits

Body:
```json
{
  "amount": 100.00
}
```

### Debit
POST /api/v1/accounts/{accountId}/debits

Body:
```json
{
  "amount": 40.00
}
```

### Example curl
```bash
ACCOUNT_ID="11111111-1111-1111-1111-111111111111"

curl -X POST "http://localhost:8080/api/v1/accounts/$ACCOUNT_ID/deposits" \
  -H "Content-Type: application/json" \
  -d '{"amount":100.00}'

curl -X POST "http://localhost:8080/api/v1/accounts/$ACCOUNT_ID/debits" \
  -H "Content-Type: application/json" \
  -d '{"amount":25.00}'
```

## Testing
The project currently includes a first test suite covering the implemented EUR-only scope.

Covered areas:
- service logic
- controller request/response handling
- repository lookup behavior
- external logging client behavior
- exception handler mapping
- mock external endpoint
- DTO and domain helper types
- Spring context startup

Run all tests:
```powershell
mvn test
```

Run a single test class:
```powershell
mvn -Dtest=AccountBalanceServiceTest test
```

More detailed testing notes are available in [src/test/README.md](src/test/README.md).

## Notes
- Debit fails with HTTP 422 when there is not enough balance.
- Debit fails with HTTP 502 if external logging endpoint fails.
- This is intentionally minimal for step 1: only EUR.
- Next steps can add external call simulation, multi-currency balances, get balance, and exchange.
