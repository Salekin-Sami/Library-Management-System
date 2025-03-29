-- Create book_copies table
CREATE TABLE book_copies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL,
    copy_number INT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'Available',
    location VARCHAR(255),
    price DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (book_id) REFERENCES books(id),
    UNIQUE KEY unique_book_copy (book_id, copy_number)
);

-- Remove copy-related columns from books table
ALTER TABLE books
DROP COLUMN copy_number,
DROP COLUMN total_copies;

-- Migrate existing data
INSERT INTO book_copies (book_id, copy_number, status, location, price, created_at, updated_at)
SELECT id, 1, status, location, price, created_at, updated_at
FROM books; 