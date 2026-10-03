# CampusCompute - Quick Start Guide

**Get the entire system running in 10 minutes!**

---

## 🎯 What You'll Run

1. **PostgreSQL Database** (Docker container)
2. **Redis Cache** (Docker container)
3. **Spring Boot Backend** (Java application on port 8081)
4. **React Frontend** (Vite dev server on port 3000)
5. **Python Agent** (Optional - for testing device features)

---

## ✅ Prerequisites Check

Before starting, ensure you have:

```bash
# Java (JDK 17+)
java -version
# Should show: java version "17" or higher

# Node.js (18+)
node -v
# Should show: v18.x.x or higher

# Python (3.11+)
python --version
# Should show: Python 3.11 or higher

# Docker
docker --version
# Should show: Docker version 24.x.x or higher

# Docker Compose
docker-compose --version
# Should show: docker-compose version 1.x.x or higher
```

---

## 🚀 Step-by-Step Startup

### Step 1: Start Database Services (2 minutes)

```bash
# Navigate to project root
cd d:\Courses\BTech\7th_Sem\Major

# Start PostgreSQL and Redis
docker-compose up -d

# Verify containers are running
docker ps
# Should see: campuscompute-postgres and campuscompute-redis
```

**Expected Output**:
```
Creating campuscompute-postgres ... done
Creating campuscompute-redis    ... done
```

---

### Step 2: Start Backend (3 minutes)

**Option A: Using Maven Wrapper (Recommended)**
```bash
cd backend
.\mvnw spring-boot:run
```

**Option B: Using IDE**
1. Open `backend` folder in IntelliJ IDEA or Eclipse
2. Run `CampusComputeApplication.java`

**Wait for**:
```
Started CampusComputeApplication in X.XXX seconds
```

**Backend is now running on**: http://localhost:8081

**Test Backend**:
```bash
curl http://localhost:8081/api/health
# Should return: {"status":"UP"}
```

---

### Step 3: Start Frontend (2 minutes)

**Open a NEW terminal/command prompt**:

```bash
cd d:\Courses\BTech\7th_Sem\Major\frontend

# Install dependencies (first time only)
npm install

# Start development server
npm run dev
```

**Expected Output**:
```
  VITE v5.4.21  ready in 626 ms

  ➜  Local:   http://localhost:3000/
  ➜  Network: use --host to expose
```

**Frontend is now running on**: http://localhost:3000

---

### Step 4 (Optional): Start Agent (3 minutes)

**Only needed if you want to test device features**

**Open a NEW terminal/command prompt**:

```bash
cd d:\Courses\BTech\7th_Sem\Major\agent

# Install dependencies (first time only)
pip install -r requirements.txt

# Configure agent
# Edit config.yaml and set enrollment_token (get from admin dashboard)

# Start agent
python src\main.py
```

**Expected Output**:
```
INFO - Starting CampusCompute Agent
INFO - Connecting to broker...
INFO - Connected successfully
```

---

## 🎉 System is Ready!

### Access URLs

| Service | URL | Purpose |
|---------|-----|---------|
| **Frontend** | http://localhost:3000 | Main application |
| **Backend API** | http://localhost:8081/api | REST API |
| **Health Check** | http://localhost:8081/api/health | Backend status |
| **PostgreSQL** | localhost:5432 | Database |
| **Redis** | localhost:6379 | Cache |

---

## 🧪 Quick Test Flow

### Test 1: Register Organization (2 minutes)

1. Open browser: http://localhost:3000
2. Click **"Register Organization"**
3. Fill in the form:
   ```
   Organization Name: Test College
   Organization Code: TEST
   Domain: test.edu
   Contact Email: admin@test.edu
   Admin Username: admin
   Admin Email: admin@test.edu
   Password: admin123
   ```
4. Click **"Register Organization"**
5. You'll be redirected to login

### Test 2: Login as Admin (1 minute)

1. Login with:
   ```
   Username: admin
   Password: admin123
   ```
2. You should see the **Admin Dashboard** with statistics cards

### Test 3: View Dashboard Stats (1 minute)

On the admin dashboard, you should see:
- Devices Online: 0/0
- Active Students: 0
- Containers Running: 0
- CPU Utilization: 0%

### Test 4: Generate Device Token (1 minute)

1. Click **"Devices"** in sidebar
2. Click **"Add Device"** button
3. Copy the enrollment token
4. (Optional) Use this token in agent config.yaml

### Test 5: Upload Students (2 minutes)

1. Click **"Students"** in sidebar
2. Click **"Upload Students"** button
3. Paste in text area:
   ```
   500101234, john@test.edu, John Doe
   500101235, jane@test.edu, Jane Smith
   ```
4. Click **"Upload"**
5. Students should appear in the table

### Test 6: Student First-Time Setup (2 minutes)

1. Open new incognito window: http://localhost:3000/first-time-setup
2. Enter:
   ```
   Student ID: 500101234
   Email: john@test.edu
   Password: password123
   Confirm Password: password123
   ```
3. Click **"Complete Setup"**
4. You should see **Student Dashboard**

### Test 7: Create Container (Student) (1 minute)

1. As student, click **"Containers"** in sidebar
2. Click **"Create Container"** button
3. Fill in:
   ```
   Image Name: ubuntu:latest
   CPU Cores: 1
   RAM: 1 GB
   ```
4. Click **"Create"**
5. Container should appear with status "PENDING" or "RUNNING"

