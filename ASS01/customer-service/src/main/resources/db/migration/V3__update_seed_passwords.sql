-- Update seed customer passwords to BCrypt hash of 'Abc@1234'
-- Flyway V3: Standardize test customer passwords
UPDATE customer
SET password = '$2a$10$HF/WqQLmPgbidn9c4xUTueHnIv5lUeMw0J9Wge/CZ.Bb.KSJa5nLm'
WHERE email IN ('an@gmail.com', 'binh@gmail.com', 'chi@gmail.com');

-- Remove temp hash test users if they exist
DELETE FROM customer WHERE email LIKE '%temphash%';
