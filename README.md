# CampusCompute - Campus-Aware Cloud Resource Pooling System

**Institution**: UPES Dehradun  
**Academic Year**: 2026-27  
**Project Type**: B.Tech Major Project  

---

## 🎯 Project Overview

CampusCompute transforms underutilized college lab computers into a shared cloud platform, enabling students to access containerized environments on-demand. The system features an **adaptive scheduler with reservation-aware scoring** to avoid scheduling conflicts with lab classes.

### Novel Research Contribution
**Adaptive Scheduling Algorithm** with multi-factor scoring that considers:
- CPU & RAM availability (60%)
- System load (20%)
- **Reservation proximity** (15%) - Avoids PCs with upcoming classes
- Reliability score (5%)

---

## 🚀 Current Status

### ✅ Completed Features (MVP)

1. **Authentication & Authorization**
   - JWT-based authentication
   - User registration and login
   - Secure password hashing (BCrypt)

2. **Container Lifecycle Management**
   - Create containers with resource limits
   - Stop/Start containers
   - Delete containers with cleanup
   - Real-time status tracking

3. **Quota Management**
   - Per-user resource quotas
   - CPU, RAM, container count limits
   - Quota validation before allocation

4. **Intelligent Scheduling**
   - Adaptive scheduler with reservation awareness
   - Multi-factor device selection
   - Resource availability checking
   - First-fit fallback strategy

5. **Lab & Reservation Management**
   - Lab creation and device assignment
   - Class schedule reservations
   - Conflict detection
   - Reservation proximity scoring

6. **WebSocket Communication**
   - Real-time agent-broker communication
   - Container lifecycle events
   - System metrics streaming
   - **Interactive terminal access** (NEW)

7. **Docker Integration**
   - Automatic container creation
   - Resource limit enforcement
   - Image management
   - Container monitoring

8. **Terminal Access** (NEW)
   - WebSocket-based interactive shell
   - Real-time bidirectional I/O
   - PTY session management
   - Multi-session support

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Frontend (Planned)                    │
│              React Dashboard + Terminal UI               │
└────────────────────┬────────────────────────────────────┘
                     │ HTTPS + WebSocket
┌────────────────────┴────────────────────────────────────┐
│                  Backend (Spring Boot)                   │
│  ┌──────────────┬──────────────┬─────────────────────┐  │
│  │  Auth/JWT    │   Scheduler  │  Container Service  │  │
│  │  Quota Mgmt  │   Websocket  │  Terminal Handler   │  │
│  └──────────────┴──────────────┴─────────────────────┘  │
└────────────────────┬──────────┬─────────────────────────┘
                     │          │
         ┌───────────┴─┐    ┌───┴──────────┐
         │ PostgreSQL  │    │    Redis     │
         │  (Database) │    │   (Cache)    │
         └─────────────┘    └──────────────┘
                     │
              WebSocket (Agent)
                     │
    ┌────────────────┴────────────────────────┐
    │         Python Agent (Lab PCs)          │
    │  ┌──────────┬──────────┬─────────────┐  │
    │  │  Docker  │  System  │  Terminal   │  │
    │  │ Manager  │ Monitor  │  Manager    │  │
    │  └──────────┴──────────┴─────────────┘  │
    └────────────────┬────────────────────────┘
                     │
              ┌──────┴──────┐
              │   Docker    │
              │  Containers │
              └─────────────┘
