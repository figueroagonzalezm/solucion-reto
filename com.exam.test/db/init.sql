CREATE TABLE IF NOT EXISTS clients (
    id         INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    first_name VARCHAR(100),
    last_name  VARCHAR(100),
    surname    VARCHAR(100),
    phone      VARCHAR(30),
    email      VARCHAR(150)
);

-- For databases created before the email column existed
ALTER TABLE clients ADD COLUMN IF NOT EXISTS email VARCHAR(150);
