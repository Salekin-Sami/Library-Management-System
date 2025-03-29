-- First ensure status has a default value and is not nullable
ALTER TABLE books MODIFY status VARCHAR(50) NOT NULL DEFAULT 'Available';

-- Then drop the available column if it exists
ALTER TABLE books DROP COLUMN IF EXISTS available;

-- Update any existing books that might have null status
UPDATE books SET status = 'Available' WHERE status IS NULL; 