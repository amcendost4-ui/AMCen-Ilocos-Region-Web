-- DOST AMCen Booking System Database Schema with Authentication

CREATE DATABASE IF NOT EXISTS amcen_bookings;
USE amcen_bookings;

-- Bookings Table
CREATE TABLE IF NOT EXISTS bookings (
  id INT AUTO_INCREMENT PRIMARY KEY,
  firstName VARCHAR(100) NOT NULL,
  lastName VARCHAR(100) NOT NULL,
  email VARCHAR(100) NOT NULL,
  phone VARCHAR(20) NOT NULL,
  institution VARCHAR(150),
  dateRequested DATE NOT NULL,
  timeRequested TIME NOT NULL,
  purpose VARCHAR(200) NOT NULL,
  facility VARCHAR(200) NOT NULL,
  participants INT,
  requests LONGTEXT,
  status ENUM('pending', 'approved', 'rejected', 'completed') DEFAULT 'pending',
  createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  notes LONGTEXT,
  INDEX idx_status (status),
  INDEX idx_email (email),
  INDEX idx_dateRequested (dateRequested),
  INDEX idx_createdAt (createdAt)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Admin Users Table
CREATE TABLE IF NOT EXISTS admin_users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  databaseId VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  name VARCHAR(100),
  email VARCHAR(100),
  role ENUM('admin', 'manager', 'staff') DEFAULT 'staff',
  active BOOLEAN DEFAULT TRUE,
  createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  lastLogin TIMESTAMP NULL,
  INDEX idx_databaseId (databaseId),
  INDEX idx_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default admin user (password: admin123)
INSERT INTO admin_users (databaseId, password, name, role) 
VALUES ('admin', 'ee26b0dd4af7e749aa1a8ee3c10ae9923f618980772e473f8819a5d4940e0db99', 'System Administrator', 'admin');

-- Import this file in MySQL:
-- mysql -u root -p amcen_bookings < schema.sql
