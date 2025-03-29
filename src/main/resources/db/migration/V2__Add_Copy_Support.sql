-- Add copy support columns
ALTER TABLE books
ADD COLUMN copy_number INT DEFAULT 1,
ADD COLUMN total_copies INT DEFAULT 1;

-- Update existing books to have copy_number = 1 and total_copies = 1
UPDATE books SET copy_number = 1, total_copies = 1 WHERE copy_number IS NULL; 