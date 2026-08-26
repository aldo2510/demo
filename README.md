# Fictitious Insurance — Insurance Core API

Enterprise-style REST API for **Fictitious Insurance**, built with Java 25, Spring Boot 4.1 and Maven.

## Architecture

```text
HTTP Client
    |
    v
REST Controllers
    |
    v
Application Services
    |
    +---- Domain rules
    |
    +---- Thread-safe in-memory repository
    |
    +---- External Risk API (RestClient)
```

The project intentionally starts without a database so it can be demonstrated locally with zero infrastructure. Persistence is represented by thread-safe in-memory stores and can later be replaced by PostgreSQL without changing the API contract.

## Main capabilities

### Customers
- Create customer
- Retrieve customer
- Duplicate document validation

### Policies
- Create an active policy
- Calculate premium from product and insured amount
- Validate customer existence
- Validate policy date range
- Retrieve policy

Supported demo products:
- `AUTO_STANDARD` — 3.5% premium rate
- `AUTO_PREMIUM` — 5.0% premium rate
- Other products — 4.0% default demo rate

### Claims
- Register a claim against an active policy
- Validate that the estimated loss does not exceed the insured amount
- Retrieve claim
- Enforce claim state transitions

```text
REGISTERED -> UNDER_REVIEW -> APPROVED -> PAID
                         \\-> REJECTED
```

### External integration

`GET /api/v1/risk/{reference}` calls the configured external Risk API using Spring `RestClient`. The default endpoint is HTTPBin and is intentionally used as a safe integration stub for the demo.

## API endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/v1/customers` | Create customer |
| GET | `/api/v1/customers/{id}` | Get customer |
| POST | `/api/v1/policies` | Issue policy |
| GET | `/api/v1/policies/{id}` | Get policy |
| POST | `/api/v1/claims` | Register claim |
| GET | `/api/v1/claims/{id}` | Get claim |
| PATCH | `/api/v1/claims/{id}/status?status=UNDER_REVIEW` | Advance claim state |
| GET | `/api/v1/risk/{reference}` | Call external risk integration |
| GET | `/actuator/health` | Health check |
| GET | `/actuator/metrics` | Application metrics |

## Example flow

### 1. Create customer

```bash
curl -X POST http://localhost:8080/api/v1/customers \\
  -H 'Content-Type: application/json' \\
  -d '{
    "documentNumber": "70123456",
    "fullName": "Maria Lopez",
    "email": "maria@example.com"
  }'
```

### 2. Issue policy

```bash
curl -X POST http://localhost:8080/api/v1/policies \\
  -H 'Content-Type: application/json' \\
  -d '{
    "customerId": "CUSTOMER_UUID",
    "productCode": "AUTO_STANDARD",
    "vehiclePlate": "ABC-123",
    "insuredAmount": 50000,
    "startDate": "2026-01-01",
    "endDate": "2026-12-31"
  }'
```

### 3. Register claim

```bash
curl -X POST http://localhost:8080/api/v1/claims \\
  -H 'Content-Type: application/json' \\
  -d '{
    "policyId": "POLICY_UUID",
    "incidentType": "COLLISION",
    "estimatedLoss": 12000,
    "description": "Vehicle collision claim"
  }'
```

### 4. Move claim through the workflow

```bash
curl -X PATCH 'http://localhost:8080/api/v1/claims/CLAIM_UUID/status?status=UNDER_REVIEW'
curl -X PATCH 'http://localhost:8080/api/v1/claims/CLAIM_UUID/status?status=APPROVED'
curl -X PATCH 'http://localhost:8080/api/v1/claims/CLAIM_UUID/status?status=PAID'
```

## Run locally

Requirements:
- Java 25
- Maven 3.9+

```bash
mvn clean verify
mvn spring-boot:run
```

## CI

GitHub Actions executes:
1. Checkout
2. Java 25 setup with Maven cache
3. Compile
4. Unit tests
5. Checkstyle quality gate
6. JaCoCo coverage report
7. JAR packaging
8. Artifact upload

The workflow is triggered by pushes and pull requests targeting `main`.

## DevSecOps lab

This branch includes a **pre-built 50-minute laboratory** focused on false-positive management and severity-based Quality Gates.

Start here:

➡️ **[LAB.md](./LAB.md)**

The exercise is already automated with Semgrep, a severity policy, a controlled false-positive exception and a GitHub Actions Quality Gate. The learner does not build the pipeline from scratch; the lab focuses on observing, analyzing, documenting the false positive and remediating the real CRITICAL finding.

## Next enterprise evolution

- PostgreSQL + Flyway
- Spring Data JPA
- Redis caching
- Kafka domain events
- OAuth2/JWT security
- OpenAPI/Swagger
- Resilience4j retries, timeout and circuit breaker
- SonarQube/SonarCloud
- SCA and container scanning
- Docker image
- Kubernetes deployment
- OpenTelemetry observability
