# API Gateway Service

Spring Cloud Gateway entry point for the Transaction Risk Engine platform.

## Local URL

```text
http://localhost:8088
```

## Routes

| Gateway path | Downstream service |
| --- | --- |
| `/api/v1/transactions/**` | `transaction-ingestion-service` at `http://localhost:8080` |
| `/api/v1/risk-decisions/**` | `risk-engine-service` at `http://localhost:8081` |
| `/api/v1/alerts/**` | `alert-service` at `http://localhost:8082` |

The transaction route rewrites `/api/v1/transactions/**` to `/api/transactions/**`
because the ingestion service currently exposes `/api/transactions`.

## Configuration

Downstream URLs can be overridden with environment variables:

```text
TRANSACTION_INGESTION_SERVICE_URL=http://localhost:8080
RISK_ENGINE_SERVICE_URL=http://localhost:8081
ALERT_SERVICE_URL=http://localhost:8082
```

## Run

```bash
mvn spring-boot:run
```
