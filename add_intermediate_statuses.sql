-- Migration to add STOPPING and RESTARTING statuses to containers table
-- Run this SQL against your PostgreSQL database

-- Drop the old check constraint
ALTER TABLE containers DROP CONSTRAINT IF EXISTS containers_status_check;

-- Add the new check constraint with STOPPING and RESTARTING
ALTER TABLE containers ADD CONSTRAINT containers_status_check 
    CHECK (status IN ('PENDING', 'CREATING', 'RUNNING', 'STOPPING', 'STOPPED', 'RESTARTING', 'FAILED', 'DELETED'));

-- Verify the constraint was added
SELECT conname, contype, pg_get_constraintdef(oid) 
FROM pg_constraint 
WHERE conrelid = 'containers'::regclass AND conname = 'containers_status_check';
