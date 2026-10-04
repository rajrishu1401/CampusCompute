# CampusCompute - Localhost Testing Guide

**Date**: October 4, 2026  
**Setup**: Testing on single laptop (all services on localhost)  

---

## 🎯 Services Running

✅ **PostgreSQL**: localhost:5432 (Docker)  
✅ **Redis**: localhost:6379 (Docker)  
✅ **Backend**: http://localhost:8081 (Spring Boot)  
✅ **Frontend**: http://localhost:3000 (Vite)  
⏳ **Agent**: Will start after getting token (Python)  

---

## 📋 Testing Flow

### STEP 1: Register Organization (5 minutes)

**Action**: Visit http://localhost:3000

1. You'll see the Landing Page
2. Click **"Register Organization"** button
3. Fill in the form:
   ```
   Organization Name: UPES Dehradun
   Organization Code: UPES2026
   Admin Full Name: Rishu Raj
   Email: admin@test.com
   Password: Admin@123
   ```
4. Click **"Register"**
5. Wait for success message
6. You'll be redirected to login page

**Expected Result**: ✅ Registration successful, redirected to login

---

### STEP 2: Login as Admin (2 minutes)

**Action**: On login page

1. Enter credentials:
   ```
   Email: admin@test.com
   Password: Admin@123
   ```
2. Click **"Login"**
3. You'll see the Admin Dashboard

**Expected Result**: ✅ Admin dashboard with stats (all zeros initially)

**What You Should See:**
- Organization name at top
- Statistics cards (Devices: 0, Students: 0, Containers: 0)
- Sidebar with: Dashboard, Devices, Students
- Welcome message

---

### STEP 3: Generate Enrollment Token (3 minutes)

**Action**: In Admin Dashboard

1. Click **"Devices"** in sidebar
2. You'll see empty device list
3. Click **"ADD DEVICE"** button (blue, top right)
4. A dialog appears with token information

**What You'll See:**
```
Enrollment Token: eyJhbGc...xyz123
Organization ID: 1
Organization Code: UPES2026
Expires At: 2026-10-05 15:30:00

Install Instructions:
- Copy the enrollment token below
- Use it with the agent or installer
```

5. **Copy the enrollment token** (the long string)
6. Keep this dialog open or save the token somewhere

**Expected Result**: ✅ Token generated and copied

---

### STEP 4: Configure Agent (5 minutes)

**Action**: Open new terminal/command prompt

1. Navigate to agent folder:
   ```cmd
   cd d:\Courses\BTech\7th_Sem\Major\agent
   ```

2. Open config.yaml in notepad:
   ```cmd
   notepad config.yaml
   ```

3. Update the configuration:
   ```yaml
   broker:
     url: "ws://localhost:8081/ws/agent"
     enrollment_token: "PASTE-YOUR-TOKEN-HERE"  # ← Paste the token you copied
     organization_id: 1
   
   device:
     device_id: "TEST-LAPTOP-001"
     hostname: "My Test Laptop"
   
   docker:
     socket: "npipe:////./pipe/docker_engine"  # Windows
   
   heartbeat_interval: 30
   
   logging:
     level: "DEBUG"
     file: "logs/agent.log"
   ```

4. Save and close notepad

**Expected Result**: ✅ Config file updated with your token

---

### STEP 5: Start Agent (3 minutes)

**Action**: In same terminal (agent folder)

1. Start the agent:
   ```cmd
   python src/main.py
   ```

2. Watch the console output:
   ```
   2026-10-04 15:30:00 - Starting CampusCompute Agent
   2026-10-04 15:30:00 - Device ID: TEST-LAPTOP-001
   2026-10-04 15:30:00 - Organization: 1
   2026-10-04 15:30:01 - Connecting to broker: ws://localhost:8081/ws/agent
   2026-10-04 15:30:01 - Connected successfully
   2026-10-04 15:30:01 - Sending enrollment message
   2026-10-04 15:30:02 - Device registered successfully
   2026-10-04 15:30:02 - Starting heartbeat (interval: 30s)
   2026-10-04 15:30:02 - System info: CPU=8 cores, RAM=16GB, Disk=256GB
   2026-10-04 15:30:02 - Heartbeat sent
   ```

