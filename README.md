# Transaction Risk Engine

An event-driven financial transaction risk monitoring platform built with Spring Boot, Kafka, PostgreSQL, and AI-assisted risk explanations.

The project is being developed incrementally as a multi-service platform.

## Current Services

```text
transaction-risk-engine/
  services/
    transaction-ingestion-service/
    risk-engine-service/
    alert-service/
```
### Transaction Ingestion Service
The transaction-ingestion-service is now able to receive transaction requests through a REST API, validate customer/account information, persist transaction records, save outbox events, publish messages to Kafka, and audit consumed Kafka events.

**Architecture**

```angular2html
Client / Postman
    |
    v
Transaction REST API
    |
    v
TransactionIngestionService
    |
    |-- validates customer
    |-- validates account
    |-- validates idempotency key
    |
    v
PostgreSQL
    |
    |-- transactions
    |-- outbox_events
    |
    v
Outbox Publisher Job
    |
    v
Kafka topic: transaction.created.v1
    |
    v
Kafka Consumer
    |
    v
transaction_event_audit
```
Flow

```angular2html
1. Client submits transaction using POST /api/transactions
2. Service validates customer and account
3. Service checks idempotency key
4. Transaction is saved in transactions table
5. TransactionCreatedEvent is saved in outbox_events table
6. Outbox publisher reads pending events
7. Event is published to Kafka topic transaction.created.v1
8. Kafka consumer receives the event
9. Consumer updates transaction_event_audit table
```

## Local Run Instructions

### Prerequisites
Make sure the following tools are installed:
```angular2html
Java 17 or Java 21
Maven
Docker
Docker Compose
Postman
```
1. Start Local Infrastructure

From the root folder:
```text
cd transaction-risk-engine
docker compose up -d
```
This should start:

```
PostgreSQL
Kafka
```

Verify containers are running:

```docker ps```

Expected containers:

```
transaction-risk-engine-postgres
transaction-risk-engine-kafka
```
2. Verify PostgreSQL

Connect to PostgreSQL:

```docker exec -it transaction-risk-engine-postgres psql -U transaction_risk_user -d transaction_risk_engine```

List tables:

```\dt```

Expected tables:

```
customers
accounts
transactions
outbox_events
transaction_event_audit
flyway_schema_history
```

Exit PostgreSQL:

```\q```
3. Run Transaction Ingestion Service

Navigate to the service folder:

```
cd services/transaction-ingestion-service
```
Run the Spring Boot application:

```mvn spring-boot:run```

The service should start on:

```http://localhost:8080```
4. Verify Kafka Topic

The transaction events are published to:

```transaction.created.v1```
To list Kafka topics:

```
docker exec -it transaction-risk-engine-kafka kafka-topics.sh \
--bootstrap-server localhost:9092 \
--list
```

Expected topic:

```transaction.created.v1```
