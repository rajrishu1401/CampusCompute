# CampusCompute - Development Setup Guide

## Prerequisites

### Backend Development
- **Java 17** or higher
- **Maven 3.8+**
- **PostgreSQL 15+**
- **Redis 7+**
- **IDE:** IntelliJ IDEA (recommended) or Eclipse

### Agent Development
- **Python 3.11+**
- **Docker Engine 24+**
- **pip** package manager
- **IDE:** VS Code with Python extension (recommended) or PyCharm

### General
- **Git** for version control
- **Postman** or **curl** for API testing
- **Draw.io** for viewing architecture diagrams

---

## Step 1: Database Setup

### Install PostgreSQL

**Windows:**
```bash
# Download from: https://www.postgresql.org/download/windows/
# Or use Chocolatey
choco install postgresql
```

**Linux:**
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
```

**Mac:**
```bash
brew install postgresql@15
```

### Create Database

```sql
-- Connect to PostgreSQL
psql -U postgres

-- Create database
CREATE DATABASE campuscompute;

-- Create user (optional)
CREATE USER campuscompute_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE campuscompute TO campuscompute_user;

-- Exit
\q
```

### Install Redis

**Windows:**
```bash
# Download from: https://github.com/microsoftarchive/redis/releases
# Or use Chocolatey
choco install redis-64
```

**Linux:**
```bash
sudo apt install redis-server
sudo systemctl start redis-server
```

**Mac:**
```bash
brew install redis
brew services start redis
```

Test Redis:
```bash
redis-cli ping
# Should return: PONG
```

---

## Step 2: Backend Setup

### Clone & Navigate
```bash
cd backend
```

### Configure Database Connection

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/campuscompute
    username: postgres
    password: your_password  # Change this
```

### Install Dependencies & Build
```bash
# Clean and install
mvn clean install

# Skip tests if needed
mvn clean install -DskipTests
```

### Run Backend
```bash
mvn spring-boot:run
```

Backend should start on: `http://localhost:8080`

### Verify Backend
```bash
# Check health (once endpoint is created)
curl http://localhost:8080/actuator/health

# Check API docs
curl http://localhost:8080/api
```

---

## Step 3: Agent Setup

### Navigate to Agent Directory
```bash
cd agent
```

### Create Virtual Environment (Recommended)
```bash
# Create venv
python -m venv venv

# Activate
# Windows:
venv\Scripts\activate
# Linux/Mac:
source venv/bin/activate
```

### Install Dependencies
```bash
pip install -r requirements.txt
```

### Install Docker

**Windows:**
- Download Docker Desktop: https://www.docker.com/products/docker-desktop

**Linux:**
```bash
# Ubuntu/Debian
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER
```

**Mac:**
- Download Docker Desktop: https://www.docker.com/products/docker-desktop

Verify Docker:
```bash
docker --version
docker ps
```

### Configure Agent

Edit `config.yaml`:

```yaml
device:
  device_id: "LAB-PC-001"  # Change per machine
  lab_name: "Computer Science Lab"

broker:
  url: "ws://localhost:8080/ws/agent"  # Local development

mtls:
  enabled: false  # Disable for local development
```

### Create Logs Directory
```bash
mkdir logs
```

### Run Agent
```bash
python src/main.py
```

---

## Step 4: Development Tools

### Install IDE Extensions

**VS Code:**
- Extension Pack for Java
- Python
- Spring Boot Extension Pack
- Docker
- YAML

**IntelliJ IDEA:**
- Spring Boot plugin (built-in)
- Docker plugin

### API Testing

**Install Postman:**
- Download: https://www.postman.com/downloads/

**Or use curl:**
```bash
# Example: Login (once endpoint exists)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

---

## Step 5: Verify Full Setup

### 1. Check PostgreSQL
```bash
psql -U postgres -d campuscompute -c "SELECT version();"
```

### 2. Check Redis
```bash
redis-cli ping
```

### 3. Check Docker
```bash
docker ps
docker images
```

### 4. Start Backend
```bash
cd backend
mvn spring-boot:run
```

Expected output:
```
CampusCompute Backend Started
API: http://localhost:8080/api
```

### 5. Start Agent
```bash
cd agent
python src/main.py
```

Expected output:
```
CampusCompute Agent Started
Device ID: LAB-PC-001
```

### 6. Check Logs
- Backend: Console output
- Agent: `agent/logs/agent.log`

---

## Common Issues & Solutions

### Issue: PostgreSQL connection refused

**Solution:**
```bash
# Check if PostgreSQL is running
# Windows:
sc query postgresql-x64-15
# Linux:
sudo systemctl status postgresql
```

### Issue: Redis connection error

**Solution:**
```bash
# Start Redis
# Windows:
redis-server
# Linux:
sudo systemctl start redis-server
```

### Issue: Docker daemon not running

**Solution:**
- Windows/Mac: Start Docker Desktop
- Linux: `sudo systemctl start docker`

### Issue: Port 8080 already in use

**Solution:**
```bash
# Change port in application.yml
server:
  port: 8081
```

### Issue: Python module not found

**Solution:**
```bash
# Ensure virtual environment is activated
# Reinstall dependencies
pip install -r requirements.txt
```

---

## Development Workflow

### Daily Workflow

1. **Start databases:**
   ```bash
   # PostgreSQL & Redis should be running
   ```

2. **Start backend:**
   ```bash
   cd backend
   mvn spring-boot:run
   ```

3. **Start agent (separate terminal):**
   ```bash
   cd agent
   python src/main.py
   ```

4. **Make changes and test:**
   - Backend: Auto-reloads with Spring DevTools (add to pom.xml)
   - Agent: Restart manually after changes

### Testing

**Backend:**
```bash
# Run tests
mvn test

# Run specific test
mvn test -Dtest=UserServiceTest
```

**Agent:**
```bash
# Run tests (once test files created)
pytest tests/
```

---

## Environment Variables (Production)

Create `.env` file (don't commit):

```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=campuscompute
DB_USER=postgres
DB_PASSWORD=your_password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# JWT
JWT_SECRET=your-secret-key-change-in-production

# Agent
BROKER_URL=wss://campuscompute.upes.ac.in/ws/agent
```

---

## Next Steps

Once setup is complete:

1. **Backend:** Implement repositories and services
2. **Agent:** Add WebSocket client connection
3. **Integration:** Test agent-broker communication
4. **Database:** Create initial admin user
5. **Testing:** Write unit tests

See `PROJECT_STRUCTURE.md` for detailed task breakdown.

---

## Support

- Check documentation in `docs/` folder
- View architecture diagrams in `.drawio` files
- Read synopsis: `project_synopsis.pdf`
- API documentation: (will be added)

Happy coding! 🚀