---

## 🛑 Stopping the System

### Stop Frontend
```bash
# In frontend terminal
Press Ctrl+C
```

### Stop Backend
```bash
# In backend terminal
Press Ctrl+C
```

### Stop Databases
```bash
cd d:\Courses\BTech\7th_Sem\Major
docker-compose down
```

### Stop Agent (if running)
```bash
# In agent terminal
Press Ctrl+C
```

---

## 🔧 Troubleshooting

### Problem: Backend won't start

**Error**: "Port 8081 is already in use"

**Solution**:
```bash
# Find process using port 8081
netstat -ano | findstr :8081

# Kill the process (replace PID with actual process ID)
taskkill /PID <PID> /F

# Or change port in application.properties
```

---

### Problem: Frontend won't start

**Error**: "Port 3000 is already in use"

**Solution**:
```bash
# Kill process on port 3000
netstat -ano | findstr :3000
taskkill /PID <PID> /F
```

---

### Problem: Database connection failed

**Error**: "Connection refused to localhost:5432"

**Solution**:
```bash
# Check if containers are running
docker ps

# If not running, start them
docker-compose up -d

# Check logs
docker logs campuscompute-postgres
```

---

### Problem: Agent won't connect

**Error**: "Connection refused to localhost:8081"

**Solution**:
1. Ensure backend is running
2. Check enrollment token is correct
3. Verify WebSocket URL in config.yaml:
   ```yaml
   broker:
     url: "ws://localhost:8081/ws/agent"
   ```

---

### Problem: Frontend shows CORS error

**Solution**:
Backend CORS is already configured for http://localhost:3000. If using different port, update `CorsConfig.java`:
```java
.allowedOrigins("http://localhost:3000", "http://localhost:YOUR_PORT")
```

---

## 📊 Verify Everything is Working

### Backend Health Check
```bash
curl http://localhost:8081/api/health
```
**Expected**: `{"status":"UP"}`

### Database Check
```bash
docker exec campuscompute-postgres psql -U campuscompute -d campuscompute -c "SELECT COUNT(*) FROM organizations;"
```
**Expected**: Shows count of organizations

### Redis Check
```bash
docker exec campuscompute-redis redis-cli PING
```
**Expected**: `PONG`

### Frontend Check
Open http://localhost:3000 in browser
**Expected**: CampusCompute landing page loads

---

## 🎯 Default Credentials

After following the test flow above:

**Admin**:
- Username: `admin`
- Password: `admin123`
- Role: ORG_ADMIN

**Student** (after first-time setup):
- Student ID: `500101234`
- Username: `500101234` (same as student ID)
- Password: `password123` (what you set)
- Role: STUDENT

---

## 📁 Important Files

### Configuration Files
- `backend/src/main/resources/application.properties` - Backend config
- `frontend/vite.config.js` - Frontend proxy config
- `agent/config.yaml` - Agent configuration
- `docker-compose.yml` - Database services

### Log Files
- Backend logs: Console output
- Agent logs: `agent/logs/agent.log`
- Frontend logs: Browser console

---

## 🔄 Restart Everything

```bash
# Terminal 1: Databases
cd d:\Courses\BTech\7th_Sem\Major
docker-compose down
docker-compose up -d

# Terminal 2: Backend
cd d:\Courses\BTech\7th_Sem\Major\backend
.\mvnw spring-boot:run

# Terminal 3: Frontend
cd d:\Courses\BTech\7th_Sem\Major\frontend
npm run dev

# Terminal 4 (Optional): Agent
cd d:\Courses\BTech\7th_Sem\Major\agent
python src\main.py
```

---

## 💡 Development Tips

### Hot Reload

**Frontend**: 
- Changes to `.jsx` files reload automatically
- No restart needed

**Backend**:
- Changes require restart
- Use Spring DevTools for faster restarts (optional)

**Agent**:
- Changes require restart
- Stop with Ctrl+C and start again

### Database Reset

```bash
# Stop containers
docker-compose down

# Remove volumes (deletes all data)
docker volume rm major_postgres_data major_redis_data

# Start fresh
docker-compose up -d
```

---

## 📞 Need Help?

### Check Logs

**Backend**:
```bash
# Logs are in the terminal where you ran .\mvnw spring-boot:run
```

**Frontend**:
```bash
# Logs are in the terminal where you ran npm run dev
# Also check browser console (F12)
```

**Database**:
```bash
docker logs campuscompute-postgres
docker logs campuscompute-redis
```

**Agent**:
```bash
cat agent/logs/agent.log
```

---

## ✅ Quick Start Checklist

- [ ] Java, Node.js, Python installed
- [ ] Docker Desktop running
- [ ] PostgreSQL & Redis containers started
- [ ] Backend running on port 8081
- [ ] Frontend running on port 3000
- [ ] Opened http://localhost:3000 in browser
- [ ] Registered organization
- [ ] Logged in as admin
- [ ] Viewed dashboard
- [ ] (Optional) Started agent

---

## 🎉 You're All Set!

The CampusCompute platform is now running on your local machine.

**Next Steps**:
1. Explore the admin dashboard
2. Create student accounts
3. Test container creation
4. Try the web terminal
5. Review the documentation

**Enjoy developing!** 🚀

---

**Last Updated**: October 3, 2026  
**Tested On**: Windows 11, JDK 23, Node 20.15.1, Python 3.13, Docker Desktop
