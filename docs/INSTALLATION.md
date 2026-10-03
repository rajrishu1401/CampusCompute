# CampusCompute - Installation & Deployment Guide

**Version**: 1.0  
**Last Updated**: October 3, 2026  
**Audience**: System Administrators, DevOps Engineers  

---

## Table of Contents

1. [Overview](#overview)
2. [System Requirements](#system-requirements)
3. [Quick Start (Development)](#quick-start-development)
4. [Production Deployment](#production-deployment)
5. [Database Setup](#database-setup)
6. [Backend Deployment](#backend-deployment)
7. [Frontend Deployment](#frontend-deployment)
8. [Agent Installation](#agent-installation)
9. [Configuration](#configuration)
10. [Security](#security)
11. [Monitoring](#monitoring)
12. [Troubleshooting](#troubleshooting)

---

## Overview

CampusCompute consists of four main components:

1. **Backend**: Spring Boot REST API + WebSocket server
2. **Frontend**: React SPA with Material-UI
3. **Database**: PostgreSQL + Redis
4. **Agent**: Python application on lab computers

### Architecture

```
                    Internet
                       │
                       ▼
┌──────────────────────────────────────────┐
│            Frontend (React)               │
│         http://your-domain.com            │
└──────────────────┬───────────────────────┘
                   │ REST API
                   ▼
┌──────────────────────────────────────────┐
│       Backend (Spring Boot :8081)         │
│  ┌─────────────────────────────────────┐ │
│  │  REST API  │  WebSocket  │  Auth    │ │
│  └─────────────────────────────────────┘ │
└──────────────────┬───────────────────────┘
                   │
      ┌────────────┴────────────┐
      ▼                         ▼
┌──────────┐            ┌──────────┐
│PostgreSQL│            │  Redis   │
│  :5432   │            │  :6379   │
└──────────┘            └──────────┘
                   
                   ▲ WebSocket
                   │
      ┌────────────┴────────────┐
      │                         │
┌──────────┐            ┌──────────┐
│ Agent 1  │            │ Agent N  │
│ Lab PC 1 │            │ Lab PC N │
└──────────┘            └──────────┘
```

---

## System Requirements

### Backend Server

**Minimum:**
- CPU: 2 cores
- RAM: 4 GB
- Disk: 20 GB
- OS: Linux (Ubuntu 20.04+), Windows Server 2019+

**Recommended:**
- CPU: 4+ cores
- RAM: 8+ GB
- Disk: 50+ GB SSD
- OS: Ubuntu 22.04 LTS

**Software:**
- Java 17 or higher
- PostgreSQL 15+
- Redis 7+
- Maven 3.8+ (for building)

### Frontend Server

**Option 1: Static Hosting (CDN)**
- Any static file host (S3, Netlify, Vercel, GitHub Pages)

**Option 2: Self-Hosted**
- NGINX or Apache web server
- 1 GB RAM, 5 GB disk

### Lab Computers (Agents)

**Minimum per Device:**
- CPU: 4 cores
- RAM: 8 GB
- Disk: 100 GB
- OS: Windows 10/11, Ubuntu 20.04+, macOS 10.15+

**Software:**
- Docker Desktop (Windows/Mac) or Docker Engine (Linux)
- Python 3.8+
- Internet connectivity to backend

### Network Requirements

**Ports:**
- Backend: 8081 (HTTP/WebSocket)
- PostgreSQL: 5432 (internal)
- Redis: 6379 (internal)
- Frontend: 80/443 (HTTP/HTTPS)

**Firewall Rules:**
- Allow lab computers → backend (port 8081)
- Allow users → frontend (port 80/443)
- Allow frontend → backend (port 8081)

---

## Quick Start (Development)

For local development and testing.

### Step 1: Clone Repository

```bash
git clone https://github.com/rajrishu1401/CampusCompute.git
cd CampusCompute
```

### Step 2: Start Database Services

```bash
# Start PostgreSQL and Redis using Docker Compose
docker-compose up -d

# Verify services are running
docker ps
```

Expected output:
```
CONTAINER ID   IMAGE                COMMAND                  STATUS
xxx            postgres:15-alpine   "docker-entrypoint..."   Up (healthy)
xxx            redis:7-alpine       "docker-entrypoint..."   Up (healthy)
```

### Step 3: Start Backend

```bash
cd backend

# Build and run
mvn clean install
mvn spring-boot:run

# Or run the JAR directly
java -jar target/campuscompute-1.0.0.jar
```

Backend will start on: `http://localhost:8081`

Verify: `curl http://localhost:8081/api/health`

### Step 4: Start Frontend

```bash
cd frontend

# Install dependencies (first time only)
npm install

# Start development server
npm run dev
```

Frontend will start on: `http://localhost:3000`

Visit: `http://localhost:3000` in your browser

### Step 5: Configure Agent (Optional)

```bash
cd agent

# Install dependencies (first time only)
pip install -r requirements.txt

# Edit configuration
cp config.yaml.example config.yaml
nano config.yaml

# Start agent
python src/main.py
```

✅ **Development environment ready!**

---

## Production Deployment

### Option 1: Docker Compose (Recommended)

Complete stack deployment with one command.

#### Create Production Docker Compose

Create `docker-compose.prod.yml`:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: campuscompute-postgres
    restart: always
    environment:
      POSTGRES_DB: campuscompute
      POSTGRES_USER: campuscompute
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - campuscompute
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U campuscompute"]
      interval: 30s
      timeout: 10s
      retries: 5

  redis:
    image: redis:7-alpine
    container_name: campuscompute-redis
    restart: always
    volumes:
      - redis_data:/data
    networks:
      - campuscompute
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 30s
      timeout: 10s
      retries: 5

  backend:
    image: campuscompute/backend:latest
    container_name: campuscompute-backend
    restart: always
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/campuscompute
      SPRING_DATASOURCE_USERNAME: campuscompute
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      SPRING_REDIS_HOST: redis
      SPRING_REDIS_PORT: 6379
      JWT_SECRET: ${JWT_SECRET}
      JWT_EXPIRATION: 86400000
    ports:
      - "8081:8081"
    networks:
      - campuscompute
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8081/api/health"]
      interval: 30s
      timeout: 10s
      retries: 5

  frontend:
    image: campuscompute/frontend:latest
    container_name: campuscompute-frontend
    restart: always
    environment:
      VITE_API_URL: http://backend:8081
    ports:
      - "80:80"
    depends_on:
      - backend
    networks:
      - campuscompute

volumes:
  postgres_data:
  redis_data:

networks:
  campuscompute:
    driver: bridge
```

#### Create Environment File

Create `.env`:

```bash
# Database
DB_PASSWORD=your-secure-database-password-here

# JWT
JWT_SECRET=your-very-long-secret-key-at-least-256-bits

# Backend
BACKEND_URL=http://your-domain.com:8081

# Email (for notifications - optional)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-app-password
```

#### Build Docker Images

**Backend:**
```bash
cd backend

# Create Dockerfile
cat > Dockerfile << 'EOF'
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
EOF

# Build image
docker build -t campuscompute/backend:latest .
```

**Frontend:**
```bash
cd frontend

# Create Dockerfile
cat > Dockerfile << 'EOF'
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
EOF

# Create nginx config
cat > nginx.conf << 'EOF'
server {
    listen 80;
    server_name _;
    root /usr/share/nginx/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://backend:8081;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
    }

    location /ws {
        proxy_pass http://backend:8081;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "Upgrade";
        proxy_set_header Host $host;
    }
}
EOF

# Build image
docker build -t campuscompute/frontend:latest .
```

#### Deploy

```bash
# Start all services
docker-compose -f docker-compose.prod.yml up -d

# Check logs
docker-compose -f docker-compose.prod.yml logs -f

# Check status
docker-compose -f docker-compose.prod.yml ps
```

✅ **Production stack running!**

Visit: `http://your-server-ip`

### Option 2: Manual Deployment

For more control or existing infrastructure.

#### Database Setup

See [Database Setup](#database-setup) section below.

#### Backend Deployment

See [Backend Deployment](#backend-deployment) section below.

#### Frontend Deployment

See [Frontend Deployment](#frontend-deployment) section below.

---

## Database Setup

### PostgreSQL Installation

**Ubuntu:**
```bash
# Install PostgreSQL 15
sudo apt update
sudo apt install -y postgresql-15 postgresql-contrib-15

# Start service
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

**Windows:**
Download installer from: https://www.postgresql.org/download/windows/

### Create Database and User

```bash
# Connect as postgres user
sudo -u postgres psql

# In psql:
CREATE DATABASE campuscompute;
CREATE USER campuscompute WITH ENCRYPTED PASSWORD 'your-secure-password';
GRANT ALL PRIVILEGES ON DATABASE campuscompute TO campuscompute;
\q
```

### Initialize Schema

Tables are auto-created by Hibernate on first run, or manually:

```bash
psql -U campuscompute -d campuscompute -f backend/src/main/resources/schema.sql
```

### Redis Installation

**Ubuntu:**
```bash
# Install Redis 7
sudo apt install -y redis-server

# Configure
sudo nano /etc/redis/redis.conf
# Set: bind 127.0.0.1
# Set: requirepass your-redis-password

# Restart
sudo systemctl restart redis-server
sudo systemctl enable redis-server
```

**Windows:**
Use Redis Docker container or WSL.

### Verify Database

```bash
# Test PostgreSQL connection
psql -U campuscompute -d campuscompute -h localhost -c "SELECT version();"

# Test Redis connection
redis-cli ping
```

---

## Backend Deployment

### Option 1: Systemd Service (Linux)

**Build JAR:**
```bash
cd backend
mvn clean package -DskipTests
```

**Create application.properties:**
```bash
sudo mkdir -p /opt/campuscompute/config
sudo nano /opt/campuscompute/config/application.properties
```

Content:
```properties
# Server
server.port=8081

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/campuscompute
spring.datasource.username=campuscompute
spring.datasource.password=your-db-password

# Redis
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.password=your-redis-password

# JWT
jwt.secret=your-very-long-secret-key-at-least-256-bits
jwt.expiration=86400000

# Logging
logging.level.com.campuscompute=INFO
logging.file.path=/var/log/campuscompute
```

**Copy JAR:**
```bash
sudo cp target/campuscompute-1.0.0.jar /opt/campuscompute/campuscompute.jar
```

**Create service:**
```bash
sudo nano /etc/systemd/system/campuscompute.service
```

Content:
```ini
[Unit]
Description=CampusCompute Backend
After=postgresql.service redis.service

[Service]
Type=simple
User=campuscompute
WorkingDirectory=/opt/campuscompute
ExecStart=/usr/bin/java -jar /opt/campuscompute/campuscompute.jar --spring.config.location=/opt/campuscompute/config/application.properties
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

**Create user:**
```bash
sudo useradd -r -s /bin/false campuscompute
sudo chown -R campuscompute:campuscompute /opt/campuscompute
```

**Start service:**
```bash
sudo systemctl daemon-reload
sudo systemctl start campuscompute
sudo systemctl enable campuscompute

# Check status
sudo systemctl status campuscompute

# View logs
sudo journalctl -u campuscompute -f
```

### Option 2: Windows Service

Use [NSSM](https://nssm.cc/) to create Windows service:

```cmd
nssm install CampusCompute "C:\Program Files\Java\jdk-17\bin\java.exe" "-jar C:\CampusCompute\campuscompute.jar"
nssm start CampusCompute
```

### Verify Backend

```bash
curl http://localhost:8081/api/health
```

Expected:
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "redis": { "status": "UP" }
  }
}
```

---

## Frontend Deployment

### Option 1: NGINX (Recommended)

**Build production bundle:**
```bash
cd frontend
npm run build
```

Creates `dist/` folder with static files.

**Install NGINX:**
```bash
sudo apt install -y nginx
```

**Configure NGINX:**
```bash
sudo nano /etc/nginx/sites-available/campuscompute
```

Content:
```nginx
server {
    listen 80;
    server_name your-domain.com;
    root /var/www/campuscompute;
    index index.html;

    # Frontend
    location / {
        try_files $uri $uri/ /index.html;
    }

    # Backend API proxy
    location /api {
        proxy_pass http://localhost:8081;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # WebSocket proxy
    location /ws {
        proxy_pass http://localhost:8081;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # Security headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;

    # Gzip compression
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml text/javascript;
}
```

**Deploy files:**
```bash
sudo mkdir -p /var/www/campuscompute
sudo cp -r dist/* /var/www/campuscompute/
sudo chown -R www-data:www-data /var/www/campuscompute
```

**Enable site:**
```bash
sudo ln -s /etc/nginx/sites-available/campuscompute /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

### Option 2: CDN (S3, Netlify, Vercel)

**For S3:**
```bash
# Build
npm run build

# Upload to S3
aws s3 sync dist/ s3://your-bucket-name/ --delete

# Set bucket policy for public read
```

**For Netlify:**
```bash
# Install Netlify CLI
npm install -g netlify-cli

# Deploy
netlify deploy --prod --dir=dist
```

### SSL/HTTPS (Let's Encrypt)

```bash
# Install certbot
sudo apt install -y certbot python3-certbot-nginx

# Get certificate
sudo certbot --nginx -d your-domain.com

# Auto-renewal is configured automatically
```

---

## Agent Installation

See [Windows Installer README](../agent-installer/README.md) for details.

### Quick Agent Deployment

**Method 1: Windows Installer (Easiest)**
1. Build installer: `cd agent-installer && python build_installer.py`
2. Distribute `CampusCompute_Agent_Installer.exe` to lab computers
3. Users run installer, enter enrollment token
4. Done!

**Method 2: Manual (Linux/Mac/Advanced)**
```bash
# On lab computer
git clone https://github.com/rajrishu1401/CampusCompute.git
cd CampusCompute/agent

# Install dependencies
pip3 install -r requirements.txt

# Configure
cp config.yaml.example config.yaml
nano config.yaml

# Edit config (add enrollment token, backend URL)

# Run
python3 src/main.py
```

**Create systemd service (Linux):**
```bash
sudo nano /etc/systemd/system/campuscompute-agent.service
```

Content:
```ini
[Unit]
Description=CampusCompute Agent
After=network.target docker.service

[Service]
Type=simple
User=agent
WorkingDirectory=/opt/campuscompute-agent
ExecStart=/usr/bin/python3 /opt/campuscompute-agent/src/main.py
Restart=always

[Install]
WantedBy=multi-user.target
```

---

## Configuration

### Environment Variables

**Backend:**
```bash
# Required
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/campuscompute
SPRING_DATASOURCE_USERNAME=campuscompute
SPRING_DATASOURCE_PASSWORD=secure-password
JWT_SECRET=256-bit-secret-key

# Optional
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379
SERVER_PORT=8081
LOGGING_LEVEL_ROOT=INFO
```

**Frontend:**
```bash
VITE_API_URL=http://your-backend-url:8081
```

**Agent:**
```yaml
broker:
  url: "ws://your-backend-url:8081/ws/agent"
  enrollment_token: "token-from-admin-panel"
  organization_id: 1

device:
  device_id: "unique-device-id"
  hostname: "Friendly Device Name"

docker:
  socket: "unix:///var/run/docker.sock"  # Linux/Mac
  # socket: "npipe:////./pipe/docker_engine"  # Windows
```

---

## Security

### SSL/TLS

Always use HTTPS in production:
```bash
# Let's Encrypt (free)
sudo certbot --nginx -d your-domain.com

# Or use your own certificates
```

### Firewall

**Ubuntu (ufw):**
```bash
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw allow 8081/tcp
sudo ufw enable
```

### Database Security

```sql
-- Restrict PostgreSQL to localhost
# In postgresql.conf:
listen_addresses = 'localhost'

-- Use strong passwords
ALTER USER campuscompute WITH PASSWORD 'very-secure-password-123!@#';
```

### JWT Configuration

Use strong secret (256-bit):
```bash
# Generate secure secret
openssl rand -base64 32
```

### CORS Configuration

Update `backend/src/main/java/com/campuscompute/config/CorsConfig.java`:
```java
.allowedOrigins("https://your-domain.com")
```

---

## Monitoring

### Health Checks

**Backend:**
```bash
curl http://localhost:8081/api/health
```

**Database:**
```bash
psql -U campuscompute -c "SELECT 1;"
redis-cli ping
```

### Logs

**Backend logs:**
```bash
# Systemd
sudo journalctl -u campuscompute -f

# File
tail -f /var/log/campuscompute/application.log
```

**NGINX logs:**
```bash
tail -f /var/log/nginx/access.log
tail -f /var/log/nginx/error.log
```

**Agent logs:**
```bash
tail -f /opt/campuscompute-agent/logs/agent.log
```

### Monitoring Tools (Optional)

- **Prometheus**: Metrics collection
- **Grafana**: Dashboards
- **ELK Stack**: Log aggregation
- **Uptime Kuma**: Uptime monitoring

---

## Troubleshooting

### Backend Won't Start

**Check logs:**
```bash
sudo journalctl -u campuscompute -n 50
```

**Common issues:**
- Database connection failed → verify credentials
- Port 8081 in use → change port or stop conflicting service
- Missing environment variables → check config file

### Frontend Not Loading

**Check NGINX:**
```bash
sudo nginx -t
sudo systemctl status nginx
```

**Common issues:**
- 502 Bad Gateway → backend not running
- 404 errors → check root path and try_files directive
- CORS errors → verify backend CORS configuration

### Agent Can't Connect

**Check connectivity:**
```bash
# Test WebSocket
wscat -c ws://your-backend:8081/ws/agent

# Test HTTP
curl http://your-backend:8081/api/health
```

**Common issues:**
- Connection refused → firewall blocking port 8081
- Invalid token → generate new enrollment token
- Certificate errors → verify SSL configuration

### Database Issues

**Connection pool exhausted:**
```properties
# Increase pool size
spring.datasource.hikari.maximum-pool-size=20
```

**Slow queries:**
```sql
-- Enable query logging
ALTER DATABASE campuscompute SET log_min_duration_statement = 1000;
```

---

## Backup & Recovery

### Database Backup

**Automated backup script:**
```bash
#!/bin/bash
BACKUP_DIR="/backup/postgres"
DATE=$(date +%Y%m%d_%H%M%S)

pg_dump -U campuscompute campuscompute > "$BACKUP_DIR/campuscompute_$DATE.sql"

# Keep only last 7 days
find $BACKUP_DIR -type f -mtime +7 -delete
```

**Schedule with cron:**
```bash
# Daily at 2 AM
0 2 * * * /usr/local/bin/backup-campuscompute.sh
```

### Restore Database

```bash
psql -U campuscompute -d campuscompute < backup.sql
```

---

## Performance Tuning

### PostgreSQL

```sql
-- Increase shared buffers (25% of RAM)
shared_buffers = 2GB

-- Increase work memory
work_mem = 16MB

-- Enable query planning
effective_cache_size = 6GB
```

### NGINX

```nginx
# Worker processes
worker_processes auto;

# Connections per worker
worker_connections 1024;

# Keep-alive
keepalive_timeout 65;

# Compression
gzip on;
gzip_comp_level 6;
```

### Backend

```properties
# Connection pool
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5

# Logging
logging.level.org.hibernate.SQL=WARN
```

---

## Appendix

### Port Reference

| Service | Port | Protocol |
|---------|------|----------|
| Frontend | 80 | HTTP |
| Frontend (SSL) | 443 | HTTPS |
| Backend | 8081 | HTTP/WebSocket |
| PostgreSQL | 5432 | TCP |
| Redis | 6379 | TCP |

### Directory Structure (Production)

```
/opt/campuscompute/
├── campuscompute.jar          # Backend JAR
├── config/
│   └── application.properties # Backend config
└── logs/                      # Application logs

/var/www/campuscompute/        # Frontend files
├── index.html
├── assets/
└── ...

/opt/campuscompute-agent/      # Agent (on lab PCs)
├── src/
├── config.yaml
└── logs/
```

### Useful Commands

```bash
# Check service status
sudo systemctl status campuscompute
sudo systemctl status nginx
sudo systemctl status postgresql

# View logs
sudo journalctl -u campuscompute -f
sudo tail -f /var/log/nginx/error.log

# Restart services
sudo systemctl restart campuscompute
sudo systemctl reload nginx

# Check disk space
df -h

# Check memory
free -h

# Check open ports
sudo netstat -tulpn | grep LISTEN
```

---

**Installation complete!** 🎉

For support, see:
- [Admin Guide](ADMIN_GUIDE.md)
- [Student Guide](STUDENT_GUIDE.md)
- [API Reference](API_REFERENCE.md)

---

*Last Updated: October 3, 2026*  
*Version: 1.0*  
*For: CampusCompute v1.0*