**Expected Result**: ✅ Agent connected, device registered, heartbeat started

**Leave this terminal running!**

---

### STEP 6: Verify Device in Admin Panel (2 minutes)

**Action**: Go back to browser (admin panel)

1. Close the token dialog (or refresh page)
2. You should see your device in the list!

**What You Should See:**

| Hostname | Status | CPU | RAM | Disk | Containers | Last Heartbeat |
|----------|--------|-----|-----|------|------------|----------------|
| My Test Laptop | 🟢 ONLINE | 4/8 | 8/16 GB | 128/256 GB | 0 | Just now |

3. Status should be **🟢 ONLINE** (green)
4. Last Heartbeat should be recent (< 1 minute ago)
5. Resource stats should match your laptop

**Expected Result**: ✅ Device appears with ONLINE status

---

### STEP 7: Upload Students (3 minutes)

**Action**: In Admin Panel

1. Click **"Students"** in sidebar
2. Click **"UPLOAD STUDENTS"** button
3. A dialog appears with text area
4. Paste this CSV data:
   ```
   500101234,john@test.com,John Doe
   500101235,jane@test.com,Jane Smith
   500101236,bob@test.com,Bob Wilson
   ```
5. Click **"Upload"**
6. Wait for success message

**Expected Result**: ✅ 3 students uploaded

**What You Should See:**

| Student ID | Email | Full Name | Status | Containers |
|------------|-------|-----------|--------|------------|
| 500101234 | john@test.com | John Doe | Pending | 0 |
| 500101235 | jane@test.com | Jane Smith | Pending | 0 |
| 500101236 | bob@test.com | Bob Wilson | Pending | 0 |

---

### STEP 8: Student First-Time Setup (3 minutes)

**Action**: Logout from admin, test student flow

1. Click your profile/avatar (top right)
2. Click **"Logout"**
3. Visit: http://localhost:3000/first-time-setup
4. Fill in the form:
   ```
   Student ID: 500101234
   Email: john@test.com
   Password: Student@123
   Confirm Password: Student@123
   ```
5. Click **"Complete Setup"**
6. You should auto-login to student dashboard

**Expected Result**: ✅ Student account activated, logged in

**What You Should See:**
- Student Dashboard
- Welcome message: "Welcome, John!"
- Stats: Total Containers: 0, Running: 0
- Empty container list

---

### STEP 9: Create Container (5 minutes)

**Action**: In Student Dashboard

1. Click **"Containers"** in sidebar (or click "Create Container" button)
2. Click **"CREATE CONTAINER"** button
3. Fill in the form:
   ```
   Image: ubuntu:latest
   CPU Cores: 2
   RAM: 2 GB
   Disk: 10 GB
   ```
4. Click **"Create"**
5. Wait for success notification
6. Container appears in list with **PENDING** status

**Watch the Progress:**
- Initial status: 🟡 **PENDING**
- Agent console shows: "Pulling image ubuntu:latest..."
- After 10-60 seconds: 🟢 **RUNNING**
- Terminal button becomes enabled

**Expected Result**: ✅ Container created and running

**What You Should See:**

| Image | Status | CPU | RAM | Device | Actions |
|-------|--------|-----|-----|--------|---------|
| ubuntu:latest | 🟢 RUNNING | 2 | 2 GB | My Test Laptop | Terminal Stop Delete |

---

### STEP 10: Access Terminal (5 minutes)

**Action**: In Containers page

1. Click **"Terminal"** button on your running container
2. New page/tab opens with terminal
3. Wait for connection (few seconds)
4. You should see a terminal prompt:
   ```
   root@abc123def456:/#
   ```

