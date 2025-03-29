-- Add location and price columns to books table
ALTER TABLE books ADD COLUMN location VARCHAR(255);
ALTER TABLE books ADD COLUMN price DECIMAL(10,2);

-- Update status field to have a default value
ALTER TABLE books MODIFY COLUMN status VARCHAR(50) DEFAULT 'Available';

-- Remove the available field since we're using status now
ALTER TABLE books DROP COLUMN available; 