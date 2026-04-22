# 📄 Invoice Generation System

A full-featured automated invoice management system built with **Java Spring Boot**, featuring PDF generation, email notifications, payment tracking, and scheduled recurring invoices.

---

## ✨ Features

| Feature | Details |
|---|---|
| **Invoice Management** | Create, send, track, and cancel invoices with full lifecycle management |
| **PDF Generation** | Professional PDF invoices generated with iText + Thymeleaf templates |
| **Email Notifications** | Automated emails for invoice delivery, payment confirmations, reminders |
| **Payment Tracking** | Track DRAFT → SENT → PAID → OVERDUE status transitions |
| **Recurring Invoices** | Weekly, Monthly, Quarterly, or Annual auto-generated invoices |
| **Scheduled Jobs** | Daily overdue checks, recurring invoice processing, payment reminders |
| **Client Management** | Full CRUD for clients with invoice history |
| **Search & Filter** | Search invoices by number or client, filter by status |
| **Dashboard** | Revenue stats, pending/overdue amounts, recent invoices |
| **Security** | Spring Security authentication, per-user invoice isolation |

---

## 🏗️ Tech Stack

- **Backend**: Java 17, Spring Boot 3.2
- **Templates**: Thymeleaf (web UI + email + PDF)
- **PDF**: iText 8 + html2pdf
- **Database**: H2 (dev) / PostgreSQL (production)
- **Migrations**: Flyway
- **Email**: Spring Mail (JavaMailSender)
- **Scheduler**: Spring `@Scheduled`
- **Security**: Spring Security 6
- **Build**: Maven

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+

### Run locally (H2 in-memory)

```bash
git clone https://github.com/YOUR_USERNAME/invoice-system.git
cd invoice-system
mvn spring-boot:run
```

Open `http://localhost:8080`

**Default credentials:** `admin` / `admin123`

### H2 Console (dev)
`http://localhost:8080/h2-console`

---

## ⚙️ Configuration

### Email (application.properties)
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=YOUR_EMAIL
spring.mail.password=YOUR_APP_PASSWORD
```

> For Gmail: enable 2FA and generate an [App Password](https://myaccount.google.com/apppasswords)

### Production (PostgreSQL)

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod \
  -DDB_HOST=localhost \
  -DDB_NAME=invoicedb \
  -DDB_USERNAME=postgres \
  -DDB_PASSWORD=yourpassword \
  -DMAIL_USERNAME=you@gmail.com \
  -DMAIL_PASSWORD=app_password
```

---

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/invoicesystem/
│   │   ├── controller/       # MVC Controllers
│   │   ├── service/          # Business Logic
│   │   │   ├── InvoiceService.java
│   │   │   ├── PdfGenerationService.java
│   │   │   └── EmailService.java
│   │   ├── scheduler/        # Scheduled Jobs
│   │   ├── repository/       # JPA Repositories
│   │   ├── model/            # JPA Entities
│   │   ├── dto/              # Data Transfer Objects
│   │   └── config/           # Security & Async Config
│   └── resources/
│       ├── templates/
│       │   ├── invoice/      # Invoice HTML templates
│       │   ├── client/       # Client HTML templates
│       │   ├── email/        # Email templates
│       │   └── fragments/    # Navbar, etc.
│       ├── static/           # CSS, JS
│       └── db/migration/     # Flyway SQL migrations
└── test/
    └── java/com/invoicesystem/service/
```

---

## 📊 Invoice Lifecycle

```
DRAFT ──(send)──► SENT ──(pay)──► PAID
  │                  │
  │             (overdue)
  │                  ▼
  └──(cancel)──► OVERDUE ──(pay)──► PAID
                     │
                (cancel)──► CANCELLED
```

---

## 🕐 Scheduled Jobs

| Job | Schedule | Description |
|---|---|---|
| Overdue Check | Daily at midnight | Marks SENT invoices past due date as OVERDUE |
| Recurring Invoices | Daily at 6 AM | Auto-creates and sends due recurring invoices |
| Payment Reminders | Weekdays at 9 AM | Sends email reminders for invoices due within 3 days |
| Weekly Summary | Mondays at 8 AM | Hook for weekly admin reports |

---

## 🧪 Running Tests

```bash
mvn test
```

---

## 📝 License

MIT License
