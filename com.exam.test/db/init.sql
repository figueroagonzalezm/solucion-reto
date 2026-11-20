CREATE TABLE IF NOT EXISTS clients (
    id         INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    first_name VARCHAR(100),
    last_name  VARCHAR(100),
    surname    VARCHAR(100),
    phone      VARCHAR(30)
);
