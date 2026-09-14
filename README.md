# CampusCompute

**Campus-Aware Cloud Resource Pooling System**

CampusCompute transforms underutilized campus lab computers into a shared cloud computing platform, providing students with on-demand access to isolated development environments while respecting institutional schedules and resource constraints.

---

## 🎯 Project Overview

### The Problem

College computer labs experience dramatic usage patterns:
- **During lab hours**: High utilization (80-100%)
- **Outside lab hours**: Very low utilization (5-20%)
- **Nights and weekends**: Nearly idle

This represents significant wasted computing capacity that students could leverage for coursework, projects, and research.

### The Solution

CampusCompute creates a three-layer architecture that:

1. **Aggregates** idle resources from campus lab computers
2. **Provisions** isolated Docker containers on-demand
3. **Schedules** workloads intelligently around academic schedules
4. **Delivers** browser-based terminal and file access

Students get cloud computing access. The college maximizes existing infrastructure investment.

---

## 🏗️ Architecture

```text
┌──────────────────────────────────────────────────────────┐
│                    STUDENT / ADMIN                       │
│                 React + TypeScript                       │
└──────────────────────┬───────────────────────────────────┘
                       │ HTTPS / WebSocket
                       ▼
┌──────────────────────────────────────────────────────────┐
│                 CAMPUS CLOUD BROKER                      │
│                    Spring Boot                           │
│                                                          │
│ Auth │ Scheduler │ Resource Manager │ Container Manager │
│ Quota │ Storage │ Reservation │ Monitoring │ Audit      │
└──────────────────────┬───────────────────────────────────┘
                       │ Secure Agent Channel
                       │ mTLS / WebSocket
        ┌──────────────┼──────────────┐
        ▼              ▼              ▼
   ┌─────────┐    ┌─────────┐    ┌─────────┐
   │ Lab PC  │    │ Lab PC  │    │ Lab PC  │
   │ Agent   │    │ Agent   │    │ Agent   │
   │ Docker  │    │ Docker  │    │ Docker  │
   └────┬────┘    └────┬────┘    └────┬────┘
        │              │              │
     C1 C2 C3       C1 C2          C1 C2 C3
```

### Key Components

| Component | Technology | Purpose |
|-----------|-----------|---------|
| **Frontend** | React + TypeScript + Vite | Student/admin web interface |
| **Broker** | Spring Boot + PostgreSQL | Central orchestration and scheduling |
| **Agent** | Python + Docker SDK | Resource bridge on each lab PC |
| **Runtime** | Docker Engine | Container isolation and execution |
| **Monitoring** | Prometheus + Grafana | Resource tracking and analytics |

---

## 🚀 Core Features

### For Students

- **On-Demand Environments**: Request isolated computing resources from any device
- **Browser Terminal**: Full terminal access via xterm.js (no SSH client needed)
- **Persistent Storage**: Personal workspace that survives container restarts
- **Multiple Images**: Choose from Ubuntu, Python, Node.js, Java, GCC environments
- **Resource Control**: Start, stop, delete containers through intuitive UI

### For Administrators

- **Device Management**: Register, enable, disable, and drain lab computers
- **Lab Reservations**: Block resource allocation during scheduled classes
- **Quota Management**: Set CPU, RAM, storage, and time limits per user/role
- **Real-Time Monitoring**: View campus-wide resource utilization
- **Audit Logging**: Track all system actions for security and compliance

### For the Institution

- **Maximized ROI**: Convert idle computers into productive resources
- **Academic-Aware**: Scheduler respects class schedules and lab reservations
- **Security**: Isolated containers prevent interference between users
- **Fair Access**: Quota system ensures equitable resource distribution

---

## 🎓 Novel Contributions

### 1. **Adaptive Campus Scheduler**

Traditional cloud schedulers optimize for:
- CPU availability
- Memory availability
- Network proximity

CampusCompute's scheduler adds **institutional context**:

```text
Score = 
    CPU availability
  + RAM availability
  + host load
  + reservation proximity      ← Avoids labs about to be used
  + network quality
  + reliability history
```

**Example**: At 9:40 AM, avoid scheduling 6-hour containers in a lab with class at 10:00 AM.

### 2. **Privacy-Preserving Resource Sharing**

Lab PC owners (departments) see only:
- Aggregate CPU/RAM utilization
- Number of active containers
- Resource allocation amounts

They do **not** see:
- Student identities
- Container contents
- Terminal activity
- File contents

### 3. **Hierarchical Resource Control**

