-- CampusCompute Database Schema
-- Run this after starting PostgreSQL

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('STUDENT', 'FACULTY', 'ADMIN')),
    full_name VARCHAR(100),
    sap_id VARCHAR(20),
    department VARCHAR(50),
    active BOOLEAN NOT NULL DEFAULT true,
    max_cpu_cores INTEGER NOT NULL DEFAULT 4,
    max_ram_gb INTEGER NOT NULL DEFAULT 8,
    max_containers INTEGER NOT NULL DEFAULT 3,
    max_container_lifetime_ms BIGINT NOT NULL DEFAULT 14400000,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Devices table (lab computers)
CREATE TABLE IF NOT EXISTS devices (
    id BIGSERIAL PRIMARY KEY,
    device_id VARCHAR(100) UNIQUE NOT NULL,
    hostname VARCHAR(100) NOT NULL,
    lab_name VARCHAR(50) NOT NULL,
    ip_address VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'OFFLINE' CHECK (status IN ('ONLINE', 'OFFLINE', 'BUSY', 'MAINTENANCE')),
    total_cpu_cores INTEGER NOT NULL,
    total_ram_bytes BIGINT NOT NULL,
    total_disk_bytes BIGINT NOT NULL,
    used_cpu_cores INTEGER NOT NULL DEFAULT 0,
    used_ram_bytes BIGINT NOT NULL DEFAULT 0,
    used_disk_bytes BIGINT NOT NULL DEFAULT 0,
    cpu_load_percent DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    ram_load_percent DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    reliability_score DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    last_heartbeat TIMESTAMP,
    agent_version VARCHAR(20),
    docker_version VARCHAR(20),
    enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Containers table
CREATE TABLE IF NOT EXISTS containers (
    id BIGSERIAL PRIMARY KEY,
    container_id VARCHAR(100) UNIQUE NOT NULL,
    container_name VARCHAR(100) NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    device_id BIGINT NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'CREATING', 'RUNNING', 'STOPPED', 'FAILED', 'DELETED')),
    image VARCHAR(100) NOT NULL,
    allocated_cpu_cores INTEGER NOT NULL,
    allocated_ram_bytes BIGINT NOT NULL,
    allocated_disk_bytes BIGINT NOT NULL,
    ssh_port INTEGER,
    terminal_port INTEGER,
    expires_at TIMESTAMP,
    started_at TIMESTAMP,
    stopped_at TIMESTAMP,
    persistent BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Lab reservations table
CREATE TABLE IF NOT EXISTS reservations (
    id BIGSERIAL PRIMARY KEY,
    lab_name VARCHAR(50) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    recurring BOOLEAN NOT NULL DEFAULT false,
    day_of_week INTEGER, -- 0=Sunday, 6=Saturday
    description VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Audit logs table
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    action VARCHAR(50) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_id VARCHAR(100),
    details TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_containers_user_id ON containers(user_id);
CREATE INDEX IF NOT EXISTS idx_containers_device_id ON containers(device_id);
CREATE INDEX IF NOT EXISTS idx_containers_status ON containers(status);
CREATE INDEX IF NOT EXISTS idx_devices_status ON devices(status);
CREATE INDEX IF NOT EXISTS idx_devices_lab_name ON devices(lab_name);
CREATE INDEX IF NOT EXISTS idx_reservations_lab_time ON reservations(lab_name, start_time, end_time);
CREATE INDEX IF NOT EXISTS idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON audit_logs(created_at);
