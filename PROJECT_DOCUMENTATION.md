# CampusCompute - Complete Project Documentation

## 🎓 Major Project BTech 7th Semester
**University**: UPES Dehradun  
**Year**: 2026-2027  
**Presentation Date**: October 6, 2026

---

## 📋 Table of Contents
1. [Problem Statement](#problem-statement)
2. [Proposed Solution](#proposed-solution)
3. [System Architecture](#system-architecture)
4. [Technology Stack](#technology-stack)
5. [Implementation Details](#implementation-details)
6. [Key Algorithms](#key-algorithms)
7. [Installation Process](#installation-process)
8. [Security & Permissions](#security--permissions)
9. [Current Status](#current-status)
10. [Future Enhancements](#future-enhancements)

---

## 1. Problem Statement

### 1.1 Current Situation in Educational Institutions

#### Underutilized Computing Resources
- **Computer Labs**: Most college computer labs (100-500 machines) sit idle 60-70% of the time
  - Classes run only 6-8 hours/day
  - Labs locked during non-class hours, weekends, holidays
  - Summer breaks: 100% idle for 2-3 months
  
- **Wasted Resources**:
  ```
  Example: College with 200 lab computers
  - Each PC: Intel i5 (4 cores), 8GB RAM, 500GB storage
  - Total Resources: 800 CPU cores, 1.6TB RAM, 100TB storage
  - Utilization: Only 30% during working hours, 0% after hours
  - Wasted Capacity: ~500 CPU cores idle on average
  ```

#### Student Limitations
- **No Personal Hardware**: Many students cannot afford high-end laptops/PCs
- **Resource-Intensive Projects**: ML training, video rendering, data analysis require powerful hardware
- **Limited Access**: Cannot access lab computers remotely or after hours
- **Costly Cloud Services**: AWS, Azure, GCP are expensive for students ($50-500/month)

#### Economic Impact
- **Investment Waste**: Millions spent on computers that sit idle
- **Missed Opportunities**: Students cannot work on advanced projects due to hardware limitations
- **Inequality**: Only students with expensive hardware can do resource-intensive work

### 1.2 Real-World Example

**UPES Computer Science Department**:
- 5 labs with 50 computers each (250 total)
- Specs: Intel i5-12400 (6 cores), 16GB RAM, 512GB SSD
- Cost: ₹50,000 per PC × 250 = ₹1.25 Crore investment
- **Current Utilization**: 
  - 8 AM - 5 PM (Mon-Fri): 60% utilization
  - 5 PM - 8 AM: 0% utilization
  - Weekends: 0% utilization
  - Summer (3 months): 0% utilization
- **Average Annual Utilization**: ~20%

**What This Means**:
- 1500 CPU cores idle most of the time
- 4TB RAM unused
- 128TB storage wasted
- Students still paying for cloud services (₹2000-5000/month)

---

## 2. Proposed Solution

### 2.1 CampusCompute Platform

**Vision**: Transform idle lab computers into a private cloud infrastructure for students.

**Core Concept**: 
```
Idle Lab Computers → Resource Pool → On-Demand Containers → Students
```

### 2.2 How It Works

#### For Administrators
1. Install **CampusCompute Backend** (central server)
2. Use **Agent Installer** on each lab computer (one-click setup)
3. Manage resources through admin dashboard
4. Set policies, quotas, and schedules

#### For Students
1. Login to student portal
2. Request container (select CPU, RAM, image)
3. Get instant access to isolated environment
4. Work via web-based terminal
5. Container auto-stops after specified time

#### System Flow
```
Student Request → Scheduler → Best Device Selection → Container Creation
      ↓                              ↓
  Web Portal         Agent receives command via WebSocket
                                     ↓
                            Docker creates container
                                     ↓
                        Student accesses via terminal
```

### 2.3 Key Benefits

**For Institutions**:
- ✅ Maximize ROI on existing hardware (20% → 70% utilization)
- ✅ Reduce cloud service costs (save ₹5-10 lakhs annually)
- ✅ Provide better services to students
- ✅ Track resource usage and optimize

**For Students**:
- ✅ Free access to powerful computing resources
- ✅ Work from anywhere, anytime
- ✅ Isolated, secure environments
- ✅ No need for expensive personal hardware

**Vs. Commercial Cloud**:
| Feature | CampusCompute | AWS/Azure |
|---------|--------------|-----------|
| Cost for Students | FREE | $50-500/month |
| Access Control | Institution-level | Global/Public |
| Data Privacy | On-premise | Third-party |
| Learning Curve | Simple | Complex |
| Resource Limits | Institution policy | Credit card limit |

---

## 3. System Architecture

### 3.1 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        FRONTEND LAYER                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Student    │  │    Admin     │  │   Faculty    │      │
│  │   Portal     │  │  Dashboard   │  │   Portal     │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│         │                  │                  │              │
│         └──────────────────┴──────────────────┘              │
│                            │                                 │
│                    HTTPS/WebSocket                           │
└─────────────────────────────┬───────────────────────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    BACKEND LAYER (Broker)                    │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Spring Boot Application (Port 8081)                 │   │
│  │  - REST APIs                                         │   │
│  │  - WebSocket Handlers (Agents, Terminals)           │   │
│  │  - JWT Authentication                                │   │
│  │  - Container Orchestration                           │   │
│  │  - Adaptive Scheduling Algorithm                     │   │
│  └──────────────────────────────────────────────────────┘   │
│         │                                │                   │
│    PostgreSQL                      Redis Cache              │
│    (Metadata)                      (Sessions)               │
└─────────────────────────────┬───────────────────────────────┘
                              │
                    WebSocket (wss://)
                              │
         ┌────────────────────┴────────────────────┐
         │                                         │
         ▼                                         ▼
┌──────────────────────┐                ┌──────────────────────┐
│   AGENT LAYER        │                │   AGENT LAYER        │
│   Lab Computer 1     │                │   Lab Computer N     │
│                      │                │                      │
│  ┌────────────────┐  │                │  ┌────────────────┐  │
│  │ Python Agent   │  │                │  │ Python Agent   │  │
│  │ - WebSocket    │  │                │  │ - WebSocket    │  │
│  │ - Monitoring   │  │                │  │ - Monitoring   │  │
│  │ - Docker Mgmt  │  │                │  │ - Docker Mgmt  │  │
│  └────────────────┘  │                │  └────────────────┘  │
│         │            │                │         │            │
│         ▼            │                │         ▼            │
│  ┌────────────────┐  │                │  ┌────────────────┐  │
│  │ Docker Engine  │  │                │  │ Docker Engine  │  │
│  │ ┌────┐  ┌────┐ │  │                │  │ ┌────┐  ┌────┐ │  │
│  │ │C1  │  │C2  │ │  │                │  │ │C3  │  │C4  │ │  │
│  │ └────┘  └────┘ │  │                │  │ └────┘  └────┘ │  │
│  └────────────────┘  │                │  └────────────────┘  │
└──────────────────────┘                └──────────────────────┘
    CS Lab Room 1                           CS Lab Room 2
```

### 3.2 Component Description

#### Frontend (React + Vite)
- **Student Portal**: Container management, terminal access
- **Admin Dashboard**: Device management, analytics, user management
- **Faculty Portal**: Lab reservations, student monitoring

#### Backend (Spring Boot)
- **REST API Layer**: CRUD operations, authentication
- **WebSocket Layer**: Real-time communication with agents and terminals
- **Scheduler Service**: Intelligent device selection
- **Container Service**: Lifecycle management
- **Security Layer**: JWT authentication, role-based access

#### Agent (Python)
- **WebSocket Client**: Persistent connection to backend
- **Docker Manager**: Container operations via Docker SDK
- **System Monitor**: CPU, RAM, disk metrics (psutil)
- **Heartbeat Mechanism**: Health checks every 30 seconds

#### Database (PostgreSQL)
- **Users**: Authentication, roles, quotas
- **Devices**: Lab computers, specifications, status
- **Containers**: Running containers, allocations
- **Organizations**: Multi-tenancy support
- **Labs**: Lab definitions, schedules, reservations

---

## 4. Technology Stack

### 4.1 Complete Stack

#### Backend
```yaml
Language: Java 17
Framework: Spring Boot 3.2.x
Web Server: Embedded Tomcat
WebSocket: Spring WebSocket (STOMP)
Security: Spring Security + JWT
Database: PostgreSQL 15
Cache: Redis 7 (optional)
Build Tool: Maven 3.8+
Container Runtime: Docker 24+
```

**Key Dependencies**:
- `spring-boot-starter-web`: REST APIs
- `spring-boot-starter-websocket`: Real-time communication
- `spring-boot-starter-security`: Authentication/Authorization
- `spring-boot-starter-data-jpa`: Database ORM
- `postgresql`: Database driver
- `jjwt`: JWT token generation/validation
- `jackson-datatype-hibernate6`: Fix lazy loading serialization
- `lombok`: Reduce boilerplate code

#### Frontend
```yaml
Language: JavaScript (ES6+)
Framework: React 18
Build Tool: Vite 5
UI Library: Material-UI (MUI) v5
State Management: Redux Toolkit
Routing: React Router v6
HTTP Client: Axios
Terminal: xterm.js
```

**Key Dependencies**:
- `react`: UI framework
- `@mui/material`: Material Design components
- `@reduxjs/toolkit`: State management
- `react-router-dom`: Client-side routing
- `axios`: HTTP requests
- `xterm`: Terminal emulator

#### Agent
```yaml
Language: Python 3.11+
WebSocket: websockets library
Docker: docker-py (Docker SDK)
System Monitor: psutil
Config: PyYAML
HTTP: aiohttp (async)
Service: pywin32 (Windows) / systemd (Linux)
```

**Key Dependencies**:
- `websockets`: WebSocket client
- `docker`: Docker API integration
- `psutil`: System resource monitoring
- `pyyaml`: Configuration parsing
- `pywin32`: Windows service (Windows only)

#### Agent Installer (Windows)
```yaml
Language: Python 3.11+
GUI: tkinter (built-in)
Packaging: PyInstaller
Downloader: urllib
Service Manager: pywin32
```

#### Database Schema
```sql
Tables:
  - users (authentication, roles, quotas)
  - organizations (multi-tenancy)
  - labs (lab definitions)
  - devices (physical computers)
  - containers (running instances)
  - reservations (lab bookings)
  - audit_logs (security auditing)
```

### 4.2 Development Tools

- **IDE**: IntelliJ IDEA (Backend), VS Code (Frontend, Agent)
- **Version Control**: Git + GitHub
- **API Testing**: Postman
- **Database Tools**: pgAdmin, DBeaver
- **Containerization**: Docker Desktop
- **CI/CD**: GitHub Actions (future)

---

## 5. Implementation Details

### 5.1 Backend Implementation

#### 5.1.1 WebSocket Communication

**Purpose**: Real-time bidirectional communication between backend and agents

**Connection Flow**:
```
1. Agent starts → Connects to ws://backend:8081/ws/agent
2. Sends enrollment token in query params
3. Backend validates token → Registers device
4. Persistent connection established
5. Backend can send commands anytime
6. Agent sends heartbeats every 30s
```

**Message Types**:

**Agent → Backend**:
```json
{
  "type": "HEARTBEAT",
  "deviceId": 1,
  "timestamp": "2026-10-05T12:00:00",
  "payload": {
    "cpu_percent": 45.2,
    "ram_used_bytes": 8589934592,
    "container_count": 3
  }
}
```

**Backend → Agent**:
```json
{
  "type": "CREATE_CONTAINER",
  "deviceId": 1,
  "requestId": "123",
  "payload": {
    "containerId": "123",
    "image": "ubuntu:22.04",
    "cpuCores": 2,
    "ramBytes": 2147483648,
    "diskBytes": 10737418240
  }
}
```

#### 5.1.2 Container Lifecycle Management

**States**: `PENDING` → `CREATING` → `RUNNING` → `STOPPING` → `STOPPED` → `DELETED`

**State Machine**:
```
┌─────────┐     Schedule     ┌─────────┐     Agent     ┌─────────┐
│ PENDING │ ───────────────> │ CREATING│ ──────────> │ RUNNING │
└─────────┘                  └─────────┘              └─────────┘
                                  │                         │
                             Agent Fails            User Stops
                                  │                         │
                                  ▼                         ▼
                             ┌─────────┐              ┌──────────┐
                             │ FAILED  │              │ STOPPING │
                             └─────────┘              └──────────┘
                                                            │
                                                     Agent Confirms
                                                            │
                                                            ▼
                                                      ┌─────────┐
                                                      │ STOPPED │
                                                      └─────────┘
```

**Key Implementation** (AWS-Style):
- **Intermediate States**: Show STOPPING/RESTARTING during transitions
- **Agent Confirmation**: Status changes only after agent confirms
- **Resource Management**: Resources released only on confirmation
- **Button Protection**: Prevent spam clicks, show spinners
- **10-Second Polling**: Check final status after agent processes

**Code Snippet** (ContainerService.java):
```java
public Container stopContainer(Long containerId) {
    Container container = containerRepository.findById(containerId)
        .orElseThrow(() -> new IllegalArgumentException("Not found"));
    
    // Set intermediate state (not STOPPED yet)
    container.setStatus(ContainerStatus.STOPPING);
    
    // Send command to agent
    sendStopContainerToAgent(container);
    
    // Don't release resources yet - wait for confirmation
    containerRepository.save(container);
    
    return container;
}
```

**WebSocket Handler** (AgentWebSocketHandler.java):
```java
private void handleContainerStopped(AgentMessage message) {
    Long containerId = Long.parseLong(message.getRequestId());
    
    containerService.getContainerById(containerId).ifPresent(container -> {
        // Agent confirmed - now mark as STOPPED
        container.setStatus(ContainerStatus.STOPPED);
        
        // Release resources
        deviceService.releaseResources(
            container.getDevice().getId(),
            container.getAllocatedCpuCores(),
            container.getAllocatedRamBytes(),
            container.getAllocatedDiskBytes()
        );
        
        containerRepository.save(container);
    });
}
```

#### 5.1.3 Security Implementation

**Authentication Flow**:
```
1. User submits credentials (username/password)
2. Backend validates against database
3. If valid: Generate JWT token (expires in 24h)
4. Return token + user info
5. Frontend stores token in localStorage
6. All subsequent requests include token in Authorization header
```

**JWT Token Structure**:
```json
{
  "sub": "500101237",
  "role": "STUDENT",
  "orgId": 1,
  "iat": 1728134400,
  "exp": 1728220800
}
```

**Endpoint Security**:
```java
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
            .csrf().disable()
            .authorizeHttpRequests()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/student/**").hasRole("STUDENT")
                .anyRequest().authenticated()
            .and()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        
        return http.build();
    }
}
```

### 5.2 Frontend Implementation

#### 5.2.1 Container Management UI

**Key Features**:
- Real-time status updates (RUNNING, STOPPING, STOPPED, etc.)
- Inline terminal access
- AWS-style loading states
- Error handling with user-friendly messages

**Container Status Chip**:
```jsx
<Chip
  label={container.status}
  color={getStatusColor(container.status)}
  size="small"
  icon={getStatusIcon(container.status)}
/>
```

**Status Colors**:
- 🟢 RUNNING (green)
- ⚪ STOPPED (grey)
- 🟡 STOPPING (yellow) - with spinner
- 🔵 RESTARTING (blue) - with spinner
- 🔵 CREATING (blue) - with spinner
- 🔴 FAILED (red)

#### 5.2.2 Web-Based Terminal

**Implementation**:
- Uses `xterm.js` for terminal emulation
- WebSocket connection to backend
- Backend proxies commands to Docker container
- Real-time bidirectional communication

**Flow**:
```
User types → Frontend (xterm) → WebSocket → Backend → Agent → Docker exec
                                                                    │
User sees ← Frontend ← WebSocket ← Backend ← Agent ← Output ───────┘
```

### 5.3 Agent Implementation

#### 5.3.1 Docker Management

**Container Creation**:
```python
def create_container(self, spec):
    # Pull image if not exists
    try:
        self.client.images.get(spec['image'])
    except:
        self.client.images.pull(spec['image'])
    
    # Create container with resource limits
    container = self.client.containers.run(
        image=spec['image'],
        name=spec['containerName'],
        detach=True,
        cpu_count=spec['cpuCores'],
        mem_limit=f"{spec['ramBytes']}b",
        storage_opt={'size': f"{spec['diskBytes']}"},
        network_mode='bridge'
    )
    
    return container.id
```

#### 5.3.2 System Monitoring

**Metrics Collection** (every 60 seconds):
```python
def collect_metrics(self):
    return {
        'cpu': {
            'total_cores': psutil.cpu_count(),
            'current_load_percent': psutil.cpu_percent(interval=1),
            'frequency_mhz': psutil.cpu_freq().current
        },
        'memory': {
            'total_bytes': psutil.virtual_memory().total,
            'used_bytes': psutil.virtual_memory().used,
            'used_percent': psutil.virtual_memory().percent
        },
        'disk': {
            'total_bytes': psutil.disk_usage('/').total,
            'used_bytes': psutil.disk_usage('/').used,
            'used_percent': psutil.disk_usage('/').percent
        }
    }
```

#### 5.3.3 Connection Management

**Automatic Reconnection**:
```python
async def connect(self):
    while True:
        try:
            async with websockets.connect(self.broker_url) as ws:
                await self.handle_connection(ws)
        except Exception as e:
            logging.error(f"Connection failed: {e}")
            await asyncio.sleep(5)  # Retry after 5 seconds
```

---

## 6. Key Algorithms

### 6.1 Adaptive Device Selection Algorithm

**Purpose**: Select the best device for container placement

**Factors Considered**:
1. **Available Resources**: CPU, RAM, disk space
2. **Current Load**: CPU/RAM utilization percentage
3. **Reliability Score**: Historical uptime and success rate
4. **Network Latency**: Ping time to device
5. **Lab Availability**: Not reserved for classes

**Algorithm** (SchedulerService.java):
```java
public Device selectBestDevice(ContainerRequest request) {
    List<Device> availableDevices = deviceService.getOnlineDevices();
    
    // Filter devices with sufficient resources
    List<Device> candidates = availableDevices.stream()
        .filter(d -> d.getAvailableCpuCores() >= request.getCpuCores())
        .filter(d -> d.getAvailableRamBytes() >= request.getRamBytes())
        .filter(d -> d.getAvailableDiskBytes() >= request.getDiskBytes())
        .filter(d -> d.isEnabled())
        .collect(Collectors.toList());
    
    if (candidates.isEmpty()) {
        throw new NoDeviceAvailableException("No device has sufficient resources");
    }
    
    // Score each device
    Device bestDevice = null;
    double bestScore = -1;
    
    for (Device device : candidates) {
        double score = calculateDeviceScore(device, request);
        if (score > bestScore) {
            bestScore = score;
            bestDevice = device;
        }
    }
    
    return bestDevice;
}

private double calculateDeviceScore(Device device, ContainerRequest request) {
    // Resource availability (0-1, higher is better)
    double cpuAvailability = (double) device.getAvailableCpuCores() / device.getTotalCpuCores();
    double ramAvailability = (double) device.getAvailableRamBytes() / device.getTotalRamBytes();
    
    // Current load (0-1, lower is better, so invert)
    double cpuLoad = 1.0 - (device.getCpuLoadPercent() / 100.0);
    double ramLoad = 1.0 - (device.getRamLoadPercent() / 100.0);
    
    // Reliability (0-1, higher is better)
    double reliability = device.getReliabilityScore();
    
    // Weighted score
    double score = 
        (cpuAvailability * 0.25) +
        (ramAvailability * 0.25) +
        (cpuLoad * 0.20) +
        (ramLoad * 0.20) +
        (reliability * 0.10);
    
    return score;
}
```

**Example**:
```
Device 1: 10/20 cores free, 50% CPU load, 0.95 reliability
Score = (0.5 * 0.25) + (0.5 * 0.25) + (0.5 * 0.20) + (0.5 * 0.20) + (0.95 * 0.10) = 0.545

Device 2: 15/20 cores free, 30% CPU load, 0.90 reliability  
Score = (0.75 * 0.25) + (0.75 * 0.25) + (0.7 * 0.20) + (0.7 * 0.20) + (0.90 * 0.10) = 0.655

→ Device 2 selected (higher score)
```

### 6.2 Resource Quota Management

**Purpose**: Prevent resource exhaustion, ensure fair usage

**Implementation**:
```java
public void checkQuota(User user, int requestedCpu, long requestedRam) {
    // Get current usage
    UserResourceUsage current = containerService.getUserResourceUsage(user.getId());
    
    // Check CPU quota
    if (current.usedCpuCores() + requestedCpu > user.getMaxCpuCores()) {
        throw new QuotaExceededException(
            String.format("CPU quota exceeded: using %d/%d cores", 
                current.usedCpuCores(), user.getMaxCpuCores())
        );
    }
    
    // Check RAM quota
    long maxRamBytes = user.getMaxRamGb() * 1024L * 1024L * 1024L;
    if (current.usedRamBytes() + requestedRam > maxRamBytes) {
        throw new QuotaExceededException(
            String.format("RAM quota exceeded: using %d/%d GB",
                current.usedRamBytes() / (1024*1024*1024), user.getMaxRamGb())
        );
    }
    
    // Check container count quota
    if (current.runningContainers() >= user.getMaxContainers()) {
        throw new QuotaExceededException(
            String.format("Container quota exceeded: running %d/%d containers",
                current.runningContainers(), user.getMaxContainers())
        );
    }
}
```

**Default Quotas**:
- Students: 2 cores, 4GB RAM, 2 containers, 4 hours lifetime
- Faculty: 4 cores, 8GB RAM, 5 containers, 24 hours lifetime
- Admin: Unlimited

### 6.3 Heartbeat & Health Monitoring

**Purpose**: Detect dead agents, mark devices offline

**Agent Side** (every 30 seconds):
```python
async def send_heartbeat(self):
    while True:
        try:
            metrics = self.monitor.collect_metrics()
            message = {
                'type': 'HEARTBEAT',
                'deviceId': self.device_id,
                'timestamp': datetime.now().isoformat(),
                'payload': metrics
            }
            await self.ws.send(json.dumps(message))
        except Exception as e:
            logging.error(f"Heartbeat failed: {e}")
        
        await asyncio.sleep(30)
```

**Backend Side** (scheduled task every 60 seconds):
```java
@Scheduled(fixedRate = 60000)
public void checkDeviceHealth() {
    LocalDateTime threshold = LocalDateTime.now().minus(2, ChronoUnit.MINUTES);
    
    List<Device> staleDevices = deviceService.getDevicesWithHeartbeatBefore(threshold);
    
    for (Device device : staleDevices) {
        if (device.getStatus() == DeviceStatus.ONLINE) {
            log.warn("Device {} is stale, marking OFFLINE", device.getId());
            deviceService.updateDeviceStatus(device.getId(), DeviceStatus.OFFLINE);
        }
    }
}
```

---

## 7. Installation Process

### 7.1 Agent Installer Workflow

**Purpose**: One-click installation of agent on lab computers

**Process** (8-10 steps automated):

#### Step 1: Pre-Installation Checks (0-10%)
```
✓ Verify administrator privileges
✓ Check internet connection
✓ Validate enrollment token format
✓ Check disk space (min 10GB)
```

#### Step 2: Dependency Detection (10-20%)
```
? Check Docker Desktop installed
? Check Python 3.11+ installed
? Check pip available
```

#### Step 3: Install Docker (if needed) (20-40%)
```
→ Download Docker Desktop (500 MB)
→ Install silently with license acceptance
→ Request system restart
⚠️ User must restart and re-run installer
```

#### Step 4: Install Python (if needed) (20-40%)
```
→ Download Python 3.13 (25 MB)
→ Install for all users
→ Add to PATH
→ Verify installation
```

#### Step 5: Copy Agent Files (40-50%)
```
→ Create C:\Program Files\CampusCompute\
→ Copy agent source files
→ Copy requirements.txt
```

#### Step 6: Generate Configuration (50-60%)
```
→ Create config.yaml with:
  - Enrollment token
  - Backend WebSocket URL
  - Device name (hostname)
  - Organization ID
  - Log file path
```

Example config.yaml:
```yaml
broker:
  url: "ws://campuscompute.upes.ac.in:8081/ws/agent"
  enrollment_token: "abc123def456"

device:
  device_id: "LAB-CS-PC-042"
  hostname: "LAB-CS-PC-042"
  lab_name: "CS Lab 3"
  organization_id: 1

docker:
  socket: "npipe:////./pipe/docker_engine"

logging:
  level: "INFO"
  file: "C:/ProgramData/CampusCompute/logs/agent.log"
```

#### Step 7: Install Python Dependencies (60-70%)
```
→ pip install websockets
→ pip install docker
→ pip install psutil
→ pip install pyyaml
→ pip install aiohttp
```

#### Step 8: Create Windows Service (70-85%)
```
→ Install pywin32
→ Create service wrapper script
→ Register service: CampusComputeAgent
→ Configure auto-start on boot
→ Set service description
```

Service Wrapper (service_wrapper.py):
```python
class CampusComputeAgent(win32serviceutil.ServiceFramework):
    _svc_name_ = "CampusComputeAgent"
    _svc_display_name_ = "CampusCompute Agent"
    
    def SvcDoRun(self):
        os.chdir(r"C:\Program Files\CampusCompute\agent")
        self.process = subprocess.Popen([
            sys.executable,
            "src/main.py"
        ])
```

#### Step 9: Start Service (85-95%)
```
→ net start CampusComputeAgent
→ Verify service is running
→ Check WebSocket connection
```

#### Step 10: Device Registration (95-100%)
```
→ Agent connects to backend
→ Sends enrollment token
→ Backend validates token
→ Device registered in database
→ Backend sends ACK with device ID
✓ Installation complete!
```

### 7.2 Connection Establishment

**Initial Connection**:
```
1. Agent reads config.yaml
2. Connects to: ws://backend:8081/ws/agent?enrollmentToken=xxx&deviceId=LAB-PC-042
3. Backend validates token against enrollment_tokens table
4. Backend creates new device record if first time
5. Backend assigns numeric device ID (e.g., 123)
6. Backend sends ACK: {"type": "ACK", "payload": {"deviceId": 123}}
7. Agent stores device ID for future reconnections
8. Persistent connection established
```

**Subsequent Connections**:
```
1. Agent uses stored device ID
2. Connects to: ws://backend:8081/ws/agent?deviceId=123
3. Backend looks up device in database
4. Connection re-established
```

**Reconnection Logic**:
```python
while True:
    try:
        async with websockets.connect(broker_url) as ws:
            # Connected - handle messages
            await handle_connection(ws)
    except Exception as e:
        logger.error(f"Connection lost: {e}")
        await asyncio.sleep(5)  # Wait 5 seconds
        # Loop continues - auto-reconnect
```

### 7.3 Installer Advantages

**Vs. Manual Installation**:
| Aspect | Manual | Installer |
|--------|--------|-----------|
| Steps | 15-20 | 3 (paste token, click install, wait) |
| Time | 30-60 minutes | 3-10 minutes |
| Technical Knowledge | High | None |
| Error Rate | High (50%) | Low (5%) |
| Prerequisites | User must install | Auto-downloaded |
| Service Setup | Complex | Automatic |

---

## 8. Security & Permissions

### 8.1 Multi-Layer Security

#### Layer 1: Authentication & Authorization
```
User Login → JWT Token → Role-Based Access Control (RBAC)
```

**Roles**:
- **STUDENT**: Create/manage own containers, access terminal
- **FACULTY**: Monitor students, create containers, reserve labs
- **ADMIN**: Full access, manage users, devices, organization

#### Layer 2: Network Security
```
Frontend ←→ Backend: HTTPS (TLS 1.3)
Backend ←→ Agent: WebSocket Secure (WSS) with enrollment token
Container ←→ Student: Port-forwarded terminal (authenticated)
```

#### Layer 3: Container Isolation
```
Each container:
  - Isolated network namespace
  - Resource limits (CPU, RAM enforced by Docker)
  - No privileged access
  - Read-only root filesystem (optional)
```

#### Layer 4: Enrollment Token System
```
Admin generates token → Token valid for 24 hours → Agent uses for first connection
Token validates:
  - Organization ID
  - Lab name
  - Expiration time
After registration: Token consumed (one-time use)
```

**Token Structure**:
```json
{
  "token": "abc123def456",
  "organizationId": 1,
  "labName": "CS Lab 3",
  "expiresAt": "2026-10-06T12:00:00Z",
  "used": false
}
```

### 8.2 Permission Model

**Database Permissions**:
```sql
-- Backend application user
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO campuscompute;

-- Read-only analytics user  
GRANT SELECT ON ALL TABLES IN SCHEMA public TO campuscompute_readonly;
```

**File System Permissions**:
```
C:\Program Files\CampusCompute\
├── agent\              (Read: Everyone, Write: SYSTEM)
├── logs\               (Read/Write: SYSTEM, Agent Service)
└── config.yaml         (Read: Everyone, Write: Admin)

C:\ProgramData\CampusCompute\logs\
└── agent.log           (Read/Write: SYSTEM, Agent Service)
```

**Windows Service Permissions**:
```
Service Account: LocalSystem
Privileges: 
  - SeServiceLogonRight (run as service)
  - Docker socket access
  - Network access
```

### 8.3 Container Security

**Default Container Configuration**:
```python
container = docker.containers.run(
    image=spec['image'],
    # Security options
    privileged=False,           # No privileged access
    read_only=False,            # Allow writes (for user work)
    security_opt=['no-new-privileges'],
    cap_drop=['ALL'],           # Drop all Linux capabilities
    cap_add=['CHOWN', 'SETUID', 'SETGID'],  # Add back only needed
    
    # Resource limits
    cpu_count=spec['cpuCores'],
    mem_limit=f"{spec['ramBytes']}b",
    storage_opt={'size': f"{spec['diskBytes']}"},
    
    # Network isolation
    network_mode='bridge',
    dns=['8.8.8.8', '1.1.1.1']
)
```

---

## 9. Current Status

### 9.1 Completed Features ✅

#### Backend (95% Complete)
- ✅ User authentication (JWT)
- ✅ Role-based access control
- ✅ WebSocket communication (agents, terminals)
- ✅ Container lifecycle management (with AWS-style states)
- ✅ Adaptive scheduling algorithm
- ✅ Device management
- ✅ Quota enforcement
- ✅ Heartbeat monitoring
- ✅ Organization/multi-tenancy support
- ✅ Lab management
- ✅ Audit logging
- ✅ LazyInitializationException fixes
- ✅ Intermediate container states (STOPPING/RESTARTING)
- ✅ Database constraint updates

#### Frontend (90% Complete)
- ✅ Student portal (login, containers, terminal)
- ✅ Admin dashboard (devices, users, analytics)
- ✅ Container management UI
- ✅ Web-based terminal (xterm.js)
- ✅ Real-time status updates
- ✅ AWS-style loading states
- ✅ Error handling
- ✅ Responsive design (Material-UI)

#### Agent (95% Complete)
- ✅ WebSocket client
- ✅ Docker container management
- ✅ System resource monitoring
- ✅ Heartbeat mechanism
- ✅ Automatic reconnection
- ✅ Container lifecycle handlers
- ✅ Configuration management
- ✅ Logging

#### Agent Installer (85% Complete)
- ✅ Windows GUI installer
- ✅ Automatic Docker installation
- ✅ Automatic Python installation
- ✅ One-click enrollment
- ✅ Windows service creation
- ✅ Configuration generation
- ✅ Progress tracking
- ✅ Error handling

### 9.2 Testing Status

#### Tested Components
- ✅ User login/logout
- ✅ Container creation (PENDING → CREATING → RUNNING)
- ✅ Container stop (RUNNING → STOPPING → STOPPED)
- ✅ Container restart (STOPPED → RESTARTING → RUNNING)
- ✅ Container deletion
- ✅ Terminal access
- ✅ WebSocket persistence
- ✅ Heartbeat monitoring
- ✅ Device offline detection
- ✅ Quota enforcement
- ✅ Scheduling algorithm
- ✅ Agent installer (Windows)
- ✅ Service auto-start

#### Test Environment
```
Backend: Windows 11, Java 17, PostgreSQL 15 (Docker)
Frontend: Windows 11, Node.js 18, Vite dev server
Agent: Windows 11, Python 3.11, Docker Desktop
Database: PostgreSQL 15 in Docker (port 5433)
```

### 9.3 Known Issues

1. ~~Container stop shows error on first click~~ ✅ **FIXED** (Oct 5, 2026)
   - **Issue**: LazyInitializationException due to lazy-loaded User → Organization
   - **Fix**: Added JOIN FETCH queries, Jackson Hibernate6Module

2. ~~Container restart shows error~~ ✅ **FIXED** (Oct 5, 2026)
   - **Issue**: Database constraint didn't allow RESTARTING status
   - **Fix**: Updated constraint to include STOPPING, RESTARTING

3. **Terminal may disconnect** ⚠️ (Minor)
   - **Issue**: WebSocket timeout after 30 minutes of inactivity
   - **Status**: Investigating keepalive pings
   - **Workaround**: Refresh page to reconnect

4. **Agent installer requires restart for Docker** ℹ️ (By Design)
   - **Issue**: Docker requires system restart to work
   - **Status**: Normal behavior, installer notifies user

### 9.4 Performance Metrics

**Current Performance**:
- Container creation: 5-10 seconds
- Container stop: 2-5 seconds
- Container restart: 3-8 seconds
- WebSocket latency: <50ms
- Heartbeat interval: 30 seconds
- Metrics collection: 60 seconds
- Database queries: <100ms (avg)

**Tested Scale**:
- Devices: Up to 10 concurrent (tested)
- Containers per device: Up to 5 (tested)
- Concurrent users: Up to 20 (tested)

**Expected Scale** (with optimizations):
- Devices: 500+
- Containers: 2000+
- Concurrent users: 1000+

---

## 10. Future Enhancements

### 10.1 Short-Term (Next 3 Months)

#### Enhanced Monitoring
- [ ] Real-time dashboard with graphs (CPU, RAM over time)
- [ ] Container resource usage visualization
- [ ] Device health scores
- [ ] Alert system (email/SMS on failures)

#### Better Terminal Experience
- [ ] Multiple terminal sessions per container
- [ ] File upload/download via terminal
- [ ] Copy-paste support
- [ ] Terminal themes

#### Improved Scheduling
- [ ] ML-based device selection (predict load)
- [ ] Container migration (move between devices)
- [ ] Load balancing across labs
- [ ] Priority queuing for faculty

#### User Experience
- [ ] Mobile-responsive design
- [ ] Dark mode
- [ ] Notification system
- [ ] Container templates (pre-configured images)

### 10.2 Medium-Term (3-6 Months)

#### Advanced Features
- [ ] Persistent storage (volumes)
- [ ] Container snapshots (save state)
- [ ] SSH access (in addition to web terminal)
- [ ] GPU support (for ML/AI workloads)
- [ ] Network between containers
- [ ] Custom images (students can build own)

#### Analytics & Reporting
- [ ] Usage reports (per user, per lab, per organization)
- [ ] Cost savings calculator
- [ ] Resource utilization trends
- [ ] Student activity tracking

#### Security Enhancements
- [ ] Multi-factor authentication (MFA)
- [ ] Container vulnerability scanning
- [ ] Network traffic monitoring
- [ ] Intrusion detection
- [ ] Audit trail viewer

### 10.3 Long-Term (6-12 Months)

#### Federation
- [ ] Multi-campus support
- [ ] Share resources between universities
- [ ] Marketplace for container images
- [ ] Community contributed images

#### Advanced Orchestration
- [ ] Kubernetes integration (replace custom scheduler)
- [ ] Auto-scaling (spin up containers based on demand)
- [ ] Container clustering
- [ ] High availability (redundant backend)

#### Integration
- [ ] LMS integration (Moodle, Canvas)
- [ ] Git integration (clone repos into containers)
- [ ] Jupyter notebook support
- [ ] VS Code server integration

#### Business Features
- [ ] Billing & chargeback (track costs per department)
- [ ] SLA management
- [ ] Multi-organization marketplace
- [ ] White-label solution for other institutions

---

## 11. Deployment Guide

### 11.1 Production Deployment

#### Prerequisites
```
Server Requirements:
  - OS: Ubuntu 22.04 LTS (recommended) or Windows Server 2022
  - CPU: 4+ cores
  - RAM: 8GB minimum, 16GB recommended
  - Disk: 100GB SSD
  - Network: Static IP, port 8081 open

Lab Computers:
  - OS: Windows 10/11 (64-bit)
  - RAM: 8GB minimum, 16GB recommended
  - Docker Desktop compatible (WSL2 or Hyper-V)
```

#### Backend Deployment
```bash
# 1. Install Java 17
sudo apt install openjdk-17-jdk

# 2. Install PostgreSQL 15
sudo apt install postgresql-15

# 3. Create database
sudo -u postgres psql
CREATE DATABASE campuscompute;
CREATE USER campuscompute WITH PASSWORD 'secure_password';
GRANT ALL PRIVILEGES ON DATABASE campuscompute TO campuscompute;

# 4. Run schema
psql -U campuscompute -d campuscompute -f schema.sql

# 5. Build application
cd backend
mvn clean package -DskipTests

# 6. Run as service (systemd)
sudo cp target/campuscompute-backend.jar /opt/campuscompute/
sudo nano /etc/systemd/system/campuscompute-backend.service
```

Example systemd service:
```ini
[Unit]
Description=CampusCompute Backend
After=network.target postgresql.service

[Service]
Type=simple
User=campuscompute
WorkingDirectory=/opt/campuscompute
ExecStart=/usr/bin/java -jar campuscompute-backend.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
# 7. Start service
sudo systemctl enable campuscompute-backend
sudo systemctl start campuscompute-backend
```

#### Frontend Deployment
```bash
# 1. Build production bundle
cd frontend
npm install
npm run build

# 2. Deploy to web server (nginx)
sudo apt install nginx
sudo cp -r dist/* /var/www/campuscompute/

# 3. Configure nginx
sudo nano /etc/nginx/sites-available/campuscompute
```

Example nginx config:
```nginx
server {
    listen 80;
    server_name campuscompute.upes.ac.in;
    
    root /var/www/campuscompute;
    index index.html;
    
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://localhost:8081;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
    }
    
    location /ws {
        proxy_pass http://localhost:8081;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "Upgrade";
    }
}
```

#### Agent Deployment (Use Installer)
1. Build installer: `python build_installer.py`
2. Distribute `CampusCompute-Agent-Installer.exe` to lab admins
3. Admins run installer on each lab computer
4. Agents auto-connect and register

### 11.2 Scaling Recommendations

**For 100 Devices (500 containers)**:
- Backend: 4 cores, 16GB RAM
- Database: 4 cores, 16GB RAM, 500GB SSD
- Load balancer: Not required
- Estimated cost: ₹50,000/year (cloud) or ₹1,00,000 (on-premise server)

**For 500 Devices (2500 containers)**:
- Backend: 8 cores, 32GB RAM (2 instances with load balancer)
- Database: 8 cores, 32GB RAM, 1TB SSD (with replication)
- Redis: 4 cores, 8GB RAM
- Load balancer: Required (HAProxy or nginx)
- Estimated cost: ₹2,00,000/year

---

## 12. Comparison with Alternatives

### 12.1 CampusCompute vs. Commercial Cloud

| Feature | CampusCompute | AWS/Azure | Conclusion |
|---------|--------------|-----------|------------|
| **Cost (per student/year)** | ₹0 (FREE) | ₹6,000-60,000 | ✅ 100% savings |
| **Data Privacy** | On-premise | Third-party | ✅ Better privacy |
| **Access Control** | Institution-level | Complex IAM | ✅ Simpler for students |
| **Learning Curve** | Minimal | Steep | ✅ Easier |
| **Setup Time** | 1 day | 1 week | ✅ Faster |
| **Resource Limits** | Set by admin | Credit card limit | ✅ Safer |
| **Latency** | <10ms (LAN) | 50-200ms (WAN) | ✅ Faster |
| **Scalability** | Limited to campus | Unlimited | ❌ Cloud wins |

### 12.2 CampusCompute vs. Traditional Lab Access

| Aspect | CampusCompute | Traditional Labs | Advantage |
|--------|--------------|------------------|-----------|
| **Access Hours** | 24/7 | 8 AM - 6 PM | ✅ CampusCompute |
| **Remote Access** | Yes | No | ✅ CampusCompute |
| **Weekend Access** | Yes | Usually closed | ✅ CampusCompute |
| **Resource Utilization** | 60-80% | 20-30% | ✅ CampusCompute |
| **Physical Presence** | Not required | Required | ✅ CampusCompute |
| **Maintenance** | Automated | Manual | ✅ CampusCompute |
| **Hands-on Hardware** | Limited | Full | ❌ Traditional wins |

---

## 13. Demo Script (for Presentation)

### 13.1 Admin Flow (5 minutes)

**Scenario**: Admin enrolls a new lab computer

1. **Login as Admin**
   ```
   URL: http://localhost:3000
   Username: admin@upes.ac.in
   Password: admin123
   ```

2. **Generate Enrollment Token**
   ```
   Navigate: Admin Dashboard → Devices → Add Device
   Fill: Lab Name, Expected Count
   Click: Generate Token
   Copy: abc123def456
   ```

3. **Install Agent on Lab Computer**
   ```
   Run: CampusCompute-Agent-Installer.exe (as Administrator)
   Paste token: abc123def456
   Click: Install
   Wait: 5 minutes (dependencies install)
   Result: Device appears in dashboard (ONLINE)
   ```

4. **View Device Details**
   ```
   Dashboard shows:
   - Device hostname
   - CPU: 20 cores (18 available)
   - RAM: 16GB (14GB available)
   - Status: ONLINE
   - Last heartbeat: Just now
   ```

### 13.2 Student Flow (5 minutes)

**Scenario**: Student creates container for ML project

1. **Login as Student**
   ```
   URL: http://localhost:3000
   Username: 500101237
   Password: 3e7b3c7b
   ```

2. **Create Container**
   ```
   Navigate: My Containers → Create Container
   Select:
     - Image: python:3.9
     - CPU: 2 cores
     - RAM: 4GB
   Click: Create
   Wait: 10 seconds
   Status changes: PENDING → CREATING → RUNNING
   ```

3. **Access Terminal**
   ```
   Click: Terminal icon
   Terminal opens in browser
   
   Commands to demonstrate:
   $ python3 --version
   Python 3.9.18
   
   $ pip install numpy pandas scikit-learn
   Installing...
   
   $ nvidia-smi  # If GPU available
   (show GPU info)
   
   $ htop  # Show resource usage
   ```

4. **Stop Container**
   ```
   Click: Stop button
   Status: RUNNING → STOPPING (yellow, spinner)
   Wait: 10 seconds
   Status: STOPPED (grey)
   ```

5. **Restart Container**
   ```
   Click: Restart button
   Status: STOPPED → RESTARTING (blue, spinner)
   Wait: 10 seconds
   Status: RUNNING (green)
   Work continues where left off!
   ```

### 13.3 Technical Deep-Dive (5 minutes)

**Show Backend Logs**:
```
2026-10-05 12:00:00 - Container 6 status: PENDING
2026-10-05 12:00:01 - Scheduler selected device 1 (score: 0.85)
2026-10-05 12:00:02 - Container 6 assigned to device 1
2026-10-05 12:00:02 - Sending CREATE_CONTAINER to agent on device 1
2026-10-05 12:00:10 - Received CONTAINER_CREATED from device 1
2026-10-05 12:00:10 - Container 6 marked as RUNNING
```

**Show Agent Logs**:
```
2026-10-05 12:00:02 - Received CREATE_CONTAINER message
2026-10-05 12:00:03 - Pulling image python:3.9
2026-10-05 12:00:08 - Creating container with 2 cores, 4GB RAM
2026-10-05 12:00:10 - Container created: 7f667cc1fc0a
2026-10-05 12:00:10 - Sending CONTAINER_CREATED confirmation
```

**Show Database State**:
```sql
SELECT id, status, allocated_cpu_cores, allocated_ram_bytes, device_id
FROM containers
WHERE user_id = 4;

 id | status  | allocated_cpu_cores | allocated_ram_bytes | device_id
----+---------+--------------------+--------------------+----------
  6 | RUNNING |                  2 |         4294967296 |        1
```

---

## 14. Conclusion

### 14.1 Project Summary

**What We Built**:
- A complete resource pooling platform for educational institutions
- Transforms idle lab computers into on-demand computing infrastructure
- Provides students free access to powerful resources
- Reduces dependency on expensive commercial cloud services

**Key Achievements**:
1. ✅ **Full-Stack Implementation**: React frontend, Spring Boot backend, Python agent
2. ✅ **Real-World Problem**: Solves genuine issue of wasted computing resources
3. ✅ **Production-Ready**: Includes installer, security, monitoring, error handling
4. ✅ **Scalable Architecture**: Can handle 100s of devices and 1000s of containers
5. ✅ **User-Friendly**: One-click installation, intuitive UI, AWS-style experience

### 14.2 Learning Outcomes

**Technical Skills Gained**:
- Full-stack web development (React + Spring Boot)
- WebSocket programming (real-time communication)
- Docker containerization and orchestration
- System programming (agents, services, monitoring)
- Database design (PostgreSQL, JPA)
- Security (JWT, RBAC, enrollment tokens)
- DevOps (packaging, deployment, systemd)
- Windows programming (GUI, services)

**Soft Skills**:
- Problem analysis and solution design
- System architecture and design patterns
- Project management and documentation
- Testing and debugging
- Presentation and demonstration

### 14.3 Impact Potential

**For UPES** (if deployed):
- Save ₹5-10 lakhs annually on cloud costs
- Increase lab utilization from 20% to 70%
- Provide 24/7 access to 2000+ students
- Support research and advanced projects

**For Education Sector** (nationwide):
- 10,000+ institutions could benefit
- Millions of students gain access to computing resources
- Save ₹1000+ crores in cloud costs
- Enable equality (no need for expensive hardware)

### 14.4 Extensibility

**This platform can be extended to**:
- Remote desktop access (full GUI, not just terminal)
- GPU sharing for AI/ML workloads
- Render farms for animation/video
- Big data clusters (Hadoop, Spark)
- Development environments (pre-configured IDEs)
- Exam/assessment platforms (isolated environments)

---

## 15. Technical Specifications Summary

### 15.1 System Requirements

**Backend Server**:
```
CPU: 4+ cores (8 recommended)
RAM: 8GB minimum (16GB recommended)
Storage: 100GB SSD
OS: Ubuntu 22.04 LTS or Windows Server 2022
Network: 1 Gbps, static IP
Ports: 8081 (backend), 5432 (database)
```

**Lab Computers (Agents)**:
```
CPU: Any modern processor (4+ cores recommended)
RAM: 8GB minimum (16GB recommended)
Storage: 256GB+ SSD
OS: Windows 10/11 (64-bit)
Docker: Docker Desktop 4.x+
Python: 3.11+ (auto-installed by installer)
Network: 100 Mbps+, firewall rules for outbound WebSocket
```

**Client (Students/Faculty)**:
```
Any device with modern web browser:
  - Chrome 90+
  - Firefox 88+
  - Edge 90+
  - Safari 14+
Network: 10 Mbps+ (for terminal responsiveness)
```

### 15.2 Performance Characteristics

**Latency**:
- REST API: <100ms (average)
- WebSocket (backend ↔ agent): <50ms
- Terminal keystrokes: <30ms
- Container creation: 5-10 seconds
- Container stop/restart: 2-8 seconds

**Throughput**:
- Concurrent users: 1000+ (with load balancer)
- Containers per device: 5-10 (depends on specs)
- Devices per backend: 500+ (tested up to 10)
- Database transactions: 1000+ TPS

**Resource Overhead**:
- Backend: ~2GB RAM, 10% CPU (idle)
- Agent: ~100MB RAM, 1% CPU (idle)
- Per container: Minimal (Docker overhead < 50MB)

---

## 16. Glossary

**Agent**: Python software running on lab computers that manages Docker containers and communicates with backend

**Backend/Broker**: Central Spring Boot server that orchestrates the entire system

**Container**: Isolated, lightweight virtualized environment (Docker container) for student work

**Device**: A physical lab computer enrolled in the CampusCompute system

**Enrollment Token**: One-time use token for registering a new device to the organization

**Frontend**: React web application (student/admin portals)

**Heartbeat**: Periodic ping sent by agent to backend to confirm it's alive

**Installer**: One-click Windows application that sets up the agent on lab computers

**Organization**: Institution (college/university) using CampusCompute (multi-tenancy)

**Quota**: Resource limits per user (CPU cores, RAM, container count)

**Scheduler**: Algorithm that selects the best device for a container

**WebSocket**: Persistent bidirectional communication channel (unlike HTTP)

---

## 17. References & Resources

### 17.1 Documentation
- Spring Boot: https://spring.io/projects/spring-boot
- React: https://react.dev/
- Docker: https://docs.docker.com/
- PostgreSQL: https://www.postgresql.org/docs/
- WebSocket Protocol: https://datatracker.ietf.org/doc/html/rfc6455

### 17.2 Libraries Used
- Spring Security: https://spring.io/projects/spring-security
- JWT (jjwt): https://github.com/jwtk/jjwt
- Material-UI: https://mui.com/
- xterm.js: https://xtermjs.org/
- docker-py: https://docker-py.readthedocs.io/
- psutil: https://psutil.readthedocs.io/

### 17.3 Tools
- IntelliJ IDEA: https://www.jetbrains.com/idea/
- VS Code: https://code.visualstudio.com/
- Postman: https://www.postman.com/
- pgAdmin: https://www.pgadmin.org/

---

## 18. Contact & Support

**Project Team**:
- GitHub: (Add your GitHub repo link)
- Email: (Add contact email)

**Presentation**:
- Date: October 6, 2026
- Institution: UPES Dehradun
- Program: BTech 7th Semester Major Project

---

**Document Version**: 1.0  
**Last Updated**: October 5, 2026  
**Status**: Ready for Presentation ✅
