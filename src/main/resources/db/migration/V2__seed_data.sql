-- V2__seed_data.sql
-- Admin user is created programmatically by DataInitializer on startup
-- (avoids hardcoded BCrypt hashes that may not match the encoder config)

-- Sample clients
INSERT INTO clients (name, email, phone, company_name, address, city, country, postal_code, payment_terms_days)
VALUES
    ('Alice Johnson', 'alice@example.com', '+1-555-0101', 'TechCorp Inc.', '456 Tech Blvd', 'San Francisco', 'USA', '94102', 30),
    ('Bob Smith', 'bob@acmecorp.com', '+1-555-0102', 'Acme Corporation', '789 Commerce St', 'Austin', 'USA', '78701', 15),
    ('Carol White', 'carol@startupxyz.io', '+1-555-0103', 'StartupXYZ', '321 Innovation Dr', 'Seattle', 'USA', '98101', 45);