```

---

## 📦 Tech Stack

### Backend
- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: PostgreSQL 15
- **Cache**: Redis 7
- **WebSocket**: Spring WebSocket
- **Security**: Spring Security + JWT
- **Build Tool**: Maven

### Agent
- **Language**: Python 3.11+
- **Docker SDK**: docker-py
- **WebSocket**: websockets library
- **Async**: asyncio
- **System Monitoring**: psutil

### Frontend (Planned)
- **Framework**: React 18 + Vite
- **UI Library**: Material-UI / Ant Design
- **State Management**: Redux Toolkit
- **Terminal**: xterm.js
- **Charts**: Recharts
- **HTTP**: Axios

### Infrastructure
- **Containerization**: Docker
- **Database**: PostgreSQL 15
- **Cache**: Redis 7
- **Orchestration**: Docker Compose

---

## 🎓 Next Phase: Multi-Organization Platform

### Status: ✅ Phase 1 Complete - Backend Foundation Ready

**What's Implemented** (October 2026):

#### Backend Changes:
- ✅ **Organization Entity**: Complete multi-tenant data model
- ✅ **Updated Entities**: User, Device, Lab with organization relationships
- ✅ **Organization Service**: Registration, enrollment tokens, student bulk upload
- ✅ **Organization Controller**: 7 new endpoints for org management
- ✅ **JWT Updates**: Token includes organizationId and userType
- ✅ **Security Filter**: Organization context automatically set in requests
- ✅ **Repositories**: Organization-aware queries for all entities
- ✅ **First-Time Login**: Student password setup flow
- ✅ **Agent Install Script**: Auto-generated with enrollment token

#### New API Endpoints:
- `POST /api/organizations/register` - Register organization + admin
- `GET /api/organizations/me` - Get organization details
- `PUT /api/organizations/me` - Update organization (ORG_ADMIN)
- `POST /api/organizations/devices/token` - Generate enrollment token
- `POST /api/organizations/students/upload` - Bulk upload students
- `POST /api/auth/first-login` - Student first-time password setup
- `GET /api/organizations` - List all orgs (ROOT only)
- `GET /api/organizations/{id}` - Get org by ID (ROOT only)

#### Data Isolation:
- All devices scoped to organization
- All labs scoped to organization
- All containers scoped via user's organization
- Students can only see their own resources
- Org admins can only manage their org's resources

See [MULTI_ORG_IMPLEMENTATION_PHASE1.md](MULTI_ORG_IMPLEMENTATION_PHASE1.md) for complete details.

---

### Coming Soon (Phase 2-4):

#### Phase 2: Agent Updates
- Enrollment token authentication
- Organization context in device registration
- WebSocket message updates

#### Phase 3: Statistics & Monitoring
- Organization dashboard stats
- Resource usage tracking per org
- Student activity logs
- Admin analytics endpoints

#### Phase 4: Frontend Application
- React + Vite setup
- Landing page + org registration
- Admin dashboard (devices, students, stats)
- Student dashboard (containers, terminal)
- Material-UI/Ant Design components

---

### Platform Features (Multi-Org)

CampusCompute as a multi-tenant SaaS platform where multiple educational institutions can:
- Self-register their organization
- Add their lab machines by installing agents
- Manage student accounts (bulk CSV upload)
- Set organization-level quotas and policies
- Monitor usage and generate reports

### Planned User Roles

1. **Organization Admin**
   - Register organization
   - Add/manage lab machines
   - Upload student lists
   - Configure quotas and policies
   - View analytics

2. **Student**
   - Login with organization credentials
   - Create/manage containers
   - Access terminal
   - View usage statistics

3. **Super Admin** (Future)
   - Platform-wide administration
   - Monitor all organizations
   - Manage subscriptions

### Database Changes
- Add `organizations` table
- Update `users` table with `organization_id` and `user_type`
- Update `devices` and `labs` with `organization_id`
- Add `student_uploads` table for bulk registration

See [MULTI_ORG_ARCHITECTURE.md](MULTI_ORG_ARCHITECTURE.md) for complete design.

---

## 📁 Project Structure

```
CampusCompute/
├── backend/                    # Spring Boot application
│   ├── src/main/java/com/campuscompute/
│   │   ├── config/            # Configuration classes
│   │   ├── controller/        # REST controllers
│   │   ├── dto/               # Data transfer objects
│   │   ├── entity/            # JPA entities
│   │   ├── exception/         # Custom exceptions
│   │   ├── repository/        # Data repositories
│   │   ├── scheduler/         # Scheduling strategies
│   │   ├── security/          # JWT & security
│   │   ├── service/           # Business logic
│   │   └── websocket/         # WebSocket handlers
│   └── pom.xml                # Maven dependencies
│
├── agent/                      # Python agent
│   ├── src/
│   │   ├── agent.py           # Main agent logic
│   │   ├── config.py          # Configuration
│   │   ├── docker_manager.py  # Docker operations
│   │   ├── system_monitor.py  # System metrics
│   │   ├── websocket_client.py# WebSocket client
│   │   └── terminal_manager.py# Terminal sessions (NEW)
│   ├── config.yaml            # Agent configuration
│   └── requirements.txt       # Python dependencies
│
├── docker-compose.yml          # Infrastructure setup
├── MULTI_ORG_ARCHITECTURE.md   # Multi-tenant design
└── README.md                   # This file
```

---

## 🚦 Getting Started

### Prerequisites
- Java 17+
- Python 3.11+
- Docker Desktop
- PostgreSQL 15
- Redis 7
- Maven 3.8+

### 1. Start Infrastructure

```bash
docker-compose up -d
```

This starts PostgreSQL and Redis.

### 2. Start Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Backend runs on http://localhost:8081

### 3. Configure Agent

Edit `agent/config.yaml`:
```yaml
broker:
  url: "ws://localhost:8081/ws/agent"

