-- Cleanup script to mark orphaned containers as DELETED
-- Run this in PostgreSQL when Docker containers are removed but database still shows them as RUNNING

-- See current containers for user 4 (student 500119484)
SELECT id, container_id, container_name, status, image, created_at 
FROM containers 
WHERE user_id = 4 
ORDER BY created_at DESC;

-- Update all RUNNING/PENDING containers to DELETED for user 4
UPDATE containers 
SET status = 'DELETED', 
    stopped_at = NOW(),
    updated_at = NOW()
WHERE user_id = 4 
  AND status IN ('RUNNING', 'PENDING', 'CREATING', 'STOPPED');

-- Verify cleanup
SELECT id, container_id, container_name, status, image, stopped_at 
FROM containers 
WHERE user_id = 4 
ORDER BY created_at DESC;

-- Check quota is now available
SELECT 
    COUNT(*) as running_containers,
    (SELECT max_containers FROM users WHERE id = 4) as max_allowed
FROM containers 
WHERE user_id = 4 AND status = 'RUNNING';
