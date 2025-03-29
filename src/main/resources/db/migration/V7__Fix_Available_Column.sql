-- Drop the available column if it exists
ALTER TABLE books DROP COLUMN IF EXISTS available;

-- Ensure status has correct default value and is not nullable
ALTER TABLE books MODIFY status VARCHAR(50) NOT NULL DEFAULT 'Available';

-- Update any null status values to 'Available'
UPDATE books SET status = 'Available' WHERE status IS NULL; 