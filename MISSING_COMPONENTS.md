# CampusCompute - Missing Components Analysis

## 📊 Implementation Status

### ✅ COMPLETED Components

#### Backend (Spring Boot)
- ✅ **Entities**: User, Device, Container, Reservation
- ✅ **Repositories**: UserRepository, DeviceRepository, ContainerRepository, ReservationRepository
- ✅ **Services**: UserService, DeviceService, ContainerService
- ✅ **Controllers**: AuthController, DeviceController, ContainerController, HealthController
- ✅ **Security**: JWT Authentication (JwtUtil, JwtAuthenticationFilter, CustomUserDetailsService, SecurityConfig)
- ✅ **DTOs**: LoginRequest, LoginResponse, RegisterRequest, ContainerRequest, ApiResponse
- ✅ **Database**: PostgreSQL connected, schema auto-created by Hibernate
- ✅ **Configuration**: application.yml with database, Redis, JWT config

#### Agent (Python)
- ✅ **Basic Structure**: config.py, agent.py, docker_manager.py, main.py, system_monitor.py
- ✅ **Configuration**: config.yaml, requirements.txt

---

## ❌ MISSING Components (Critical for MVP)

### 🔴 **HIGH PRIORITY - Backend**

#### 1. **WebSocket Communication**
**Status**: websocket package is empty (only .gitkeep)

**Missing Files**:
- `WebSocketConfig.java` - Spring WebSocket configuration
- `AgentWebSocketHandler.java` - Handle agent connections
- `TerminalWebSocketHandler.java` - Handle terminal sessions
- `WebSocketSessionManager.java` - Manage active connections
- `AgentMessage.java` (DTO) - Message format between broker and agents

**Why Critical**: Without this, broker cannot communicate with agents. No container creation, no heartbeats.

**Example**:
```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(agentWebSocketHandler(), "/ws/agent")
                .setAllowedOrigins("*");
    }
}
```

---

#### 2. **Lab Entity and Management**
**Status**: Missing entirely

**Missing Files**:
- `Lab.java` (entity)
- `LabRepository.java`
- `LabService.java`
- `LabController.java` (admin endpoints)

**Why Critical**: Reservations reference labs. Device-to-lab mapping is core to scheduler.