```text
College
  └── Department
       └── Lab
            └── Device
                 └── Container
                      └── Student Workspace
```

Clear separation of control planes at each level.

---

## 🛠️ Technology Stack

### Frontend
- **Framework**: React 18 + TypeScript
- **Build Tool**: Vite
- **Styling**: Tailwind CSS
- **Routing**: React Router
- **API Client**: TanStack Query + Axios
- **Terminal**: xterm.js
- **State Management**: Zustand (optional)

### Backend
- **Framework**: Spring Boot 3.2
- **Language**: Java 17+
- **Security**: Spring Security + JWT
- **Database**: PostgreSQL 15
- **ORM**: Spring Data JPA + Hibernate
- **Caching**: Redis
- **WebSocket**: Spring WebSocket (STOMP)
- **API Docs**: SpringDoc OpenAPI

### Agent
- **Language**: Python 3.11+
- **Container API**: Docker SDK for Python
- **System Monitoring**: psutil
- **WebSocket**: websocket-client
- **Security**: cryptography (mTLS)

### Infrastructure
- **Containerization**: Docker Engine 24+
- **Reverse Proxy**: Nginx
- **Monitoring**: Prometheus + Grafana
- **Logging**: ELK Stack (optional)

---

## 📂 Project Structure

```text
campuscompute/
│
├── frontend/                 # React application
│   ├── src/
│   │   ├── components/       # Reusable UI components
│   │   ├── pages/            # Student and admin pages
│   │   ├── services/         # API integration
│   │   ├── hooks/            # Custom React hooks
│   │   ├── utils/            # Utilities
│   │   └── App.tsx
│   ├── package.json
│   └── vite.config.ts
│
├── backend/                  # Spring Boot application
│   ├── src/main/java/
│   │   └── edu/campus/compute/
│   │       ├── auth/         # Authentication & authorization
│   │       ├── users/        # User management
│   │       ├── labs/         # Lab management
│   │       ├── devices/      # Device registration & monitoring
│   │       ├── scheduler/    # Resource scheduling logic
│   │       ├── containers/   # Container lifecycle
│   │       ├── storage/      # Persistent volume management
│   │       ├── reservations/ # Lab reservation system
│   │       ├── quotas/       # Quota enforcement
│   │       ├── monitoring/   # Metrics collection
│   │       ├── websocket/    # WebSocket handling
│   │       └── audit/        # Audit logging
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── schema.sql
│   └── pom.xml
│
├── agent/                    # Python agent for lab PCs
│   ├── main.py               # Entry point
│   ├── config.py             # Configuration
│   ├── authentication.py     # mTLS and enrollment
│   ├── connection.py         # WebSocket connection
│   ├── heartbeat.py          # Periodic status updates
│   ├── monitor.py            # Resource monitoring
│   ├── docker_manager.py     # Docker API wrapper
│   ├── container_policy.py   # Security enforcement
│   ├── terminal.py           # Terminal session handling
│   ├── storage.py            # Volume management
│   └── requirements.txt
│
├── infrastructure/           # Deployment configurations
│   ├── docker-compose.yml    # Local development stack
│   ├── nginx/                # Nginx configuration
│   ├── prometheus/           # Monitoring config
│   └── grafana/              # Dashboard definitions
│
├── docs/                     # Documentation
│   ├── api/                  # API documentation
│   ├── architecture/         # Architecture diagrams
│   ├── deployment/           # Deployment guides
│   └── security/             # Security policies
│
└── README.md
```

---

## 🔒 Security Model

### Isolation Layers

1. **Authentication**: JWT-based user authentication
2. **Authorization**: Role-based access control (RBAC)
3. **Ownership Verification**: Every container action validates user ownership
4. **Container Isolation**: Docker resource limits and capability restrictions
5. **Network Security**: mTLS for agent-broker communication
6. **Audit Logging**: All administrative actions logged

### Container Security

Each student container enforces:

```yaml
security_opt:
  - no-new-privileges:true
cap_drop:
  - ALL
cap_add:
  - CHOWN
  - SETGID
  - SETUID
resources:
  cpus: "2.0"
  memory: "4096M"
  pids: 512
storage_opt:
  size: "20G"
```

**Never allowed**:
- `--privileged` flag
- Docker socket mounting (`/var/run/docker.sock`)
- Host filesystem access
- Arbitrary capability additions

### Agent Security

- **Mutual TLS**: Both broker and agent authenticate each other
- **Command Validation**: Agent only accepts predefined command types
- **No Arbitrary Execution**: Agent never runs shell commands from network messages
- **Enrollment Process**: Devices require admin-generated enrollment tokens
- **Certificate-Based Auth**: Persistent credentials via device certificates

