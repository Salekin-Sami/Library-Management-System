-- Users table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    temp_password VARCHAR(255),
    temp_password_expiry TIMESTAMP,
    password_reset_required BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create test admin user
INSERT INTO users (email, password_hash, role) 
VALUES ('sirajussalekin23522@gmail.com', '$2a$10$8K1p/a0dL1LXMIgoEDFrwOQDyxw4CuIyQo8p3B6.Z3g1uC9z7PtDy', 'admin')
ON DUPLICATE KEY UPDATE email = email; 