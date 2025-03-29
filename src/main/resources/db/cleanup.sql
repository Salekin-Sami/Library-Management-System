-- Drop the foreign key constraint first
ALTER TABLE reservations DROP FOREIGN KEY FKrsdd3ib3landfpmgoolccjakt;

-- Then drop the reservations table
DROP TABLE IF EXISTS reservations; 