**Database Schema**:
```sql
CREATE TABLE labs (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    building VARCHAR(100),
    room VARCHAR(50),
    department VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

#### 3. **Scheduler Service**
**Status**: Missing entirely

**Missing Files**:
- `SchedulerService.java` - Device selection algorithm
- `SchedulingStrategy.java` (interface)
- `FirstAvailableStrategy.java` - Basic implementation
- `AdaptiveSchedulerStrategy.java` - Reservation-aware (research contribution)

**Why Critical**: This is THE core component. Without scheduler, containers can't be assigned to devices.

**Key Methods**:
```java
public class SchedulerService {
    Device selectBestDevice(ContainerRequest request);
    double calculateDeviceScore(Device device, ContainerRequest request);
    double calculateReservationProximityScore(Device device, Duration duration);
}
```

---

#### 4. **Exception Handling**
**Status**: Missing entirely

**Missing Files**:
- `GlobalExceptionHandler.java` - @ControllerAdvice
- Custom exceptions:
  - `ResourceNotFoundException.java`
  - `QuotaExceededException.java`
  - `NoDeviceAvailableException.java`
  - `ContainerCreationException.java`
  - `AuthenticationException.java`

**Why Critical**: Proper error handling and user-friendly error messages.

---

#### 5. **Quota Entity and Enforcement**
**Status**: Entity might exist in User, but no separate service

**Missing Files**:
- `QuotaService.java` - Validation and enforcement logic
- Admin endpoints in `QuotaController.java` to set user quotas

**Why Critical**: Prevents resource abuse. Must check before container creation.

---

#### 6. **Reservation Service and Controller**
**Status**: Entity exists, but no service/controller

**Missing Files**:
- `ReservationService.java` - Create, overlap detection
- `ReservationController.java` - Admin endpoints for managing reservations

**Why Critical**: Without this, scheduler cannot implement reservation-aware logic.

---

#### 7. **Monitoring and Statistics**
**Status**: Missing entirely

**Missing Files**:
- `MonitoringService.java` - Aggregate statistics
- `MonitoringController.java` - Endpoints for dashboard
- `ResourceUsage.java` (entity) - Store historical metrics

**Database Schema**:
```sql
CREATE TABLE resource_usage (
    id SERIAL PRIMARY KEY,
    container_id INTEGER REFERENCES containers(id),
    cpu_percent DECIMAL(5,2),
    memory_mb INTEGER,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

#### 8. **Audit Logging**
**Status**: Missing entirely

**Missing Files**:
- `AuditLog.java` (entity)
- `AuditLogRepository.java`
- `AuditService.java` - Log all admin actions
- `AuditController.java` - View audit logs

**Why Important**: Security compliance, track all admin actions.

---

#### 9. **CORS Configuration**
**Status**: Missing

**Missing Files**:
- `CorsConfig.java` - Allow frontend to access backend APIs

**Example**:
```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH");
    }
}
```

---

### 🔴 **HIGH PRIORITY - Agent**

#### 1. **WebSocket Connection to Broker**
**Status**: Not implemented

**Missing in agent.py**:
- WebSocket client connection
- Message handling (CREATE_CONTAINER, DELETE_CONTAINER, STOP_CONTAINER)
- Reconnection logic with exponential backoff
- Authentication (token or mTLS)

**Example**:
```python
import websocket

class BrokerConnection:
    def __init__(self, broker_url):
        self.ws = websocket.WebSocketApp(
            broker_url,
            on_message=self.on_message,
            on_error=self.on_error,
            on_close=self.on_close
        )
    
    def on_message(self, ws, message):
        data = json.loads(message)
        if data['type'] == 'CREATE_CONTAINER':
            self.handle_create_container(data)
```

---

#### 2. **Heartbeat Service**
**Status**: Basic monitoring exists, but no heartbeat sending

**Missing**:
- Periodic heartbeat transmission to broker (every 30 seconds)
- Include metrics: CPU, RAM, disk, container count
- Handle broker disconnection

**Example**:
```python
def send_heartbeat(self):
    while True:
        metrics = self.system_monitor.get_metrics()
        self.broker_connection.send({
            'type': 'HEARTBEAT',
            'deviceId': self.device_id,
            'timestamp': datetime.now().isoformat(),
            'resources': metrics
        })
        time.sleep(30)
```

---

#### 3. **Container Lifecycle in docker_manager.py**
**Status**: Partial implementation

**Missing Methods**:
- `create_container()` - Create with resource limits
- `start_container()`
- `stop_container()`
- `delete_container()`
- `get_container_logs()`
- Security policies (no privileged, cap drop)

---

#### 4. **Device Enrollment**
**Status**: Not implemented

**Missing**:
- Enrollment token validation with broker
- Device registration (send hostname, IP, specs to broker)
- Receive and store device ID from broker

---

#### 5. **Terminal Session Handler**
**Status**: Not implemented

**Missing Files**:
- `terminal.py` - Handle `docker exec` for terminal sessions
- Forward stdin/stdout between WebSocket and Docker
- Session cleanup

---

#### 6. **Storage/Volume Management**
**Status**: Not implemented

**Missing Files**:
- `storage.py` - Create Docker volumes
- Attach volumes to containers
- Volume lifecycle management

---

#### 7. **Installation Scripts**
**Status**: Not implemented

**Missing Files**:
- `install.sh` - Install dependencies, setup systemd service
- `systemd/campuscompute-agent.service` - Systemd service file

---

### 🟡 **MEDIUM PRIORITY - Backend**

#### 1. **File Upload/Download for Containers**
**Status**: Not implemented

**Missing**:
- `FileController.java` - Endpoints for file operations
- WebSocket or multipart upload
- `docker cp` integration via agent

**Endpoints**:
```
POST   /api/containers/:id/files/upload
GET    /api/containers/:id/files/download?path=/workspace/file.txt
GET    /api/containers/:id/files?path=/workspace
DELETE /api/containers/:id/files?path=/file.txt
```

---

#### 2. **Container Images Management**
**Status**: Hardcoded in requests

**Missing**:
- `ContainerImage.java` (entity) - Pre-approved images
- `ContainerImageRepository.java`
- Admin endpoints to manage allowed images
- Image pull tracking

**Why Important**: Security - students can only use approved images.

---

#### 3. **Department Entity**
**Status**: Referenced in User, but no entity

**Missing**:
- `Department.java` (entity)
- `DepartmentRepository.java`
- Admin management

---

#### 4. **Notification System**
**Status**: Not implemented

**Missing**:
- Email notifications (container created, expiring soon)
- WebSocket notifications to frontend
- `NotificationService.java`

---

#### 5. **API Documentation**
**Status**: Not configured

**Missing**:
- Swagger/OpenAPI configuration
- `@OpenAPIDefinition` annotations
- Auto-generated API docs

---

### 🟢 **LOW PRIORITY - Nice to Have**

#### 1. **GPU Support**
**Status**: Not planned yet

#### 2. **Multi-Campus Federation**
**Status**: Future work

#### 3. **Advanced Failure Recovery**
**Status**: Not implemented

#### 4. **Prometheus/Grafana Integration**
**Status**: Not implemented

#### 5. **Load Testing Scripts**
**Status**: Not implemented

---

## 🎯 **IMMEDIATE NEXT STEPS** (Priority Order)

### Week 1: Core Communication
1. ✅ **Implement Backend WebSocket** (AgentWebSocketHandler, WebSocketConfig)
2. ✅ **Implement Agent WebSocket Client** (connect to broker)
3. ✅ **Implement Heartbeat** (agent → broker every 30s)
4. ✅ **Test Connection** (agent connects, sends heartbeat, broker logs it)

### Week 2: Container Creation Flow
5. ✅ **Implement Lab Entity** (Lab.java, LabRepository, LabService)
6. ✅ **Implement SchedulerService** (basic first-available strategy)
7. ✅ **Complete docker_manager.py** (create, start, stop, delete)
8. ✅ **Integrate ContainerService** (request → schedule → send to agent → receive response)
9. ✅ **Test End-to-End** (POST /api/containers → agent creates Docker container)

### Week 3: Quota & Reservations
10. ✅ **Implement QuotaService** (validation before container creation)
11. ✅ **Implement ReservationService** (CRUD, overlap detection)
12. ✅ **Update Scheduler** (integrate reservation proximity scoring)

### Week 4: Terminal & Frontend
13. ✅ **Implement Terminal WebSocket** (broker as proxy)
14. ✅ **Implement Terminal in Agent** (docker exec, I/O forwarding)
15. ✅ **Start Frontend Development** (React setup, login page)

---

## 📋 **Missing Components Summary**

| Component | Status | Priority | Estimated Effort |
|-----------|--------|----------|------------------|
| WebSocket (Backend) | ❌ Missing | 🔴 Critical | 2-3 days |
| WebSocket (Agent) | ❌ Missing | 🔴 Critical | 2-3 days |
| Scheduler Service | ❌ Missing | 🔴 Critical | 3-4 days |
| Lab Entity & Service | ❌ Missing | 🔴 Critical | 1 day |
| Exception Handling | ❌ Missing | 🔴 Critical | 1 day |
| Quota Service | ❌ Missing | 🔴 Critical | 1-2 days |
| Reservation Service | ❌ Missing | 🔴 Critical | 1-2 days |
| Agent Heartbeat | ❌ Missing | 🔴 Critical | 1 day |
| Agent Container Mgmt | 🟡 Partial | 🔴 Critical | 2-3 days |
| Agent Device Enrollment | ❌ Missing | 🔴 Critical | 1 day |
| Terminal WebSocket | ❌ Missing | 🟡 Important | 3-4 days |
| Terminal Agent Handler | ❌ Missing | 🟡 Important | 2-3 days |
| Monitoring Service | ❌ Missing | 🟡 Important | 2-3 days |
| Audit Logging | ❌ Missing | 🟡 Important | 1-2 days |
| File Operations | ❌ Missing | 🟢 Optional | 2-3 days |
| Frontend | ❌ Missing | 🔴 Critical | 2-3 weeks |
| CORS Config | ❌ Missing | 🔴 Critical | 30 mins |
| API Documentation | ❌ Missing | 🟡 Important | 1 day |

**Total Critical Components Missing**: 11
**Total Estimated Time for MVP**: 6-8 weeks (with 1 developer)

---

## 🚀 **To Achieve MVP (Minimum Viable Product)**

**Must Complete**:
1. WebSocket communication (broker ↔ agent)
2. Scheduler service
3. Lab entity
4. Container creation end-to-end flow
5. Quota enforcement
6. Terminal proxy
7. Basic frontend (login, container list, terminal)

**After MVP, these unlock key features**:
- Reservation-aware scheduling (research contribution)
- File upload/download
- Monitoring dashboard
- Audit logging

---

## 📝 **Notes**

- Currently at ~30% completion for backend MVP
- Agent is at ~20% completion
- Frontend is at 0% completion
- Database schema is auto-generated by Hibernate (good for rapid prototyping)
- No tests written yet (unit, integration, e2e)
- No deployment configuration (Docker compose, Kubernetes, systemd)
- No CI/CD pipeline