---

## 📊 Database Schema (Core Entities)

```sql
-- Users and roles
users (id, email, password_hash, role, department_id)
departments (id, name, quota_profile)

-- Physical resources
labs (id, name, department_id, building, room)
devices (id, lab_id, hostname, status, enrollment_token_hash)
device_resources (device_id, cpu_cores, ram_mb, storage_gb)

-- Virtual resources
containers (id, user_id, device_id, name, image, status, created_at, expires_at)
volumes (id, container_id, mount_path, size_gb)
resource_allocations (id, container_id, cpu, memory, storage)

-- Scheduling
reservations (id, lab_id, start_time, end_time, reason)
quotas (id, user_id, max_cpu, max_memory, max_storage, max_containers)

-- Monitoring
heartbeats (device_id, timestamp, cpu_usage, ram_usage, docker_healthy)
resource_usage (container_id, timestamp, cpu_percent, memory_mb)
audit_logs (id, user_id, action, resource_type, resource_id, timestamp)
```

---

## 🚦 Getting Started

### Prerequisites

- **Node.js** 18+ (for frontend)
- **Java** 17+ (for backend)
- **Python** 3.11+ (for agent)
- **Docker Engine** 24+ (for agent host machines)
- **PostgreSQL** 15+ (for broker database)
- **Redis** 7+ (for caching/queuing)

### Local Development Setup

#### 1. Clone Repository

```bash
git clone https://github.com/your-org/campuscompute.git
cd campuscompute
```

#### 2. Start Infrastructure

```bash
cd infrastructure
docker-compose up -d postgres redis nginx
```

#### 3. Run Backend

```bash
cd backend
./mvnw spring-boot:run
```

Backend runs at: `http://localhost:8080`

#### 4. Run Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs at: `http://localhost:5173`

#### 5. Run Agent (on a lab PC or local Docker host)

```bash
cd agent
pip install -r requirements.txt
python main.py --config config.yaml
```

### First-Time Configuration

1. **Create Admin User**: Run database seed script
2. **Generate Enrollment Token**: Use admin API or UI
3. **Install Agent**: On a lab PC with Docker installed
4. **Verify Device Registration**: Check admin dashboard
5. **Create Test Container**: Use student portal

---

## 📈 Development Milestones

### ✅ Milestone 1: Authentication (Week 1-2)
- [ ] User registration and login
- [ ] JWT token generation and validation
- [ ] Role-based access control
- [ ] Frontend authentication integration

### ✅ Milestone 2: Device Management (Week 2-3)
- [ ] Device enrollment API
- [ ] Agent installation script
- [ ] Heartbeat mechanism
- [ ] Device status monitoring

### ✅ Milestone 3: Container Provisioning (Week 3-5)
- [ ] Container creation API
- [ ] Docker integration in agent
- [ ] Resource limit enforcement
- [ ] Container lifecycle management

### ✅ Milestone 4: Basic Scheduling (Week 5-6)
- [ ] Simple resource matching
- [ ] Quota validation
- [ ] Resource reservation logic
- [ ] End-to-end container request flow

### ✅ Milestone 5: Browser Terminal (Week 6-7)
- [ ] WebSocket terminal proxy
- [ ] xterm.js integration
- [ ] Docker exec session management
- [ ] Terminal security validation

### ✅ Milestone 6: Storage & Monitoring (Week 7-8)
- [ ] Persistent Docker volumes
- [ ] File upload/download API
- [ ] Resource usage metrics
- [ ] Admin monitoring dashboard

### ✅ Milestone 7: Advanced Features (Week 8-9)
- [ ] Lab reservation system
- [ ] Advanced quota management
- [ ] Device drain functionality
- [ ] Audit logging

### ✅ Milestone 8: Adaptive Scheduler (Week 9-10)
- [ ] Reservation-aware scheduling
- [ ] Host load monitoring
- [ ] Dynamic capacity adjustment
- [ ] Performance benchmarking

---

## 🔬 Research & Evaluation

### Metrics to Collect

**Resource Utilization**:
- Average CPU utilization (per device, campus-wide)
- Average RAM utilization
- Storage efficiency
- Idle time reduction

**Performance**:
- Container startup time (mean, p95, p99)
- Scheduling decision time
- Terminal latency
- API response times

**Fairness**:
- Resource distribution across users
- Wait times per department
- Quota adherence

