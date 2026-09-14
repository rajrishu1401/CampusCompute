-- CampusCompute Initial Data
-- Creates default admin user and sample data

-- Default admin user
-- Username: admin, Password: admin123 (bcrypt hash)
INSERT INTO users (username, email, password_hash, role, full_name, active, max_cpu_cores, max_ram_gb, max_containers)
VALUES (
    'admin',
    'admin@upes.ac.in',
    '$2a$10$XQ0HlH5VlXvQGvVVMzJQYuK3.5C8xH5HB8k9xHvGmQ5YzGh0K5GK.',  -- admin123
    'ADMIN',
    'System Administrator',
    true,
    8,
    16,
    10
) ON CONFLICT (username) DO NOTHING;

-- Sample student user
-- Username: student1, Password: student123
INSERT INTO users (username, email, password_hash, role, full_name, sap_id, department, active)
VALUES (
    'student1',
    'student1@studentmail.upes.ac.in',
    '$2a$10$XQ0HlH5VlXvQGvVVMzJQYuK3.5C8xH5HB8k9xHvGmQ5YzGh0K5GK.',  -- student123
    'STUDENT',
    'Test Student',
    '500012345',
    'Computer Science',
    true
) ON CONFLICT (username) DO NOTHING;

-- Sample faculty user
-- Username: faculty1, Password: faculty123
INSERT INTO users (username, email, password_hash, role, full_name, department, active, max_cpu_cores, max_ram_gb)
VALUES (
    'faculty1',
    'faculty1@upes.ac.in',
    '$2a$10$XQ0HlH5VlXvQGvVVMzJQYuK3.5C8xH5HB8k9xHvGmQ5YzGh0K5GK.',  -- faculty123
    'FACULTY',
    'Test Faculty',
    'Computer Science',
    true,
    6,
    12
) ON CONFLICT (username) DO NOTHING;

-- Sample lab reservations (CS Lab classes)
INSERT INTO reservations (lab_name, start_time, end_time, recurring, day_of_week, description)
VALUES
    ('Computer Science Lab', CURRENT_TIMESTAMP + INTERVAL '1 day' + INTERVAL '10 hours', CURRENT_TIMESTAMP + INTERVAL '1 day' + INTERVAL '12 hours', true, 1, 'Data Structures Class'),
    ('Computer Science Lab', CURRENT_TIMESTAMP + INTERVAL '1 day' + INTERVAL '14 hours', CURRENT_TIMESTAMP + INTERVAL '1 day' + INTERVAL '16 hours', true, 1, 'Algorithms Class'),
    ('AI Lab', CURRENT_TIMESTAMP + INTERVAL '1 day' + INTERVAL '11 hours', CURRENT_TIMESTAMP + INTERVAL '1 day' + INTERVAL '13 hours', true, 2, 'Machine Learning Lab'),
    ('Network Lab', CURRENT_TIMESTAMP + INTERVAL '1 day' + INTERVAL '15 hours', CURRENT_TIMESTAMP + INTERVAL '1 day' + INTERVAL '17 hours', true, 3, 'Computer Networks Lab')
ON CONFLICT DO NOTHING;
