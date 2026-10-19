-- Crea la base deportal, las tablas y los datos de prueba.
-- Ejecutarlo una vez, antes de arrancar la aplicación:
--   mysql -u USUARIO -p < database/deportal.sql

CREATE DATABASE IF NOT EXISTS deportal
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE deportal;

CREATE TABLE sport_types (
  code varchar(32) NOT NULL,
  name varchar(80) NOT NULL,
  created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at datetime DEFAULT NULL,
  PRIMARY KEY (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE users (
  id bigint NOT NULL AUTO_INCREMENT,
  name varchar(120) NOT NULL,
  email varchar(180) NOT NULL,
  password_hash varchar(72) NOT NULL,
  role enum('ADMIN','CUSTOMER') NOT NULL,
  type enum('MEMBER','NON_MEMBER') NOT NULL,
  created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at datetime DEFAULT NULL,
  active_email varchar(180) GENERATED ALWAYS AS (CASE WHEN deleted_at IS NULL THEN email ELSE NULL END) STORED,
  PRIMARY KEY (id),
  UNIQUE KEY uq_users_active_email (active_email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE courts (
  id bigint NOT NULL AUTO_INCREMENT,
  sport_type_code varchar(32) NOT NULL,
  name varchar(120) NOT NULL,
  capacity smallint NOT NULL,
  opens_at time NOT NULL,
  closes_at time NOT NULL,
  hourly_rate decimal(10,2) NOT NULL,
  created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at datetime DEFAULT NULL,
  active_name varchar(120) GENERATED ALWAYS AS (CASE WHEN deleted_at IS NULL THEN name ELSE NULL END) STORED,
  PRIMARY KEY (id),
  UNIQUE KEY uq_courts_active_name (active_name),
  KEY idx_courts_sport_type_code (sport_type_code),
  CONSTRAINT fk_courts_sport_type FOREIGN KEY (sport_type_code) REFERENCES sport_types (code),
  CONSTRAINT chk_courts_capacity CHECK (capacity BETWEEN 1 AND 50),
  CONSTRAINT chk_courts_hourly_rate CHECK (hourly_rate >= 5.00),
  CONSTRAINT chk_courts_hours CHECK (opens_at >= '06:00:00' AND closes_at <= '22:00:00' AND opens_at < closes_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE reservations (
  id bigint NOT NULL AUTO_INCREMENT,
  user_id bigint NOT NULL,
  court_id bigint NOT NULL,
  reservation_date date NOT NULL,
  starts_at time NOT NULL,
  ends_at time NOT NULL,
  status enum('CONFIRMED','WAITING','CANCELLED') NOT NULL,
  base_amount decimal(10,2) NOT NULL,
  discount_amount decimal(10,2) NOT NULL,
  amount_paid decimal(10,2) NOT NULL,
  refund_amount decimal(10,2) DEFAULT NULL,
  cancelled_at datetime DEFAULT NULL,
  created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at datetime DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_reservations_court_date_status (court_id, reservation_date, status),
  KEY idx_reservations_user_id (user_id),
  CONSTRAINT fk_reservations_court FOREIGN KEY (court_id) REFERENCES courts (id),
  CONSTRAINT fk_reservations_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT chk_reservations_cancellation CHECK (
    (status = 'CANCELLED' AND cancelled_at IS NOT NULL AND refund_amount IS NOT NULL)
    OR (status <> 'CANCELLED' AND cancelled_at IS NULL AND refund_amount IS NULL)
  ),
  CONSTRAINT chk_reservations_duration CHECK (TIME_TO_SEC(TIMEDIFF(ends_at, starts_at)) BETWEEN 3600 AND 28800),
  CONSTRAINT chk_reservations_money CHECK (
    base_amount >= 0
    AND discount_amount >= 0
    AND discount_amount <= base_amount
    AND amount_paid >= 0
    AND (refund_amount IS NULL OR refund_amount >= 0)
  ),
  CONSTRAINT chk_reservations_window CHECK (starts_at >= '06:00:00' AND ends_at <= '22:00:00' AND starts_at < ends_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- La contraseña de todas las cuentas es 123456.
INSERT INTO sport_types (code, name) VALUES
  ('FOOTBALL', 'Fútbol'),
  ('BASKETBALL', 'Básquet'),
  ('TENNIS', 'Tenis'),
  ('VOLLEYBALL', 'Vóley'),
  ('MULTIUSOS', 'Multiusos');

INSERT INTO users (name, email, password_hash, role, type) VALUES
  ('Admin', 'admin@admin.com', '$2b$12$i2Zd9UbQZFn8vqD8iuczu.LhMJab2lUXXXW17dKjYrqz6qGQ5NTv2', 'ADMIN', 'MEMBER'),
  ('Juan Pérez', 'juan.perez@deportal.test', '$2b$12$i2Zd9UbQZFn8vqD8iuczu.LhMJab2lUXXXW17dKjYrqz6qGQ5NTv2', 'CUSTOMER', 'MEMBER'),
  ('María García', 'maria.garcia@deportal.test', '$2b$12$i2Zd9UbQZFn8vqD8iuczu.LhMJab2lUXXXW17dKjYrqz6qGQ5NTv2', 'CUSTOMER', 'NON_MEMBER'),
  ('Carlos López', 'carlos.lopez@deportal.test', '$2b$12$i2Zd9UbQZFn8vqD8iuczu.LhMJab2lUXXXW17dKjYrqz6qGQ5NTv2', 'CUSTOMER', 'MEMBER');

INSERT INTO courts (sport_type_code, name, capacity, opens_at, closes_at, hourly_rate) VALUES
  ('FOOTBALL', 'Cancha Central', 22, '06:00:00', '22:00:00', 20.00),
  ('BASKETBALL', 'Cancha Norte', 10, '07:00:00', '21:00:00', 15.00),
  ('TENNIS', 'Cancha Sur', 4, '08:00:00', '20:00:00', 25.00),
  ('MULTIUSOS', 'Multiusos Este', 16, '06:00:00', '22:00:00', 18.00);
