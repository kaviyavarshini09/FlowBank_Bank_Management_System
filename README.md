# FlowBank_Bank_Management_System

A full-stack Java banking application for opening and managing accounts, recording deposits and withdrawals, transferring funds, and viewing per-account transaction history.

## Technology

- Java 21 and Spring Boot 3
- Spring Data JPA for persistence and atomic transaction processing
- H2 file database (data remains after restarting the app)
- Vanilla HTML, CSS, and JavaScript dashboard

## Run locally

1. Install JDK 21 or newer and Apache Maven.
2. From this folder, run:

   ```powershell
   mvn spring-boot:run
   ```

3. Open `http://localhost:8080` in a browser.

The H2 database console is also available at `http://localhost:8080/h2-console`. Use the JDBC URL `jdbc:h2:file:./data/bankdb`, username `sa`, and an empty password.

## API

| Method | Endpoint | Purpose |
| --- | --- | --- |
| GET | `/api/dashboard` | Account totals and recent activity |
| GET / POST | `/api/accounts` | List or create accounts |
| GET | `/api/accounts/{number}` | Get an account |
| POST | `/api/accounts/{number}/deposit` | Deposit funds |
| POST | `/api/accounts/{number}/withdraw` | Withdraw funds |
| GET | `/api/accounts/{number}/transactions` | Account transaction history |
| POST | `/api/transfers` | Transfer funds between accounts |

Transfers run in one database transaction: both account balance updates and both transaction records succeed or fail together.
