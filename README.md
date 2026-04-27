# Banking API (Java 17, Spring Boot)

Multi-currency account management with:
- Add/debit money to accounts in multiple currencies (EUR, USD, SEK, GBP)
- Separate balance tracking per currency
- Real-time currency exchange using Swedbank rates
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

Swagger UI (Interactive API documentation):
- http://localhost:8080/swagger-ui.html
- Try out all endpoints directly from the browser

## API endpoints (Multi-currency: EUR, USD, SEK, GBP)

All endpoints support multiple currencies. Account balances are tracked separately per currency.

**Recommended: Use Swagger UI to test endpoints** — Open http://localhost:8080/swagger-ui.html in your browser to interactively test all endpoints.

### Account Operations
Base path for account operations:
- /api/v1/accounts/{accountId}

#### Deposit
POST /api/v1/accounts/{accountId}/deposits

Add money to the account (USD, SEK, or GBP). If no balance exists for the currency, it will be created.

Body:
```json
{
  "amount": 100.00,
  "currency": "USD"
}
```

#### Debit
POST /api/v1/accounts/{accountId}/debits

Debit money from the account. **Before processing the debit, the system calls an external logging endpoint** (configured in `external.logging-url`, default: `http://httpstat.us/200`). 
- If the external call fails, the debit is **NOT processed** and an error is returned.
- If the external call succeeds, the debit is processed.

Body:
```json
{
  "amount": 40.00,
  "currency": "USD"
}
```

#### Get Balance
GET /api/v1/accounts/{accountId}?currency=USD

Returns the current account balance for a specific currency.

Response (200 OK):
```json
{
  "accountId": "11111111-1111-1111-1111-111111111111",
  "currency": "USD",
  "balance": 60.00
}
```

Error responses:
- 404 NOT_FOUND: Account balance does not exist for the specified currency

### Currency Exchange
POST /api/v1/exchange

Convert an amount between any supported currencies using current Swedbank exchange rates.

Body:
```json
{
  "amount": 100.00,
  "fromCurrency": "USD",
  "toCurrency": "EUR"
}
```

Response (200 OK):
```json
{
  "originalAmount": 100.00,
  "originalCurrency": "USD",
  "convertedAmount": 85.38,
  "targetCurrency": "EUR",
  "exchangeRate": 0.8538
}
```

### Example curl
```bash
ACCOUNT_ID="11111111-1111-1111-1111-111111111111"

# Deposit 100 USD into the account
curl -X POST "http://localhost:8080/api/v1/accounts/$ACCOUNT_ID/deposits" \
  -H "Content-Type: application/json" \
  -d '{"amount":100.00,"currency":"USD"}'

# Get USD balance
curl -X GET "http://localhost:8080/api/v1/accounts/$ACCOUNT_ID?currency=USD"

# Debit 25 USD from the account
curl -X POST "http://localhost:8080/api/v1/accounts/$ACCOUNT_ID/debits" \
  -H "Content-Type: application/json" \
  -d '{"amount":25.00,"currency":"USD"}'

# Convert 100 USD to EUR using current Swedbank rates
curl -X POST "http://localhost:8080/api/v1/exchange" \
  -H "Content-Type: application/json" \
  -d '{"amount":100.00,"fromCurrency":"USD","toCurrency":"EUR"}'
```

### Example PowerShell
```powershell
$ACCOUNT_ID = "11111111-1111-1111-1111-111111111111"
$API_URL = "http://localhost:8080/api/v1/accounts"

# Deposit 100 USD into the account
Invoke-RestMethod -Uri "$API_URL/$ACCOUNT_ID/deposits" -Method Post `
  -Headers @{"Content-Type" = "application/json"} `
  -Body '{"amount":100.00,"currency":"USD"}' | ConvertTo-Json

# Get USD balance (note: use backtick before the ? for query parameters)
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/accounts/$ACCOUNT_ID`?currency=USD" -Method Get | ConvertTo-Json

# Debit 25 USD from the account
Invoke-RestMethod -Uri "$API_URL/$ACCOUNT_ID/debits" -Method Post `
  -Headers @{"Content-Type" = "application/json"} `
  -Body '{"amount":25.00,"currency":"USD"}' | ConvertTo-Json

# Convert 100 USD to EUR using current Swedbank rates
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/exchange" -Method Post `
  -Headers @{"Content-Type" = "application/json"} `
  -Body '{"amount":100.00,"fromCurrency":"USD","toCurrency":"EUR"}' | ConvertTo-Json
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
- Debit fails with HTTP 422 when there is not enough balance in the specified currency.
- Debit fails with HTTP 502 if external logging endpoint fails.
- Currency support: EUR (base), USD, SEK, GBP with Swedbank exchange rates.
- Exchange rates are fetched from Swedbank (https://www.swedbank.ee/private/d2d/payments2/rates/currencyExchange).
- Each account can hold multiple currency balances tracked independently.
- Account balances are queried separately per currency.
- Conversions use ECB reference rates with 2-decimal precision (HALF_EVEN rounding).