**Test Commands:**
```bash
# Test 1: Basic commands
ls -la
pwd
whoami

# Test 2: System info
uname -a
cat /etc/os-release

# Test 3: Install something
apt update
apt install -y curl

# Test 4: Test network
curl https://www.google.com

# Test 5: Create a file
echo "Hello from CampusCompute!" > test.txt
cat test.txt
```

**Expected Result**: ✅ All commands work, terminal is responsive

---

### STEP 11: Stop Container (2 minutes)

**Action**: Go back to Containers page

1. Click **"Stop"** button
2. Confirm if prompted
3. Status changes to 🔴 **STOPPED**
4. Terminal button becomes disabled

**Expected Result**: ✅ Container stopped successfully

---

### STEP 12: Verify Admin Dashboard Updated (2 minutes)

**Action**: Logout from student account, login as admin

1. Logout from student dashboard
2. Login as admin (admin@test.com / Admin@123)
3. View Dashboard

**What You Should See:**
- Devices: **1** (online: 1)
- Students: **3** (approved: 1, pending: 2)
- Containers: **1** (stopped: 1)
- Resource utilization stats updated

**Expected Result**: ✅ All stats updated correctly

---

## ✅ Testing Checklist

Mark as you complete:

### Organization & Auth
- [ ] Organization registration works
- [ ] Admin login works
- [ ] Admin dashboard loads
- [ ] Student first-time setup works
- [ ] Student login works

### Device Management
- [ ] Enrollment token generated
- [ ] Agent connects successfully
- [ ] Device appears in admin panel
- [ ] Device status shows ONLINE
- [ ] Resource stats displayed correctly
- [ ] Heartbeat updates regularly

### Student Management
- [ ] Bulk CSV upload works
- [ ] Students appear in list
- [ ] Student status shows correctly

### Container Management
- [ ] Container creation initiated
- [ ] Container status: PENDING → RUNNING
- [ ] Container appears in student dashboard
- [ ] Container appears on device in admin panel
- [ ] Container stats shown correctly

### Terminal
- [ ] Terminal page opens
- [ ] Terminal connects to container
- [ ] Commands work (ls, pwd, etc.)
- [ ] Can install packages (apt)
- [ ] Network works (curl)
- [ ] File operations work

### Container Actions
- [ ] Stop container works
- [ ] Container status updates to STOPPED
- [ ] Start container works (if implemented)
- [ ] Delete container works

### Dashboard Updates
- [ ] Admin dashboard reflects changes
- [ ] Statistics update in real-time
- [ ] Device stats update
- [ ] Student stats update

---

## 🐛 Common Issues & Solutions

### Issue 1: Agent Can't Connect

**Symptoms:**
```
Connection refused
Error connecting to ws://localhost:8081/ws/agent
```

**Solutions:**
1. Check backend is running: http://localhost:8081/api/health
2. Verify URL in config.yaml is correct
3. Check firewall isn't blocking port 8081
4. Restart backend if needed

---

### Issue 2: Container Stuck in PENDING

**Symptoms:**
- Container stays PENDING for > 2 minutes
- Never changes to RUNNING

**Solutions:**
1. Check agent console for errors
2. Verify Docker is running: `docker ps`
3. Image might be large (wait up to 5 minutes)
4. Check agent logs: `agent/logs/agent.log`

---

### Issue 3: Device Shows OFFLINE

**Symptoms:**
- Device appears but status is OFFLINE
- Or device disappears after being online

**Solutions:**
1. Check if agent is still running
2. Restart agent: `python src/main.py`
3. Check agent console for errors
4. Verify WebSocket connection in backend logs

---

### Issue 4: Terminal Won't Connect

**Symptoms:**
- Terminal page opens but shows "Connecting..."
- Or "Connection failed" error