**Reliability**:
- Container success rate
- Device uptime
- Failed allocation rate
- Automatic recovery success

### Experimental Scenarios

1. **Baseline**: Measure lab utilization without CampusCompute
2. **Simple Allocation**: First-available scheduling
3. **Adaptive Scheduling**: Academic-aware scheduling with reservations
4. **Peak Load**: Stress test with concurrent requests
5. **Failure Recovery**: Agent disconnection and reconnection

### Expected Outcomes

| Scenario | Lab Utilization | Student Satisfaction |
|----------|----------------|---------------------|
| Without System | 18% | Baseline |
| Basic Allocation | 52% | +40% |
| Adaptive Scheduler | 68% | +65% |

---

## 👥 Team Responsibilities

### Member 1: Core Backend
- Spring Boot application structure
- PostgreSQL schema and migrations
- Authentication & authorization
- REST API development
- Docker Compose setup

### Member 2: Scheduling & Intelligence
- Resource manager implementation
- Scheduling algorithm
- Quota enforcement
- Reservation system
- Adaptive scheduling logic

### Member 3: Agent & Infrastructure
- Python agent development
- Docker API integration
- Container security policies
- Network configuration
- Monitoring setup (Prometheus/Grafana)

### Member 4: Full-Stack & UI (You)
- React application architecture
- Student portal (dashboard, containers, terminal)
- Admin portal (devices, labs, reservations, users)
- WebSocket integration (terminal)
- File manager UI
- Frontend authentication flow

**Note**: All members should understand the complete system architecture.

---

## 🎯 What to Build (Priority Order)

### Phase 1: Minimum Viable Product (MVP)

**Must Have**:
- ✅ User authentication (login/logout)
- ✅ Device registration and heartbeat
- ✅ Basic scheduler (first-available)
- ✅ Container creation, start, stop, delete
- ✅ Resource limits (CPU, RAM)
- ✅ Browser terminal
- ✅ Persistent storage (Docker volumes)
- ✅ Basic admin dashboard

### Phase 2: Production Features

**Should Have**:
- ✅ Lab reservation system
- ✅ Quota management
- ✅ Device drain functionality
- ✅ Resource usage monitoring
- ✅ Audit logging
- ✅ File upload/download
- ✅ Multiple container images

### Phase 3: Research Contribution

**Nice to Have**:
- ✅ Adaptive campus scheduler
- ✅ Prometheus/Grafana integration
- ✅ Advanced failure handling
- ✅ Performance benchmarking
- ✅ Security hardening
- ✅ Auto-scaling policies

### What NOT to Add

**Avoid Scope Creep**:
- ❌ Kubernetes orchestration (Docker is sufficient)
- ❌ Blockchain/cryptocurrency features
- ❌ Public marketplace
- ❌ Full VM orchestration
- ❌ Microservices architecture (Spring Boot monolith is fine)
- ❌ Public Internet provider onboarding
- ❌ AI/ML everywhere without purpose

---

## 🔄 Complete Request Flow

### Student Creates Environment

```text
1. Student logs in → JWT issued
2. Student requests: 2 CPU / 4 GB RAM / Ubuntu image
3. Frontend sends: POST /api/containers
4. Backend authenticates JWT
5. Backend validates quota (max 4 CPU allowed)
6. Backend checks reservations (no conflicts)
7. Scheduler searches available devices
8. Device PC-23 selected (best score)
9. PostgreSQL transaction: reserve resources
10. Backend sends CREATE_CONTAINER command to Agent-23
11. Agent validates command structure
12. Agent calls Docker API
13. Docker creates volume: student-123-workspace
14. Docker creates container with resource limits
15. Docker starts container
16. Agent reports success to backend
17. Backend updates database: status=RUNNING
18. Frontend receives: container ID, status, endpoint
19. Student clicks "Open Terminal"
20. Frontend establishes WebSocket: wss://broker/terminal/123
21. Backend validates: JWT user owns container 123
22. Backend forwards to Agent-23
23. Agent executes: docker exec -it container-123 /bin/bash
24. Terminal session starts
25. Student sees: bash prompt in browser
```

**Total time**: ~3-8 seconds (target)

---

## 📚 API Overview

### Authentication
```
POST   /api/auth/login
POST   /api/auth/logout
POST   /api/auth/refresh
GET    /api/auth/me
```

### Containers (Student)
```
GET    /api/containers
POST   /api/containers
GET    /api/containers/:id
PATCH  /api/containers/:id/start
PATCH  /api/containers/:id/stop
DELETE /api/containers/:id
GET    /api/containers/:id/stats
WS     /ws/terminal/:id
```

