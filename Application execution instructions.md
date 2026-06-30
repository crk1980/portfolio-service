# Application Execution Instructions

## 1. Start the Application

From the project root (`c:\gitrepo\PFLatest\portfolio-service`), run:

```bash
mvn spring-boot:run
```

This starts the Spring Boot application on port `8080` by default.

If you prefer to build and run the jar:

```bash
mvn clean package
java -jar target/portfolio-performance-0.0.1-SNAPSHOT.jar
```

## 2. Available API Endpoints

Base URL:

- `http://localhost:8080/api/performance`

Endpoints:

- Daily return calculation: `POST http://localhost:8080/api/performance/daily-return`
- Attribution calculation: `POST http://localhost:8080/api/performance/attribution`
- Health check: `GET http://localhost:8080/api/performance/health`

## 3. Run JSON Test Cases

A sample request file is provided at:

- `input_json_FDE_L2`

To test the attribution endpoint with that sample file:

```bash
curl -X POST http://localhost:8080/api/performance/attribution \
  -H "Content-Type: application/json" \
  -d @input_json_FDE_L2
```

If your client requires a single request object, send one element from the array rather than the whole file.

## 4. Run JUnit Test Cases

From the project root, execute:

```bash
mvn test
```

This runs the full JUnit suite, including unit tests under `src/test/java`.

To run only the service tests:

```bash
mvn -Dtest=PortfolioPerformanceServiceTest test
```

## 5. Notes

- The attribution sample file includes cases for:
  - valid request with fallback pricing
  - valid request with fixed income fallback
  - idempotent duplicate request handling
  - invalid total weight rejection

- The application uses port `8080` unless overridden by `src/main/resources/application.properties`.
- Ensure the application is running before sending API requests or running JSON payload tests.
