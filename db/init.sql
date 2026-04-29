CREATE TABLE IF NOT EXISTS app_user (
    id SERIAL PRIMARY KEY,
    login TEXT UNIQUE NOT NULL CHECK (login <> ''),
    name TEXT NOT NULL CHECK (name <> ''),
    email TEXT UNIQUE NOT NULL CHECK (email <> ''),
    password TEXT NOT NULL CHECK (password <> ''),
    role TEXT NOT NULL CHECK (role <> '')
);

INSERT INTO app_user (login, name, email, password, role)
VALUES ('admin', 'Admin', 'admin@mail.ru', '$2a$10$v7mCqVeo9iyLMUtdqoecNu/iLCqgbJfNvrr2lDTnkiyqB/LHZeAW2', 'ROLE_ADMIN'),
       ('user1', 'User One', 'user1@mail.ru', '$2a$10$nF10lKIg55m/q2PwoL0JJeCPO0Efi4KDL7S0QMpCaLrBuMK5Rb.u6', 'ROLE_USER'),
       ('host1', 'Host One', 'host1@mail.ru', '$2a$10$nF10lKIg55m/q2PwoL0JJeCPO0Efi4KDL7S0QMpCaLrBuMK5Rb.u6', 'ROLE_HOST');

CREATE TABLE IF NOT EXISTS accommodation (
    id SERIAL PRIMARY KEY,
    host_id INTEGER NOT NULL REFERENCES app_user,
    name TEXT NOT NULL CHECK (name <> ''),
    description TEXT NOT NULL CHECK (name <> ''),
    max_guests_number SMALLINT NOT NULL CHECK (max_guests_number > 0),
    beds_count SMALLINT NOT NULL CHECK (beds_count >= 0),
    address TEXT NOT NULL CHECK (address <> ''),
    rating REAL CHECK (rating >= 0),
    price_per_night BIGINT NOT NULL CHECK (price_per_night > 0),
    is_published BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS booking (
    id SERIAL PRIMARY KEY,
    accommodation_id INTEGER NOT NULL REFERENCES accommodation,
    user_id INTEGER NOT NULL REFERENCES app_user,
    check_in DATE NOT NULL,
    check_out DATE NOT NULL CHECK (check_out > check_in),
    price BIGINT NOT NULL CHECK (price > 0)
);

CREATE TABLE IF NOT EXISTS payment_data (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES app_user,
    payment_method_name TEXT NOT NULL CHECK (payment_method_name <> '')
);

CREATE TABLE IF NOT EXISTS payment_confirmation (
    id SERIAL PRIMARY KEY,
    payment_data_id INTEGER NOT NULL REFERENCES payment_data,
    booking_id INTEGER NOT NULL REFERENCES booking
);

CREATE TABLE IF NOT EXISTS booking_request (
    id SERIAL PRIMARY KEY,
    accommodation_id INTEGER NOT NULL REFERENCES accommodation,
    client_id INTEGER NOT NULL REFERENCES app_user,
    host_id INTEGER NOT NULL REFERENCES app_user,
    payment_data_id INTEGER NOT NULL REFERENCES payment_data,
    check_in DATE NOT NULL,
    check_out DATE NOT NULL CHECK (check_out > check_in),
    message_to_host TEXT NOT NULL CHECK (message_to_host <> '')
);

CREATE TABLE IF NOT EXISTS yookassa_payment_data (
    payment_data_id INTEGER PRIMARY KEY REFERENCES payment_data,
    yookassa_payment_id TEXT NOT NULL CHECK (yookassa_payment_id <> '')
);
