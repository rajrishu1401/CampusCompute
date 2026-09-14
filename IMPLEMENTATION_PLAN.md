# CampusCompute - Complete Implementation Plan

## 📋 Project Setup Checklist

### Prerequisites Installation

#### Development Machine Requirements
- [ ] **Java 17+** - `java -version` (OpenJDK or Oracle JDK)
- [ ] **Maven 3.8+** - `mvn -version` (or use Spring Boot wrapper)
- [ ] **Python 3.11+** - `python --version`
- [ ] **Node.js 18+** - `node -v`
- [ ] **Docker Desktop** (Windows) - For testing agent locally
- [ ] **PostgreSQL 15+** - Database (or use Docker)
- [ ] **Redis 7+** - Caching (or use Docker)
- [ ] **Git** - Version control
- [ ] **IDE**: IntelliJ IDEA (backend), VS Code (frontend/agent), PyCharm (agent)

---

## 🏗️ Phase-by-Phase Implementation

### **PHASE 1: Backend Foundation (Week 1-3)**

#### Week 1: Project Setup & Authentication

**Day 1-2: Initialize Spring Boot Project**

```bash
# Create backend directory structure
mkdir campuscompute
cd campuscompute
mkdir backend frontend agent infrastructure docs
```

**Backend Dependencies (pom.xml)**:
```xml
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-websocket</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- Database -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>
    
    <!-- Redis -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    
    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.3</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.12.3</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.12.3</version>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Lombok (optional, for reducing boilerplate) -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

**Tasks**:
- [ ] Create Spring Boot project (Spring Initializr or manual)
- [ ] Setup package structure:
  ```
  edu.upes.campuscompute
  ├── auth/
  ├── user/
  ├── device/
  ├── container/
  ├── lab/
  ├── reservation/
  ├── quota/
  ├── scheduler/
  ├── websocket/
  ├── monitoring/
  ├── audit/
  ├── config/
  └── exception/
  ```
- [ ] Configure `application.yml` (database, JWT secret, server port)
- [ ] Setup PostgreSQL database (local or Docker)
- [ ] Create database: `CREATE DATABASE campuscompute;`

**Day 3-4: User Authentication System**

**Database Schema**:
```sql
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    department_id INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
```

**Implementation**:
- [ ] Create `User` entity
- [ ] Create `UserRepository` (JPA)
- [ ] Create `UserService` (registration, login logic)
- [ ] Create `AuthController` (login, register, logout endpoints)
- [ ] Implement JWT generation and validation
- [ ] Create `JwtAuthenticationFilter`
- [ ] Configure Spring Security
- [ ] Add password encryption (BCrypt)

**API Endpoints**:
```
POST /api/auth/register
POST /api/auth/login
POST /api/auth/logout
POST /api/auth/refresh
GET  /api/auth/me
```

**Day 5-7: Role-Based Access Control**

- [ ] Create `Role` enum (STUDENT, FACULTY, ADMIN, SUPER_ADMIN)
- [ ] Implement `@PreAuthorize` annotations
- [ ] Create method security configuration
- [ ] Add role checking in services
- [ ] Test with different user roles
- [ ] Create seed data (admin user, test students)

**Deliverable**: Working authentication system with JWT

---

#### Week 2: Device Management & Agent Communication

**Day 1-2: Device Registration**

**Database Schema**:
```sql
CREATE TABLE labs (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    department_id INTEGER,
    building VARCHAR(100),
    room VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE devices (
    id SERIAL PRIMARY KEY,
    lab_id INTEGER REFERENCES labs(id),
    hostname VARCHAR(255) UNIQUE NOT NULL,
    ip_address VARCHAR(45),
    mac_address VARCHAR(17),
    status VARCHAR(50) NOT NULL, -- PENDING, ACTIVE, DISABLED, DRAINED
    enrollment_token_hash VARCHAR(255),
    cpu_cores INTEGER,
    ram_mb INTEGER,
    storage_gb INTEGER,
    os_info TEXT,
    docker_version VARCHAR(50),
    last_heartbeat TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_devices_status ON devices(status);
CREATE INDEX idx_devices_lab ON devices(lab_id);
```

**Implementation**:
- [ ] Create `Lab` entity and repository
- [ ] Create `Device` entity and repository
- [ ] Create `DeviceService` (enrollment, status management)
- [ ] Create `DeviceController` (admin endpoints)
- [ ] Implement enrollment token generation
- [ ] Create device approval workflow

**API Endpoints**:
```
POST   /api/admin/devices/generate-token
POST   /api/devices/enroll (with enrollment token)
GET    /api/admin/devices
GET    /api/admin/devices/:id
PATCH  /api/admin/devices/:id/enable
PATCH  /api/admin/devices/:id/disable
PATCH  /api/admin/devices/:id/drain
DELETE /api/admin/devices/:id
```

**Day 3-5: WebSocket for Agent Communication**

**Database Schema**:
```sql
CREATE TABLE heartbeats (
    id SERIAL PRIMARY KEY,
    device_id INTEGER REFERENCES devices(id),
    cpu_usage DECIMAL(5,2),
    ram_usage DECIMAL(5,2),
    disk_usage DECIMAL(5,2),
    container_count INTEGER,
    docker_healthy BOOLEAN,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_heartbeats_device ON heartbeats(device_id, timestamp DESC);
```

**Implementation**:
- [ ] Configure Spring WebSocket
- [ ] Create `AgentWebSocketHandler`
- [ ] Implement connection management (device ID mapping)
- [ ] Create `AgentMessage` DTOs (HEARTBEAT, CREATE_CONTAINER, etc.)
- [ ] Implement message routing
- [ ] Add connection authentication (mTLS or token-based initially)
- [ ] Create `HeartbeatService` to store device metrics

**WebSocket Protocol**:
```json
// Agent -> Broker (Heartbeat)
{
  "type": "HEARTBEAT",
  "deviceId": "device-123",
  "timestamp": "2026-09-05T10:30:00Z",
  "resources": {
    "cpuUsage": 45.2,
    "ramUsage": 60.5,
    "diskUsage": 30.0,
    "containerCount": 3
  }
}

// Broker -> Agent (Create Container)
{
  "type": "CREATE_CONTAINER",
  "requestId": "req-789",
  "containerId": "container-456",
  "image": "ubuntu:24.04",
  "resources": {
    "cpuCores": 2,
    "ramMb": 4096,
    "diskGb": 20
  }
}
```

**Day 6-7: Testing & Integration**

- [ ] Test device enrollment flow
- [ ] Test heartbeat reception
- [ ] Create admin dashboard API for device list
- [ ] Document API endpoints (Swagger/OpenAPI)

**Deliverable**: Backend can register devices and receive heartbeats

---

#### Week 3: Container Management APIs

**Day 1-3: Container Lifecycle Backend**

**Database Schema**:
```sql
CREATE TABLE containers (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) NOT NULL,
    device_id INTEGER REFERENCES devices(id),
    container_name VARCHAR(255) UNIQUE NOT NULL,
    docker_container_id VARCHAR(64),
    image VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL, -- PENDING, CREATING, RUNNING, STOPPED, FAILED, DELETED
    cpu_cores INTEGER NOT NULL,
    ram_mb INTEGER NOT NULL,
    storage_gb INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP,
    stopped_at TIMESTAMP,
    expires_at TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE volumes (
    id SERIAL PRIMARY KEY,
    container_id INTEGER REFERENCES containers(id),
    volume_name VARCHAR(255) UNIQUE NOT NULL,
    mount_path VARCHAR(255) NOT NULL,
    size_gb INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_containers_user ON containers(user_id);
CREATE INDEX idx_containers_device ON containers(device_id);
CREATE INDEX idx_containers_status ON containers(status);
```

**Implementation**:
- [ ] Create `Container` entity and repository
- [ ] Create `Volume` entity and repository
- [ ] Create `ContainerService` (CRUD operations)
- [ ] Create `ContainerController` (student endpoints)
- [ ] Implement ownership validation (user can only access their containers)
- [ ] Add container state machine (PENDING → CREATING → RUNNING)

**API Endpoints**:
```
GET    /api/containers              # List user's containers
POST   /api/containers              # Request new container
GET    /api/containers/:id          # Get container details
PATCH  /api/containers/:id/start    # Start stopped container
PATCH  /api/containers/:id/stop     # Stop running container
DELETE /api/containers/:id          # Delete container
GET    /api/containers/:id/logs     # Get container logs
```

**Day 4-5: Quota System**

**Database Schema**:
```sql
CREATE TABLE quotas (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) UNIQUE,
    max_containers INTEGER NOT NULL,
    max_cpu_cores INTEGER NOT NULL,
    max_ram_mb INTEGER NOT NULL,
    max_storage_gb INTEGER NOT NULL,
    max_duration_hours INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Implementation**:
- [ ] Create `Quota` entity and repository
- [ ] Create `QuotaService` (validation logic)
- [ ] Add quota check before container creation
- [ ] Calculate current usage (sum of user's active containers)
- [ ] Return helpful error messages when quota exceeded

**Day 6-7: Basic Scheduler (First-Available)**

**Implementation**:
- [ ] Create `SchedulerService`
- [ ] Implement device selection logic:
  ```
  1. Filter devices by status=ACTIVE
  2. Filter devices with enough CPU/RAM
  3. Filter devices with low load
  4. Select first available
  ```
- [ ] Integrate with `ContainerService`
- [ ] Send `CREATE_CONTAINER` command to agent via WebSocket
- [ ] Handle agent response (success/failure)

**Deliverable**: Students can create containers via API (scheduled to available devices)

---

### **PHASE 2: Agent Development (Week 4-5)**

#### Week 4: Python Agent Core

**Day 1-2: Project Setup**

**Directory Structure**:
```
agent/
├── main.py
├── config.py
├── connection.py
├── heartbeat.py
├── monitor.py
├── docker_manager.py
├── container_policy.py
├── terminal.py
├── storage.py
├── requirements.txt
└── systemd/
    └── campuscompute-agent.service
```

**requirements.txt**:
```
docker==7.0.0
websocket-client==1.6.4
psutil==5.9.6
pyyaml==6.0.1
cryptography==41.0.7
```

**config.yaml**:
```yaml
broker:
  url: "wss://campuscompute.upes.ac.in/ws/agent"
  
device:
  enrollment_token: ""  # Generated by admin
  
monitoring:
  heartbeat_interval: 30  # seconds
  
docker:
  socket: "/var/run/docker.sock"
  
security:
  max_cpu_per_container: 4
  max_ram_per_container_mb: 8192
  max_storage_per_container_gb: 50
```

**Day 3-4: Docker Integration**

**docker_manager.py**:
- [ ] Create `DockerManager` class
- [ ] Implement `create_container()` with resource limits
- [ ] Implement `start_container()`
- [ ] Implement `stop_container()`
- [ ] Implement `delete_container()`
- [ ] Implement `get_container_stats()`
- [ ] Add security policies (no privileged, cap drop)

**Example**:
```python
import docker

class DockerManager:
    def __init__(self):
        self.client = docker.from_env()
    
    def create_container(self, request):
        """Create container with security policies"""
        container = self.client.containers.create(
            image=request['image'],
            name=request['containerName'],
            detach=True,
            cpu_count=request['resources']['cpuCores'],
            mem_limit=f"{request['resources']['ramMb']}m",
            security_opt=['no-new-privileges:true'],
            cap_drop=['ALL'],
            cap_add=['CHOWN', 'SETGID', 'SETUID'],
            pids_limit=512,
            storage_opt={'size': f"{request['resources']['diskGb']}G"}
        )
        return container
```

**Day 5: Heartbeat & Monitoring**

**monitor.py**:
- [ ] Use `psutil` to get system metrics
- [ ] Get Docker container count
- [ ] Check Docker daemon health

**heartbeat.py**:
- [ ] Schedule periodic heartbeat (30 seconds)
- [ ] Send metrics to broker
- [ ] Handle network failures

**Day 6-7: WebSocket Connection**

**connection.py**:
- [ ] Implement WebSocket connection to broker
- [ ] Handle reconnection with exponential backoff
- [ ] Parse incoming messages (CREATE_CONTAINER, DELETE_CONTAINER, etc.)
- [ ] Send responses back to broker

**main.py**:
```python
def main():
    config = load_config()
    docker_mgr = DockerManager()
    connection = BrokerConnection(config)
    heartbeat = HeartbeatService(connection, docker_mgr)
    
    # Start heartbeat thread
    heartbeat.start()
    
    # Connect to broker
    connection.connect()
    
    # Message loop
    while True:
        msg = connection.receive_message()
        handle_message(msg, docker_mgr)
```

**Deliverable**: Agent can connect to broker, send heartbeats, and create containers

---

#### Week 5: Agent Advanced Features

**Day 1-3: Terminal Session Handling**

**terminal.py**:
- [ ] Implement `docker exec` for terminal sessions
- [ ] Forward stdin/stdout between WebSocket and Docker
- [ ] Handle session cleanup

**Day 4-5: Storage Management**

**storage.py**:
- [ ] Create Docker volumes for persistence
- [ ] Implement volume lifecycle (create, attach, delete)
- [ ] Add volume size limits

**Day 6: Installation & Systemd**

**Installation Script** (`install.sh`):
```bash
#!/bin/bash
# Install Python dependencies
pip3 install -r requirements.txt

# Copy systemd service
sudo cp systemd/campuscompute-agent.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable campuscompute-agent
sudo systemctl start campuscompute-agent
```

**Day 7: Testing & Integration**

- [ ] Test full container lifecycle (backend → agent → Docker)
- [ ] Test reconnection scenarios
- [ ] Test resource limit enforcement
- [ ] Verify security policies

**Deliverable**: Complete agent that manages containers on lab PCs

---

### **PHASE 3: Advanced Backend Features (Week 6-7)**

#### Week 6: Reservations & Advanced Quota

**Day 1-3: Lab Reservation System**

**Database Schema**:
```sql
CREATE TABLE reservations (
    id SERIAL PRIMARY KEY,
    lab_id INTEGER REFERENCES labs(id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    created_by INTEGER REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_reservations_time ON reservations(lab_id, start_time, end_time);
```

**Implementation**:
- [ ] Create `Reservation` entity and repository
- [ ] Create `ReservationService`
- [ ] Add overlap detection (prevent double booking)
- [ ] Create admin API endpoints
- [ ] Integrate with scheduler (avoid reserved labs)

**Day 4-5: Monitoring & Statistics**

**Database Schema**:
```sql
CREATE TABLE resource_usage (
    id SERIAL PRIMARY KEY,
    container_id INTEGER REFERENCES containers(id),
    cpu_percent DECIMAL(5,2),
    memory_mb INTEGER,
    network_rx_bytes BIGINT,
    network_tx_bytes BIGINT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_usage_container ON resource_usage(container_id, timestamp DESC);
```

**Implementation**:
- [ ] Create endpoints to get campus-wide statistics
- [ ] Create endpoints for per-device statistics
- [ ] Create endpoints for per-user statistics
- [ ] Add aggregation queries (daily/weekly utilization)

**Day 6-7: Audit Logging**

**Database Schema**:
```sql
CREATE TABLE audit_logs (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50),
    resource_id INTEGER,
    details JSONB,
    ip_address VARCHAR(45),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_user ON audit_logs(user_id, timestamp DESC);
```

**Implementation**:
- [ ] Create `AuditService`
- [ ] Add audit logging to all admin actions
- [ ] Create admin endpoint to view logs

**Deliverable**: Complete backend with reservations, monitoring, audit

---

#### Week 7: Terminal WebSocket Proxy

**Day 1-4: Terminal Proxy**

**Implementation**:
- [ ] Create `/ws/terminal/:containerId` WebSocket endpoint
- [ ] Authenticate user (owns container?)
- [ ] Forward connection to device agent
- [ ] Stream stdin/stdout bidirectionally
- [ ] Handle connection cleanup

**Terminal Flow**:
```
Student Browser (xterm.js)
    ↓ WebSocket
Campus Broker (Spring Boot)
    ↓ WebSocket (forward)
Lab PC Agent (Python)
    ↓ docker exec
Docker Container (bash)
```

**Day 5-7: File Upload/Download**

**API Endpoints**:
```
POST   /api/containers/:id/files/upload
GET    /api/containers/:id/files/download?path=/workspace/file.txt
GET    /api/containers/:id/files?path=/workspace    # List files
DELETE /api/containers/:id/files?path=/file.txt
```

**Implementation**:
- [ ] Use `docker cp` via agent
- [ ] Stream files via WebSocket or HTTP multipart
- [ ] Add file size limits
- [ ] Validate file paths (prevent escaping container)

**Deliverable**: Students can use terminal and manage files

---

### **PHASE 4: Frontend Development (Week 8-10)**

#### Week 8: Frontend Setup & Authentication

**Day 1-2: Project Setup**

```bash
cd frontend
npm create vite@latest . -- --template react-ts
npm install
npm install react-router-dom axios @tanstack/react-query
npm install tailwindcss postcss autoprefixer
npm install xterm @xterm/xterm @xterm/addon-fit
npm install zustand # Optional state management
npx tailwindcss init -p
```

**Directory Structure**:
```
frontend/src/
├── components/
│   ├── layout/
│   │   ├── Header.tsx
│   │   ├── Sidebar.tsx
│   │   └── Footer.tsx
│   ├── auth/
│   │   ├── LoginForm.tsx
│   │   └── ProtectedRoute.tsx
│   └── common/
│       ├── Button.tsx
│       ├── Input.tsx
│       ├── Card.tsx
│       └── Modal.tsx
├── pages/
│   ├── student/
│   │   ├── Dashboard.tsx
│   │   ├── Containers.tsx
│   │   ├── Terminal.tsx
│   │   └── Profile.tsx
│   ├── admin/
│   │   ├── Dashboard.tsx
│   │   ├── Devices.tsx
│   │   ├── Users.tsx
│   │   ├── Reservations.tsx
│   │   └── Monitoring.tsx
│   ├── Login.tsx
│   └── NotFound.tsx
├── services/
│   ├── api.ts
│   ├── auth.service.ts
│   ├── container.service.ts
│   ├── device.service.ts
│   └── websocket.service.ts
├── hooks/
│   ├── useAuth.ts
│   ├── useContainers.ts
│   └── useWebSocket.ts
├── types/
│   ├── user.types.ts
│   ├── container.types.ts
│   └── device.types.ts
├── utils/
│   ├── formatters.ts
│   └── validators.ts
├── App.tsx
└── main.tsx
```

**Day 3-4: Authentication Flow**

**Services** (`auth.service.ts`):
```typescript
export const authService = {
  async login(email: string, password: string) {
    const response = await axios.post('/api/auth/login', { email, password });
    localStorage.setItem('token', response.data.token);
    return response.data;
  },
  
  async logout() {
    localStorage.removeItem('token');
    await axios.post('/api/auth/logout');
  },
  
  async getMe() {
    return axios.get('/api/auth/me');
  }
};
```

**Components**:
- [ ] Create `LoginForm` component
- [ ] Create `ProtectedRoute` wrapper
- [ ] Setup routing with React Router
- [ ] Implement token storage and axios interceptor
- [ ] Add auto-redirect on 401

**Day 5-7: Layout & Dashboard**

- [ ] Create responsive layout (sidebar, header)
- [ ] Create student dashboard (container count, resource usage)
- [ ] Create admin dashboard (device count, active containers)
- [ ] Add navigation

**Deliverable**: Login flow and dashboard skeletons

---

#### Week 9: Student Portal

**Day 1-3: Container Management UI**

**Pages**:
- [ ] Container list page (table view)
- [ ] Create container modal (image, CPU, RAM selection)
- [ ] Container detail page (status, logs, actions)
- [ ] Start/stop/delete buttons

**Components**:
- [ ] `ContainerCard` - Display container info
- [ ] `CreateContainerModal` - Form to request container
- [ ] `ContainerList` - Table or grid of containers
- [ ] `ResourceSelector` - CPU/RAM sliders

**Day 4-7: Browser Terminal**

**Terminal Component** (`Terminal.tsx`):
```typescript
import { Terminal } from '@xterm/xterm';
import { FitAddon } from '@xterm/addon-fit';

export function TerminalComponent({ containerId }: Props) {
  useEffect(() => {
    const term = new Terminal();
    const fitAddon = new FitAddon();
    term.loadAddon(fitAddon);
    
    // Open terminal
    term.open(terminalRef.current);
    fitAddon.fit();
    
    // Connect WebSocket
    const ws = new WebSocket(`wss://broker/ws/terminal/${containerId}`);
    
    // Terminal -> WebSocket
    term.onData((data) => {
      ws.send(JSON.stringify({ type: 'input', data }));
    });
    
    // WebSocket -> Terminal
    ws.onmessage = (event) => {
      const msg = JSON.parse(event.data);
      term.write(msg.data);
    };
    
    return () => {
      ws.close();
      term.dispose();
    };
  }, [containerId]);
  
  return <div ref={terminalRef} />;
}
```

**Tasks**:
- [ ] Implement terminal component
- [ ] Add fullscreen mode
- [ ] Handle WebSocket reconnection
- [ ] Add copy/paste support
- [ ] Style terminal (colors, fonts)

**Deliverable**: Students can create containers and use terminal

---

#### Week 10: Admin Portal

**Day 1-2: Device Management**

**Pages**:
- [ ] Device list (table with status, CPU, RAM, last heartbeat)
- [ ] Device detail page (metrics, containers, actions)
- [ ] Enrollment token generation modal
- [ ] Enable/disable/drain buttons

**Day 3-4: Lab & Reservation Management**

**Pages**:
- [ ] Lab list and create lab form
- [ ] Reservation calendar view
- [ ] Create reservation modal (lab, time range)

**Day 5-6: User & Quota Management**

**Pages**:
- [ ] User list (students, faculty, admins)
- [ ] Edit user modal (role, quota)
- [ ] Quota configuration form

**Day 7: Monitoring Dashboard**

**Components**:
- [ ] Campus resource utilization chart (line chart)
- [ ] Device health status cards
- [ ] Active containers count
- [ ] Recent activity log

**Deliverable**: Complete admin portal

---

### **PHASE 5: Advanced Features (Week 11-12)**

#### Week 11: Adaptive Scheduler

**Day 1-4: Implement Reservation-Aware Scheduling**

**Scheduler Enhancement** (`SchedulerService.java`):
```java
public Device selectDevice(ContainerRequest request) {
    List<Device> candidates = deviceRepository.findByStatus(ACTIVE);
    
    // Score each device
    return candidates.stream()
        .map(device -> new DeviceScore(
            device,
            calculateScore(device, request)
        ))
        .max(Comparator.comparing(DeviceScore::getScore))
        .map(DeviceScore::getDevice)
        .orElseThrow(() -> new NoDeviceAvailableException());
}

private double calculateScore(Device device, ContainerRequest request) {
    double cpuScore = calculateCpuScore(device);
    double ramScore = calculateRamScore(device);
    double loadScore = calculateLoadScore(device);
    double proximityScore = calculateReservationProximityScore(
        device, 
        request.getEstimatedDuration()
    );
    
    return 0.3 * cpuScore 
         + 0.3 * ramScore 
         + 0.2 * loadScore 
         + 0.2 * proximityScore;
}

private double calculateReservationProximityScore(Device device, Duration duration) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime endTime = now.plus(duration);
    
    // Find next reservation in this device's lab
    Optional<Reservation> nextReservation = reservationRepository
        .findNextReservation(device.getLab().getId(), now);
    
    if (nextReservation.isEmpty()) {
        return 1.0; // No upcoming reservation = best score
    }
    
    LocalDateTime reservationStart = nextReservation.get().getStartTime();
    Duration timeUntilReservation = Duration.between(now, reservationStart);
    
    // If container would conflict with reservation, low score
    if (endTime.isAfter(reservationStart)) {
        return 0.1;
    }
    
    // More time = better score
    long hours = timeUntilReservation.toHours();
    return Math.min(1.0, hours / 24.0);
}
```

**Tasks**:
- [ ] Implement scoring algorithm
- [ ] Add reservation proximity calculation
- [ ] Add host load tracking
- [ ] Test scheduling decisions

**Day 5-7: Performance Benchmarking**

- [ ] Create load testing script (simulate 100 concurrent requests)
- [ ] Measure container startup time (P50, P95, P99)
- [ ] Measure scheduling decision time
- [ ] Measure terminal latency
- [ ] Document results

**Deliverable**: Academic-aware scheduler with benchmarks

---

#### Week 12: Production Readiness

**Day 1-2: Error Handling & Validation**

- [ ] Add comprehensive error handling
- [ ] Add input validation (backend and frontend)
- [ ] Create user-friendly error messages
- [ ] Add retry logic for transient failures

**Day 3-4: Security Hardening**

- [ ] Implement rate limiting
- [ ] Add CORS configuration
- [ ] Enable HTTPS (development certificate)
- [ ] Implement mTLS for agent (certificate generation)
- [ ] Add SQL injection protection (already handled by JPA, but audit)
- [ ] Add XSS protection (CSP headers)

**Day 5: Documentation**

- [ ] Write API documentation (Swagger/OpenAPI)
- [ ] Write deployment guide
- [ ] Write user manual (student and admin)
- [ ] Create architecture diagrams (if not done)

**Day 6-7: Testing & Bug Fixes**

- [ ] End-to-end testing
- [ ] Fix identified bugs
- [ ] Performance optimization
- [ ] Code cleanup

**Deliverable**: Production-ready system

---

## 🗂️ Implementation Priority

### Must Have (MVP)
1. ✅ User authentication (JWT)
2. ✅ Device registration & heartbeat
3. ✅ Container create/delete
4. ✅ Browser terminal
5. ✅ Basic scheduler (first-available)
6. ✅ Resource limits enforcement
7. ✅ Admin device management

### Should Have
1. ✅ Lab reservation system
2. ✅ Quota management
3. ✅ File upload/download
4. ✅ Monitoring dashboard
5. ✅ Audit logging
6. ✅ Multiple container images

### Nice to Have
1. ✅ Adaptive scheduler (research contribution)
2. ✅ Prometheus/Grafana integration
3. ✅ Advanced failure recovery
4. ✅ Performance benchmarking
5. ⏳ GPU support (future)
6. ⏳ Multi-campus federation (future)

---

## 🧪 Testing Strategy

### Unit Tests
- Backend services (JUnit + Mockito)
- Scheduler algorithm
- Quota validation
- Docker manager (agent)

### Integration Tests
- Database operations (Testcontainers)
- API endpoints (MockMvc)
- WebSocket connections
- Docker API calls

### End-to-End Tests
- Complete container lifecycle
- Terminal session
- File operations
- Device registration

### Performance Tests
- JMeter or Gatling
- Concurrent container creation
- Terminal latency measurement
- Database query performance

---

## 🚀 Deployment Checklist

### Development Environment
- [ ] Local PostgreSQL running
- [ ] Local Redis running
- [ ] Backend running (port 8080)
- [ ] Frontend running (port 5173)
- [ ] Agent running locally with Docker

### Production Environment
- [ ] VPS/Server for broker (2 vCPU, 4 GB RAM minimum)
- [ ] PostgreSQL database (managed or self-hosted)
- [ ] Redis cache
- [ ] Nginx configured with SSL certificate
- [ ] Agents installed on lab PCs
- [ ] Monitoring setup (Prometheus + Grafana)

---

## 📈 Success Metrics

### Technical Metrics
- Container startup time: < 10 seconds (P95)
- Terminal latency: < 100ms (P95)
- API response time: < 500ms (P95)
- System uptime: 99%+

### Business Metrics
- Lab utilization: 18% → 60-70%
- Active users per day
- Containers created per week
- Resource distribution fairness

### Research Metrics
- Scheduling algorithm effectiveness
- Reservation conflict avoidance rate
- Comparison with baseline scheduler

---

## 🎯 Next Steps

1. **Create project repositories** (backend, frontend, agent)
2. **Setup development environment** (PostgreSQL, Redis, Docker)
3. **Start with Phase 1, Week 1** (Backend authentication)
4. **Follow the week-by-week plan**
5. **Test continuously** (don't wait until the end)
6. **Document as you go** (API docs, architecture decisions)
7. **Commit frequently** (atomic commits with good messages)

---

## 📞 Team Coordination

### Weekly Meetings
- **Monday**: Week planning, task assignment
- **Thursday**: Mid-week sync, blockers discussion
- **Saturday**: Demo, code review, retrospective

### Communication Channels
- **GitHub Issues**: Feature requests, bugs
- **Pull Requests**: Code review
- **Discord/Slack**: Quick questions, daily standup
- **Documentation**: Shared Google Docs/Notion

### Code Review Process
1. Create feature branch
2. Implement and test locally
3. Create pull request with description
4. At least one team member reviews
5. Address feedback
6. Merge to main

---

**Ready to start building! 🚀**

Follow this plan step-by-step, and you'll have a complete, production-ready system in 12 weeks.
