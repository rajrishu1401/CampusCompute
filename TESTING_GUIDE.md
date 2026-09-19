# Testing Guide - End-to-End Container Creation

This guide walks you through testing the complete container creation flow from API to Docker.

---

## 🎯 What We're Testing

**Complete Flow**:
1. User sends POST request to `/api/containers`
2. Backend checks quota
3. Backend schedules container to best device
4. Backend sends CREATE_CONTAINER message to agent via WebSocket
5. Agent receives message and creates Docker container
6. Agent sends CONTAINER_CREATED response
7. Backend updates container status to RUNNING
8. User receives success response

---

## 📋 Prerequisites

### 1. Database Setup
You need a PostgreSQL database with at least one device and user registered.

**Start PostgreSQL** (if using Docker):
```bash
# Your existing container: reachinbox-postgres
# Already running on localhost:5432
```

**Register a Device** (via SQL or API):
```sql
-- Connect to campuscompute database
psql -U reachinbox -d campuscompute

-- Insert a test device
INSERT INTO devices (device_id, hostname, lab_name, ip_address, status, 
                     total_cpu_cores, total_ram_bytes, total_disk_bytes, 
                     used_cpu_cores, used_ram_bytes, used_disk_bytes, 
                     cpu_load_percent, ram_load_percent, reliability_score, enabled, 
                     created_at, updated_at)
VALUES ('LAB-PC-001', 'lab-pc-01', 'Computer Science Lab', '127.0.0.1', 'OFFLINE',
        8, 17179869184, 107374182400,  -- 8 cores, 16GB RAM, 100GB disk
        0, 0, 0,
        0.0, 0.0, 1.0, true,
        NOW(), NOW());

-- Get the device ID (note it down - you'll need it)
SELECT id, device_id, hostname, lab_name FROM devices WHERE device_id = 'LAB-PC-001';
```

### 2. Update Agent Configuration

Edit `agent/config.yaml`:
```yaml
device:
  id: 1  # Use the ID from the database query above
```

### 3. Install Agent Dependencies

```bash
cd agent
pip install -r requirements.txt
```

---

## 🚀 Step-by-Step Testing

### STEP 1: Start Backend

```bash
cd backend
mvn spring-boot:run
```

**Expected Output**:
```
2026-09-20 02:20:00 - Started CampusComputeApplication in 5.123 seconds
2026-09-20 02:20:00 - Tomcat started on port(s): 8081 (http)
```

**Verify Backend is Running**:
```bash
curl http://localhost:8081/api/health
```

Expected: `{"status":"UP"}`

---

### STEP 2: Start Agent

**Open a new terminal**:
```bash
cd agent
python src/main.py
```

**Expected Output**:
```
2026-09-20 02:21:00 - Starting CampusCompute Agent...
2026-09-20 02:21:00 - System: Windows 11
2026-09-20 02:21:00 - CPU: 8 cores
2026-09-20 02:21:00 - RAM: 16.00 GB
2026-09-20 02:21:00 - Docker: 24.0.7
2026-09-20 02:21:00 - Connecting to broker: ws://localhost:8081/ws/agent (Device ID: 1)
2026-09-20 02:21:01 - ✅ Connected to broker successfully
2026-09-20 02:21:01 - ✅ Agent started successfully
2026-09-20 02:21:01 - Heartbeat sent: CPU=15.2% RAM=45.3%
```

**Verify Connection in Backend Logs**:
```
2026-09-20 02:21:01 - New WebSocket connection established: xxxxx
2026-09-20 02:21:01 - Agent connected for device 1: lab-pc-01
2026-09-20 02:21:01 - Processing HEARTBEAT message from device 1
2026-09-20 02:21:01 - Device 1 marked as ONLINE
```

---

### STEP 3: Get JWT Token

**Login to get JWT**:
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "student1",
    "password": "password123"
  }'
```

**Expected Response**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzdHVkZW50MSIsImlhdCI6MTcwMDAwMDAwMCwiZXhwIjoxNzAwMDg2NDAwfQ...",
  "type": "Bearer",
  "username": "student1",
  "email": "student1@upes.ac.in"
}
```

**Save the token**:
```bash
export JWT_TOKEN="<your-token-here>"
```