**Solutions:**
1. Verify container is RUNNING (not PENDING or STOPPED)
2. Refresh the page
3. Check browser console for errors (F12)
4. Try different browser (Chrome/Firefox)
5. Check agent console for WebSocket errors

---

### Issue 5: Token Already Used

**Symptoms:**
```
Error: Enrollment token already used or invalid
```

**Solutions:**
1. Generate new token from admin panel
2. Update config.yaml with new token
3. Restart agent

---

## 📊 What to Check

### In Agent Console:
```
✅ "Connected successfully"
✅ "Device registered successfully"
✅ "Heartbeat sent" (every 30 seconds)
✅ No error messages
```

### In Admin Panel:
```
✅ Device appears in list
✅ Status: ONLINE (green)
✅ Last Heartbeat: < 1 min ago
✅ Resource stats displayed
```

### In Student Dashboard:
```
✅ Container list shows containers
✅ Status shows correctly (PENDING/RUNNING/STOPPED)
✅ Actions available (Terminal, Stop, Delete)
```

### In Terminal:
```
✅ Prompt appears: root@containerid:/#
✅ Commands execute and show output
✅ No lag or disconnections
```

---

## 🎯 Success Criteria

**Test is SUCCESSFUL if:**
1. ✅ All 12 steps completed without errors
2. ✅ Device stays ONLINE during entire test
3. ✅ Container reaches RUNNING status
4. ✅ Terminal works and commands execute
5. ✅ Admin dashboard shows correct stats
6. ✅ No errors in agent/backend logs

**Test PASSED!** 🎉

---

## 📝 Screenshot Checklist

Take screenshots for your report:

1. [ ] Landing page
2. [ ] Organization registration form
3. [ ] Admin dashboard with stats
4. [ ] Device management page with online device
5. [ ] Enrollment token dialog
6. [ ] Student management page with uploaded students
7. [ ] Student dashboard
8. [ ] Container creation form
9. [ ] Container list with running container
10. [ ] Terminal interface with commands
11. [ ] Admin dashboard with updated stats after testing

---

## 🔧 Cleanup (After Testing)

When done testing:

1. **Stop containers** (if any running)
2. **Stop agent**: Ctrl+C in agent terminal
3. **Stop frontend**: Ctrl+C in frontend terminal (or close Kiro)
4. **Stop backend**: Ctrl+C in backend terminal (or close Kiro)
5. **Keep database running** (for next test) or stop:
   ```
   docker-compose down
   ```

---

## 🎓 What This Tests

**Functionality Tested:**
- ✅ Multi-organization registration
- ✅ JWT authentication (admin & student)
- ✅ Token-based device enrollment
- ✅ WebSocket real-time communication
- ✅ Container lifecycle management
- ✅ Docker integration
- ✅ Terminal WebSocket streaming
- ✅ Real-time dashboard updates
- ✅ Statistics calculation
- ✅ Role-based access control

**Components Tested:**
- ✅ Frontend (React)
- ✅ Backend (Spring Boot)
- ✅ Agent (Python)
- ✅ Database (PostgreSQL)
- ✅ Cache (Redis)
- ✅ WebSocket (bidirectional)
- ✅ Docker (container management)

---

## 💡 Tips for Demonstration

**For Your Presentation:**
1. Practice the flow 2-3 times before demo
2. Have all terminals/windows organized
3. Use large font sizes for visibility
4. Keep agent console visible to show real-time updates
5. Prepare backup screenshots in case of live demo issues

**Impressive Points to Highlight:**
- Device auto-appears after agent starts (real-time WebSocket)
- Container status changes automatically (no refresh needed)
- Terminal works in browser (no SSH needed)
- Everything on localhost but simulates distributed system
- Multi-tenant architecture (could add another org to demo)

---

**Ready to Test!** 🚀

**Current Status:**
- ✅ Backend running
- ✅ Frontend running  
- ⏳ Ready for testing

**Start at**: http://localhost:3000

Good luck! 🎓