device:
  id: 1  # Must match database device ID
  device_id: "LAB-PC-001"
  
docker:
  socket: "npipe:////./pipe/docker_engine"  # Windows
  # socket: "unix:///var/run/docker.sock"  # Linux/Mac
```

### 4. Start Agent

```bash
cd agent
pip install -r requirements.txt
python src/main.py
```

### 5. Test API

```bash
# Register user
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "student1",
    "email": "student1@example.com",
    "password": "password123",
    "fullName": "Test Student"
  }'

# Login
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "student1",
    "password": "password123"
  }'

# Create container
curl -X POST http://localhost:8081/api/containers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "image": "alpine:latest",
    "cpuCores": 1,
    "ramBytes": 1073741824,
    "diskBytes": 5368709120,
    "lifetimeMs": 14400000
  }'
```

---

## 📊 Testing Results

### Container Lifecycle
- ✅ Create: 4 seconds (including image pull)
- ✅ Stop: < 10 seconds with resource release
- ✅ Delete: < 1 second with Docker cleanup
- ✅ Status Tracking: All transitions logged

### Scheduler Performance
- ✅ Device Selection: < 100ms
- ✅ Reservation Awareness: Working correctly
- ✅ Multi-factor Scoring: Validated

### WebSocket Communication
- ✅ Agent Connection: Auto-reconnect working
- ✅ Message Delivery: < 50ms latency
- ✅ Terminal I/O: Real-time streaming

---

## 📈 Project Timeline

### Completed (Sep 2026)
- ✅ Week 1-2: Backend foundation, authentication
- ✅ Week 3-4: Container service, Docker integration
- ✅ Week 5-6: Scheduler, WebSocket communication
- ✅ Week 7-8: Terminal access, lifecycle testing

### Planned (Oct-Nov 2026)
- 🔄 Week 9-10: Multi-org backend (database, API)
- 🔄 Week 11-12: Frontend foundation (React setup)
- 🔄 Week 13-14: Admin dashboard
- 🔄 Week 15-16: Student dashboard
- 🔄 Week 17-18: Testing, polish, deployment

---

## 🤝 Contributing

This is an academic project. For questions or suggestions, please open an issue.

---

## 📄 License

This project is developed as part of B.Tech curriculum at UPES Dehradun.

---

## 👥 Team

**Project Lead**: [Your Name]  
**Institution**: UPES Dehradun  
**Supervisor**: [Supervisor Name]  
**Academic Year**: 2026-27

---

## 📞 Contact

- **Email**: [your.email@example.com]
- **GitHub**: [https://github.com/rajrishu1401/CampusCompute](https://github.com/rajrishu1401/CampusCompute)

---

**Last Updated**: September 27, 2026  
**Version**: 0.5.0 (MVP Complete, Multi-org Design Phase)  
**Status**: ✅ Core Features Operational, Ready for Multi-org Implementation