---

### STEP 4: Create Container via API

**Send container creation request**:
```bash
curl -X POST http://localhost:8081/api/containers \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "image": "ubuntu:24.04",
    "cpuCores": 2,
    "ramBytes": 2147483648,
    "diskBytes": 10737418240,
    "lifetimeMs": 14400000
  }'
```

**Parameters**:
- `cpuCores`: 2 CPU cores
- `ramBytes`: 2GB (2 * 1024 * 1024 * 1024)
- `diskBytes`: 10GB (10 * 1024 * 1024 * 1024)
- `lifetimeMs`: 4 hours (4 * 60 * 60 * 1000)

---

### STEP 5: Monitor the Flow

#### Backend Logs (Terminal 1):
```
2026-09-20 02:25:00 - Creating container request for user ID: 1
2026-09-20 02:25:00 - Checking quota for user 1
2026-09-20 02:25:00 - Quota check passed
2026-09-20 02:25:00 - Created container 1 in PENDING state
2026-09-20 02:25:00 - Selecting device for container request: 2 cores, 2048 MB RAM
2026-09-20 02:25:00 - Found 1 online devices
2026-09-20 02:25:00 - Using scheduling strategy: AdaptiveReservationAware
2026-09-20 02:25:00 - Selected device: 1 (lab-pc-01)
2026-09-20 02:25:00 - Assigning container 1 to device 1
2026-09-20 02:25:00 - Sending CREATE_CONTAINER message to device 1 for container 1
2026-09-20 02:25:00 - CREATE_CONTAINER message sent successfully to device 1
2026-09-20 02:25:01 - Processing CONTAINER_CREATED message from device 1
2026-09-20 02:25:01 - Container 1 marked as RUNNING in database
```

#### Agent Logs (Terminal 2):
```
2026-09-20 02:25:00 - Received CREATE_CONTAINER message from broker
2026-09-20 02:25:00 - Handling broker message: CREATE_CONTAINER
2026-09-20 02:25:00 - Creating container: container-1-1726780500000 (image: ubuntu:24.04)
2026-09-20 02:25:00 - Resources: 2 cores, 2.00 GB RAM
2026-09-20 02:25:00 - Pulling image: ubuntu:24.04
2026-09-20 02:25:05 - Creating container: container-1-1726780500000
2026-09-20 02:25:06 - Container started: a3f5b8c9d1e2
2026-09-20 02:25:06 - ✅ Container created successfully: a3f5b8c9d1e2
2026-09-20 02:25:06 - Sent CONTAINER_CREATED message to broker
```

---

### STEP 6: Verify Container is Running

**Check in Docker**:
```bash
docker ps
```

**Expected**:
```
CONTAINER ID   IMAGE          COMMAND       CREATED          STATUS          PORTS     NAMES
a3f5b8c9d1e2   ubuntu:24.04   "/bin/bash"   10 seconds ago   Up 9 seconds              container-1-1726780500000
```

**Check in Database**:
```sql
SELECT id, container_name, status, image, device_id, user_id 
FROM containers 
WHERE id = 1;
```

**Expected**:
```
 id | container_name              | status  | image         | device_id | user_id
----+-----------------------------+---------+---------------+-----------+---------
  1 | container-1-1726780500000   | RUNNING | ubuntu:24.04  |         1 |       1
```

**Get Container Stats via API**:
```bash
curl -H "Authorization: Bearer $JWT_TOKEN" \
  http://localhost:8081/api/containers/stats
```

**Expected Response**:
```json
{
  "totalContainers": 1,
  "runningContainers": 1,
  "usedCpuCores": 2,
  "usedRamBytes": 2147483648,
  "remainingQuota": {
    "maxContainers": 5,
    "remainingContainers": 4,
    "maxCpuCores": 8,
    "remainingCpuCores": 6,
    "maxRamGb": 16,
    "remainingRamGb": 14
  }
}
```

---

## ✅ Success Criteria

Your integration is working if:

1. ✅ Backend starts without errors
2. ✅ Agent connects to broker (WebSocket established)
3. ✅ Agent sends heartbeats every 30 seconds
4. ✅ Device status changes to ONLINE in database
5. ✅ API request returns success (HTTP 200)
6. ✅ Container appears in `docker ps`
7. ✅ Container status is RUNNING in database
8. ✅ Backend logs show CREATE_CONTAINER → CONTAINER_CREATED flow
9. ✅ Agent logs show container creation success

---

## ❌ Troubleshooting

### Problem: Agent Can't Connect to Broker

**Symptoms**:
```
Failed to connect: [Errno 10061] No connection could be made...
```

**Solutions**:
1. Check backend is running on port 8081
2. Check `agent/config.yaml` has correct broker URL
3. Verify device ID exists in database
4. Check firewall settings

---

### Problem: "Device not found" Error

**Symptoms**:
```
Device 1 not found, closing connection
```

**Solutions**:
1. Check device exists in database:
   ```sql
   SELECT id FROM devices WHERE id = 1;
   ```
2. Update `agent/config.yaml` with correct device ID
3. Restart agent

---

### Problem: "No online devices available"

**Symptoms**:
```
No online devices available for scheduling
```

**Solutions**:
1. Check agent is connected (look for "Connected to broker" in agent logs)
2. Check device status:
   ```sql
   SELECT id, device_id, status FROM devices;
   ```
3. If status is OFFLINE, restart agent
4. Wait for heartbeat to update status to ONLINE

---

### Problem: Docker Permission Denied

**Symptoms** (Agent):
```
docker.errors.DockerException: Error while fetching server API version
```

**Solutions**:

**Linux/Mac**:
```bash
sudo usermod -aG docker $USER
# Logout and login again
```

**Windows**:
- Ensure Docker Desktop is running
- Check Docker is accessible from command line: `docker ps`

---

### Problem: Container Creation Fails

**Symptoms**:
```
Failed to create container: <error>
```

**Solutions**:
1. Check Docker is running: `docker ps`
2. Check image exists or can be pulled: `docker pull ubuntu:24.04`
3. Check disk space: `df -h`
4. Check Docker logs: `docker logs <container-id>`
5. Check agent has sufficient resources

---

## 🧪 Additional Tests

### Test 1: Multiple Containers
```bash
# Create 3 containers
for i in {1..3}; do
  curl -X POST http://localhost:8081/api/containers \
    -H "Authorization: Bearer $JWT_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
      "image": "ubuntu:24.04",
      "cpuCores": 1,
      "ramBytes": 1073741824,
      "diskBytes": 5368709120,
      "lifetimeMs": 3600000
    }'
done
```

### Test 2: Quota Exceeded
```bash
# Try to exceed quota (e.g., request 100 cores)
curl -X POST http://localhost:8081/api/containers \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "image": "ubuntu:24.04",
    "cpuCores": 100,
    "ramBytes": 2147483648,
    "diskBytes": 10737418240,
    "lifetimeMs": 14400000
  }'
```

**Expected**: HTTP 400 with "Quota exceeded" error

### Test 3: Agent Reconnection
1. Stop agent (Ctrl+C)
2. Wait 10 seconds
3. Start agent again
4. Check it reconnects automatically
5. Create a container to verify it works

---

## 📊 Monitoring

### View All Containers
```bash
curl -H "Authorization: Bearer $JWT_TOKEN" \
  http://localhost:8081/api/containers
```

### View Device Status
```bash
curl http://localhost:8081/api/devices
```

### View WebSocket Sessions (Backend Logs)
Look for:
```
Registered WebSocket session for device 1
```

---

## 🎉 Success!

If all tests pass, you have successfully integrated:
- ✅ Backend WebSocket communication
- ✅ Agent WebSocket client
- ✅ Container scheduling
- ✅ Quota enforcement
- ✅ Docker container creation
- ✅ Bidirectional messaging

**Next Steps**:
1. Test with multiple agents (different lab PCs)
2. Implement terminal WebSocket for interactive access
3. Build frontend UI
4. Add monitoring dashboard

---

## 📝 Notes

- First container creation will be slow (Docker pulls image)
- Subsequent containers with same image are fast
- Heartbeats run every 30 seconds
- Containers auto-expire after `lifetimeMs`
- Device status updates to OFFLINE if no heartbeat for 60 seconds

**Enjoy your working CampusCompute platform! 🚀**
