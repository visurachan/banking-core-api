# banking-core-api

A production-grade banking REST API built with **Java 21**, **Spring Boot 3**, and **PostgreSQL**. Designed as a portfolio project to demonstrate fintech-relevant backend engineering — double-entry ledger accounting, transactional integrity, and JWT-based authentication.

> 🚧 **Active Development** — core banking operations are implemented; additional features listed below are in progress.

---

## Why This Project

Instead of using a simple balance column updated with `balance + amount`, this project implements a **double-entry ledger** — every transaction creates two ledger entries (debit + credit) and the account balance is always **derived** from the ledger, never stored directly. This mirrors how production fintech systems handle money movement.

---

## Features

### ✅ Implemented

- **JWT Authentication** — register, login, token validation via `JwtAuthFilter` extending `OncePerRequestFilter`
- **Account Management** — create current accounts, retrieve account details and balances
- **Deposit** — funds flow from `BANK-INTERNAL` account to customer account via double-entry ledger
- **Withdraw** — funds flow from customer account back to `BANK-INTERNAL`
- **Transfer** — peer-to-peer transfers between accounts with ownership validation
- **Double-Entry Ledger** — every operation produces a DEBIT and a CREDIT entry; balance is always calculated from ledger history
- **Transactional Integrity** — `@Transactional` with PENDING → COMPLETED/FAILED status pattern; audit log survives rollback via `REQUIRES_NEW` propagation
- **Exception Handling** — custom exceptions with consistent error responses

### 🚧 In Progress / Planned

- **Transaction History** — paginated endpoint to retrieve ledger entries per account
- **Redis Caching** — cache derived balances to avoid full ledger scan on every request
- **Idempotency Keys** — prevent duplicate transactions on retried requests
- **Rate Limiting** — per-user request throttling
- **Currency Support** — multi-currency accounts with exchange rate handling

---

## Tech Stack

| Layer | Technology                 |
|---|----------------------------|
| Language | Java 21                    |
| Framework | Spring Boot 3.5            |
| Security | Spring Security, JWT (JWT) |
| Database | PostgreSQL                 |
| Migrations | Flyway                     |
| Containerisation | Docker Compose             |
| Build Tool | Maven                      |
| Utilities | Lombok, MapStruct          |

---

## Architecture Decisions

**Double-entry ledger over a balance column**
Balance is never stored — it is derived by summing ledger entries (`SUM(credits) - SUM(debits)`). This gives a full, immutable audit trail and is how real banking cores work.

**PENDING → COMPLETED/FAILED transaction log pattern**
Before any ledger writes, a `PENDING` transaction log is saved. If the operation succeeds, it is promoted to `COMPLETED`. If anything fails, the catch block marks it `FAILED`. This ensures every attempted transaction is recorded regardless of outcome.

**`REQUIRES_NEW` propagation on `TransactionLogService`**
The transaction log is saved and updated in its own independent database transaction. This means even if the outer `@Transactional` rolls back (e.g. a failed ledger write), the audit log entry survives. The ledger stays consistent; the audit trail is never lost.

**Manual `JwtAuthFilter` over Spring's default**
A custom `OncePerRequestFilter` extracts email and role from the JWT and sets the `SecurityContext` directly. This keeps the auth flow explicit, testable, and free of `UserDetailsService` overhead for stateless token validation.

**Feature-based packaging**
Code is organised by domain (`account`, `auth`, `transaction`) rather than layer (`controller`, `service`, `repository`). This scales better and makes each feature self-contained.

---

## Getting Started

### Prerequisites

- Java 21
- Docker & Docker Compose
- Maven

### Run Locally

```bash
# Clone the repo
git clone https://github.com/visurachandula/banking-core-api.git
cd banking-core-api

# Start PostgreSQL
docker-compose up -d

# Run the application
./mvnw spring-boot:run
```

### Environment Variables

Create an `application.properties` or set these as environment variables:

```
spring.datasource.url=jdbc:postgresql://localhost:5432/banking
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password
jwt.secret=your_base64_encoded_secret
jwt.expiration=86400000
```

---

## API Endpoints

### Auth — `/api/v1/auth`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/register` | Public | Register a new user |
| POST | `/login` | Public | Login and receive JWT |

### Accounts — `/api/v1/account`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/newCurrentAccount` | Bearer Token | Create a new current account |
| GET | `/myAllAccounts` | Bearer Token | Get all accounts for logged-in user |
| GET | `/accountDetails?accountNumber=` | Bearer Token | Get details for a specific account |

### Transactions — `/api/v1/account`

| Method | Endpoint | Auth                                   | Description |
|---|---|----------------------------------------|---|
| POST | `/{accountNumber}/deposit` | Public                                 | Deposit funds into an account |
| POST | `/{accountNumber}/withdraw` | Public (needs seperate authentication) | Withdraw funds from an account |
| POST | `/{myAccountNumber}/transfer` | Bearer Token                           | Transfer funds to another account |

---


## Project Status

This project is under active development as a portfolio piece targeting backend engineering roles in fintech. The core banking operations and transactional patterns are complete. Upcoming work focuses on performance (Redis caching), reliability (idempotency), and observability (transaction history).