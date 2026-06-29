Portfolio Performance - Daily Return Service

This small Spring Boot application exposes a service that calculates a daily return summary for a portfolio using start value, end value, net cash flow and benchmark movement, and validates whether the portfolio return is within an expected tolerance.

Build & Run

Requirements:
- Java 17
- Maven

From the project root:

```bash
mvn test
mvn spring-boot:run
```

API

POST /api/performance/daily-return
Request JSON fields:
- `portfolioId` (string, required)
- `valuationDate` (ISO date, optional)
- `beginMarketValue` (number, required)
- `endMarketValue` (number, required)
- `netCashFlow` (number, optional)
- `benchmarkReturnPct` (decimal, required, e.g. 0.01 = 1%)
- `currency` (string, required)
- `requestedBy` (string, required)
- `transactionId` (string, required, must be unique for each processed request)
- `accountId` (string, required)
- `transactionType` (string, required)
- `amount` (number, required)
- `sequenceNumber` (number, required)
- `requestId` (string, required)
- `status` (string, optional)
- `tolerance` (decimal, optional, default 0.005)

The API rejects duplicate `transactionId` values for requests that have already been processed in the current runtime.

Example Postman request body:

```json
{
  "portfolioId": "PORT1",
  "valuationDate": "2026-06-28",
  "beginMarketValue": 100000,
  "endMarketValue": 100800,
  "netCashFlow": 0,
  "benchmarkReturnPct": 0.007,
  "currency": "USD",
  "requestedBy": "investor@example.com",
  "transactionId": "TXN-98765",
  "accountId": "ACC-001",
  "transactionType": "DEPOSIT",
  "amount": 1000,
  "sequenceNumber": 1,
  "requestId": "REQ-12345",
  "status": "NEW",
  "tolerance": 0.005
}
```

Response: DailyReturnSummary JSON with:
- `dailyReturnPercent`
- `benchmarkReturnPercent`
- `excessReturnPercent`
- `status` (`VALID`, `REVIEW_REQUIRED`, `INVALID_INPUT`)
- `message`

Health check

GET `http://localhost:8080/api/performance/health`

Example response:
```json
{
  "status": "UP",
  "service": "Portfolio Performance"
}
```

Notes
- The service uses the assumption: return = ((end - begin - netCashFlow) / begin) * 100.
- Tolerance is absolute on the excess return difference (decimal).
- Run the app and hit Postman at `http://localhost:8080/api/performance/daily-return`.
