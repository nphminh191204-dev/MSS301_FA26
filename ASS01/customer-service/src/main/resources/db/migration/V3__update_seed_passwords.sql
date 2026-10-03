-- Update seed customer passwords to BCrypt hash of '123456'
-- Flyway V3: Standardize test customer passwords
UPDATE customer
SET password = '$2a$10$dmoDdVpWYdqLarqBfkYQteoq1YORLC5LLMd55bpomZ3EarS/vtjtW'
WHERE email IN ('an@gmail.com', 'binh@gmail.com', 'chi@gmail.com');

-- Remove temp hash test users if they exist
DELETE FROM customer WHERE email LIKE '%temphash%';
