# Invoice Management System

A Spring Boot backend for automated invoice generation, client management,
and payment tracking. Built with production patterns — async processing,
scheduled automation, Flyway migrations, and PDF/email delivery.

---



---

## How It Works

### Invoice Lifecycle
Invoices move through a defined status workflow managed by `InvoiceService`:
DRAFT → SENT → PAID
↓
OVERDUE (set automatically by scheduler)

Invoice numbers follow the format `INV-YYYY-XXXX`.
Tax and discount calculations happen at the service layer.
Line item totals are auto-calculated as `quantity * unitPrice`.

### Recurring Invoices
`InvoiceService` includes a recurring engine that clones invoice templates
based on configured intervals (Weekly, Monthly, etc.) and auto-sends them.
Triggered daily at 6 AM by the scheduler.

### Automated Scheduling
Three scheduled jobs run without manual intervention:

| Time | Job |
|------|-----|
| Midnight | Marks overdue invoices via batch DB update |
| 6 AM | Generates and sends recurring invoices |
| 9 AM (Weekdays) | Sends payment reminders for upcoming due dates |

### Email & PDF
- `EmailService` uses `@Async` so SMTP calls never block the main thread
- Emails rendered as HTML via Thymeleaf templates
- `PdfGenerationService` converts the same Thymeleaf templates to
  PDF using iText7/html2pdf
- Both run on a dedicated thread pool configured in `AsyncConfig`

### Data Access
`InvoiceRepository` includes custom queries beyond standard CRUD:
- `markOverdueInvoices` — batch status update in a single transaction
  using `@Modifying` + `@Query`
- `sumTotalByUserAndStatus` — aggregated financial metrics for dashboards
- Case-insensitive search across invoice numbers and client names

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17+ |
| Framework | Spring Boot 3.2.3 |
| Security | Spring Security |
| ORM | Spring Data JPA / Hibernate |
| Database | PostgreSQL |
| Migrations | Flyway |
| Templating | Thymeleaf |
| PDF | iText7 / html2pdf |
| Email | JavaMailSender |
| Scheduling | Spring Scheduler |
| Boilerplate | Lombok |
| Build | Maven |

---

## Database Migrations

Flyway runs migrations automatically on startup.
V1__init_schema.sql   — tables, indexes, constraints
V2__seed_data.sql     — seed clients for local development

---

## API Endpoints

### Invoices
GET    /api/invoices           — list all invoices
GET    /api/invoices/{id}      — get by id
POST   /api/invoices           — create invoice
PUT    /api/invoices/{id}      — update invoice
DELETE /api/invoices/{id}      — delete invoice

### Clients
GET    /api/clients            — list all clients
GET    /api/clients/{id}       — get by id
POST   /api/clients            — create client
PUT    /api/clients/{id}       — update client
DELETE /api/clients/{id}       — delete client

---

## Running Locally

### Prerequisites
- Java 17+
- PostgreSQL
- Maven

### Steps

```bash
git clone https://github.com/santosh6672/Invoice-Management-System.git
cd Invoice-Management-System
```

Configure your database in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/invoicedb
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email
spring.mail.password=your_app_password
```

Run:

```bash
mvn spring-boot:run
```

Flyway runs V1 and V2 automatically on first startup.
`DataInitializer` seeds client data if the database is empty.

---

## Security

- Form-based login for management UI
- `/api/**` endpoints open for external frontend integration
- CSRF disabled on API paths for non-browser clients
- User roles: `ADMIN`, `USER`

---

## In Progress

Active development on the following modules:

- **Accounts Receivable (AR) Agent** — classifies overdue invoices
  into tiers (7d / 30d / 90d+), selects reminder strategy per tier,
  logs every agent action with full audit trail
- **Invoice Reconciliation (IR)** — matches incoming payments to
  invoices with partial payment handling and mismatch detection
- **Real-time Chat** — WebSocket dispute resolution between
  clients and support using STOMP protocol

---

## Author

Santosh Kuruventi
B.Tech CSE (AI/ML)
[GitHub](https://github.com/santosh6672)
