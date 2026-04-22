-- V1__init_schema.sql
-- Initial database schema for Invoice System

CREATE TABLE IF NOT EXISTS users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    email       VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    full_name   VARCHAR(100),
    company_name     VARCHAR(100),
    company_address  TEXT,
    company_phone    VARCHAR(30),
    company_tax_number VARCHAR(50),
    logo_path   VARCHAR(255),
    is_enabled  BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role    VARCHAR(30) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS clients (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,
    email               VARCHAR(100) NOT NULL UNIQUE,
    phone               VARCHAR(30),
    company_name        VARCHAR(100),
    tax_number          VARCHAR(50),
    address             TEXT,
    city                VARCHAR(100),
    country             VARCHAR(100),
    postal_code         VARCHAR(20),
    payment_terms_days  INT DEFAULT 30,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS invoices (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_number       VARCHAR(30) NOT NULL UNIQUE,
    client_id            BIGINT NOT NULL,
    user_id              BIGINT NOT NULL,
    status               VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    issue_date           DATE NOT NULL,
    due_date             DATE NOT NULL,
    paid_date            DATE,
    subtotal             DECIMAL(10, 2),
    tax_rate             DECIMAL(5, 2) DEFAULT 18.00,
    tax_amount           DECIMAL(10, 2),
    discount_amount      DECIMAL(10, 2) DEFAULT 0.00,
    total_amount         DECIMAL(10, 2),
    currency             VARCHAR(3) DEFAULT 'USD',
    notes                TEXT,
    terms                TEXT,
    is_recurring         BOOLEAN NOT NULL DEFAULT FALSE,
    recurrence_interval  VARCHAR(20),
    next_recurrence_date DATE,
    reminder_sent        BOOLEAN NOT NULL DEFAULT FALSE,
    pdf_path             VARCHAR(512),
    created_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES clients(id),
    FOREIGN KEY (user_id)   REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS invoice_items (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id  BIGINT NOT NULL,
    description VARCHAR(255) NOT NULL,
    quantity    DECIMAL(10, 2) NOT NULL,
    unit_price  DECIMAL(10, 2) NOT NULL,
    line_total  DECIMAL(10, 2),
    sort_order  INT DEFAULT 0,
    FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_invoices_user_id    ON invoices(user_id);
CREATE INDEX IF NOT EXISTS idx_invoices_client_id  ON invoices(client_id);
CREATE INDEX IF NOT EXISTS idx_invoices_status     ON invoices(status);
CREATE INDEX IF NOT EXISTS idx_invoices_due_date   ON invoices(due_date);
CREATE INDEX IF NOT EXISTS idx_invoice_items_invoice ON invoice_items(invoice_id);
