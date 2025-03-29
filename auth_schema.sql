-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('admin', 'student') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    temp_password VARCHAR(255),
    temp_password_expiry TIMESTAMP,
    password_reset_required BOOLEAN DEFAULT FALSE
);

-- Insert default admin account (password: Admin@123)
INSERT INTO users (email, password_hash, role) 
VALUES ('admin@library.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBpwTTyU3I2K6', 'admin')
ON DUPLICATE KEY UPDATE id=id; 