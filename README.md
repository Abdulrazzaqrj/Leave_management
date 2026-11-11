# Leave Management System

A Java 17 / Spring Boot REST API and browser frontend for employee leave requests and manager decisions. MySQL is used for persistence.

## Requirements
- JDK 17+
- Maven 3.8+
- MySQL 8+

## Run locally
1. Create a database (optional; the JDBC URL creates it automatically): `CREATE DATABASE leave_management;`
2. Update `src/main/resources/application.properties` or set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` environment variables.
3. From this directory run: `mvn spring-boot:run`
4. Open http://localhost:8080

Hibernate creates/updates tables using `ddl-auto=update`. For production, use Flyway/Liquibase migrations and set `ddl-auto=validate`.

## Demo accounts
- Manager: `manager@company.com` / `Manager@123`
- Employee: `davis@company.com` / `Employee@123`
- Employee: `lina@company.com` / `Employee@123`

Demo accounts are inserted only when the employee table is empty. Change these passwords before deployment.

## REST API
All endpoints except login and static assets require a session created by `POST /api/auth/login`.

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| POST | `/api/auth/login` | Public | Login; returns profile and sets session cookie |
| GET | `/api/auth/me` | Authenticated | Current user profile |
| POST | `/api/auth/logout` | Authenticated | End session |
| GET | `/api/employee/profile` | Employee | Profile and balances |
| POST | `/api/employee/leaves` | Employee | Apply for leave |
| GET | `/api/employee/leaves` | Employee | Own leave history |
| GET | `/api/manager/leaves` | Manager | All requests |
| PUT | `/api/manager/leaves/{id}/approve` | Manager | Approve pending request |
| PUT | `/api/manager/leaves/{id}/reject` | Manager | Reject pending request |
| GET | `/api/manager/employees` | Manager | Employee balances |

### Apply leave JSON
```json
{"type":"ANNUAL","startDate":"2026-12-01","endDate":"2026-12-03","reason":"Personal work"}
```
Types: `ANNUAL`, `SICK`, `OTHER`. Statuses: `PENDING`, `APPROVED`, `REJECTED`.

### Manager decision JSON
```json
{"comment":"Approved. Enjoy your leave."}
```

## Business rules
- Leave dates are inclusive calendar days (weekends are counted).
- Start date must be today or later; end date must not precede start date.
- Pending and approved requests cannot overlap for the same employee.
- The requested type's available balance is checked on submission.
- Balance is deducted only on approval; rejected requests do not affect balance.
- Only pending requests can be decided.

## Notes for production
This is a runnable starter application. Before production, add database migrations, HTTPS, stronger password provisioning/reset, audit logging, rate limiting, CSRF protection appropriate to your deployment, and automated integration tests. If you need business-day counting or holiday calendars, extend the day calculation service.