### Files
```
GET    /api/containers/:id/files?path=/workspace
POST   /api/containers/:id/files/upload
GET    /api/containers/:id/files/download?path=file.txt
DELETE /api/containers/:id/files?path=file.txt
```

### Devices (Admin)
```
GET    /api/admin/devices
POST   /api/admin/devices/enroll
PATCH  /api/admin/devices/:id/enable
PATCH  /api/admin/devices/:id/disable
PATCH  /api/admin/devices/:id/drain
GET    /api/admin/devices/:id/stats
```

### Reservations (Admin)
```
GET    /api/admin/reservations
POST   /api/admin/reservations
DELETE /api/admin/reservations/:id
```

### Agent WebSocket (mTLS)
```
WS     /ws/agent/:deviceId
Messages:
  - HEARTBEAT
  - CREATE_CONTAINER
  - START_CONTAINER
  - STOP_CONTAINER
  - DELETE_CONTAINER
  - EXEC_TERMINAL
```

---

## 🐛 Testing Strategy

### Unit Tests
- Service layer logic (Spring Boot)
- Scheduler algorithm
- Quota validation
- Docker manager (agent)

### Integration Tests
- Database operations
- API endpoints
- WebSocket connections
- Docker API calls

### End-to-End Tests
- Complete container lifecycle
- Terminal session flow
- File operations
- Device registration

### Security Tests
- Ownership validation
- JWT expiration
- Container escape attempts
- Resource limit enforcement

### Performance Tests
- Concurrent container requests
- Scheduling under load
- Terminal latency
- Database query optimization

---

## 📖 Documentation

### For Users
- **Student Guide**: How to request and use containers
- **Terminal Guide**: Using the browser terminal
- **File Manager Guide**: Upload/download files

### For Administrators
- **Installation Guide**: Setting up the central broker
- **Agent Deployment**: Installing agents on lab PCs
- **Device Management**: Registering and managing devices
- **Reservation Guide**: Creating lab reservations
- **Monitoring Guide**: Using Prometheus/Grafana

### For Developers
- **API Documentation**: Complete REST API reference
- **Architecture Guide**: System design and data flow
- **Database Schema**: Entity relationships
- **Security Model**: Authentication and authorization
- **Contributing Guide**: Code standards and PR process

---

## 🚀 Deployment

### Development Environment
```bash
docker-compose up -d
```

### Production Deployment

**Central Server**:
1. Nginx (reverse proxy, TLS termination)
2. Spring Boot (systemd service)
3. PostgreSQL (managed service or container)
4. Redis (managed service or container)

**Lab PCs**:
1. Docker Engine installed
2. CampusCompute Agent (systemd service)
3. mTLS certificates configured
4. Network connectivity to central server

### Monitoring

**Prometheus Metrics**:
- Container count per state
- Resource utilization per device
- API response times
- Scheduling decision times
- Failed allocation rate

**Grafana Dashboards**:
- Campus Resource Overview
- Device Health Status
- Student Activity
- Scheduling Performance

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit your changes: `git commit -m 'Add amazing feature'`
4. Push to the branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

### Code Standards
- **Java**: Google Java Style Guide
- **TypeScript**: ESLint + Prettier
- **Python**: PEP 8 + Black formatter
- **Commits**: Conventional Commits

---

## 📄 License

This project is developed as an academic research project at [Your College Name].

For commercial use or deployment in production environments, please contact the authors.

---

## 🙏 Acknowledgments

- Docker for container runtime
- Spring Boot community
- React ecosystem
- xterm.js for terminal emulation
- [Your College Name] Computer Science Department

---

## 📧 Contact

**Project Team**:
- Member 1 (Backend): email@college.edu
- Member 2 (Scheduling): email@college.edu  
- Member 3 (Infrastructure): email@college.edu
- Member 4 (Frontend): email@college.edu

**Supervisor**: Prof. Name (supervisor@college.edu)

---

## 🗺️ Roadmap

### Q3 2026
- ✅ MVP with basic provisioning
- ✅ Browser terminal
- ✅ Authentication

### Q4 2026
- ✅ Lab reservations
- ✅ Adaptive scheduler
- ✅ Monitoring integration
- ✅ Security hardening

### Q1 2027
- 🔄 Performance optimization
- 🔄 Advanced failure recovery
- 🔄 Multi-campus support
- 🔄 Research paper publication

---

**Built with ❤️ for maximizing campus computing resources**
