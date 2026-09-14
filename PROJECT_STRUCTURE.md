# CampusCompute - Project Structure

## Overview

```
Major/
├── backend/                    # Spring Boot Backend (Broker)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/campuscompute/
│   │   │   │   ├── config/          # Spring configuration
│   │   │   │   ├── controller/      # REST API controllers
│   │   │   │   ├── dto/             # Data Transfer Objects
│   │   │   │   ├── entity/          # JPA entities (User, Device, Container)
│   │   │   │   ├── exception/       # Custom exceptions
│   │   │   │   ├── repository/      # Spring Data JPA repositories
│   │   │   │   ├── security/        # JWT & Security config
│   │   │   │   ├── service/         # Business logic & scheduler
│   │   │   │   ├── websocket/       # WebSocket handlers
│   │   │   │   └── CampusComputeApplication.java
│   │   │   └── resources/
│   │   │       └── application.yml  # Configuration
│   │   └── test/
│   ├── pom.xml                      # Maven dependencies
│   └── README.md
│
├── agent/                      # Python Agent (Lab PCs)
│   ├── src/
│   │   ├── __init__.py
│   │   ├── main.py              # Entry point
│   │   ├── config.py            # Configuration loader
│   │   ├── agent.py             # Main agent logic
│   │   ├── docker_manager.py    # Docker operations
│   │   ├── system_monitor.py    # System metrics (CPU, RAM, disk)
│   │   ├── websocket_client.py  # WebSocket communication (TODO)
│   │   └── auth/
│   │       ├── __init__.py
│   │       └── mtls.py          # mTLS authentication (TODO)
│   ├── config.yaml              # Agent configuration
│   ├── requirements.txt         # Python dependencies
│   ├── setup.py                 # Installation script
│   └── README.md
│
├── docs/                       # Documentation
│   ├── README.md               # Comprehensive project documentation
│   ├── project_synopsis.pdf    # 41-page synopsis
│   ├── project_synopsis.tex    # LaTeX source
│   ├── PPT_CONTENT.md         # Presentation content (12 slides)
│   ├── DIAGRAMS_GUIDE.md      # How to use diagrams
│   ├── QUICK_REFERENCE.md     # Quick reference guide
│   ├── TECH_STACK_GUIDE.md    # Technology decisions
│   └── IMPLEMENTATION_PLAN.md # Development phases
│
└── diagrams/                   # Architecture diagrams
    ├── campuscompute_architecture.drawio      # 6 technical diagrams
    └── campuscompute_network_access.drawio    # 3 network diagrams
```

## Current Status

### ✅ Completed

**Documentation:**
- Complete project synopsis (41 pages)
- Architecture diagrams (9 diagrams)
- Presentation content (12 slides)
- Technical documentation

**Backend Structure:**
- Maven project setup (pom.xml)
- Spring Boot configuration (application.yml)
- Core entities (User, Device, Container)
- Main application class

**Agent Structure:**
- Python project setup (requirements.txt)
- Configuration system (config.yaml)
- Docker manager (create, stop, delete containers)
- System monitor (CPU, RAM, disk metrics)
- Main agent logic with heartbeat & monitoring loops

### 🔄 In Progress

**Backend (Next Steps):**
1. Create repositories (UserRepository, DeviceRepository, ContainerRepository)
2. Implement services (UserService, DeviceService, ContainerService)
3. Build REST controllers (AuthController, ContainerController, DeviceController)
4. Add JWT authentication & security
5. Implement WebSocket handlers (AgentWebSocket, TerminalWebSocket)
6. Build adaptive scheduler algorithm
7. Add lab reservation system

**Agent (Next Steps):**
1. Implement WebSocket client to connect to broker
2. Add mTLS authentication
3. Test container creation/deletion
4. Add error recovery mechanisms
5. Create systemd service file
6. Build agent enrollment flow

### ⏳ Not Started

- Frontend (React application)
- Database migrations
- Integration tests
- Deployment scripts
- Monitoring & logging setup (Prometheus/Grafana)

## Technology Stack

### Backend
- **Language:** Java 17
- **Framework:** Spring Boot 3.2
- **Database:** PostgreSQL 15
- **Cache:** Redis 7
- **Security:** JWT, Spring Security
- **Communication:** WebSocket, REST API
- **Build:** Maven

### Agent
- **Language:** Python 3.11
- **Container Engine:** Docker 24+
- **Libraries:** docker-py, psutil, websockets
- **Security:** mTLS certificates
- **Deployment:** systemd service

### Frontend (Planned)
- **Framework:** React 18 + TypeScript
- **Build:** Vite
- **Styling:** Tailwind CSS
- **Terminal:** xterm.js
- **Communication:** WebSocket

## Development Workflow

### Phase 1: MVP (Current - Weeks 1-6)
- [ ] Backend authentication system
- [ ] Device registration & heartbeat
- [ ] Basic container lifecycle (create, stop, delete)
- [ ] Simple scheduler (CPU/RAM based only)
- [ ] Agent-broker WebSocket communication
- [ ] Basic REST API

### Phase 2: Advanced Features (Weeks 7-9)
- [ ] Lab reservation system
- [ ] Quota management
- [ ] File upload/download
- [ ] Admin dashboard
- [ ] Monitoring & metrics
- [ ] Browser terminal

### Phase 3: Research & Polish (Weeks 9-10)
- [ ] Adaptive campus scheduler implementation
- [ ] Performance evaluation
- [ ] Security hardening
- [ ] Documentation finalization

## Quick Start

### Backend
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### Agent
```bash
cd agent
pip install -r requirements.txt
python src/main.py
```

## Next Immediate Tasks

1. **Backend:**
   - Create JPA repositories
   - Implement UserService with JWT
   - Build AuthController (/api/auth/login, /api/auth/register)
   - Test authentication flow

2. **Agent:**
   - Implement WebSocket client
   - Connect agent to broker
   - Test heartbeat mechanism
   - Test container creation via broker command

3. **Integration:**
   - Setup PostgreSQL database
   - Setup Redis
   - Test end-to-end: agent → broker → database

## Team Responsibilities

- **Backend Developer:** Spring Boot API, database, scheduler
- **Scheduling Developer:** Adaptive scheduler algorithm, reservation system
- **Infrastructure Developer:** Python agent, Docker, deployment, mTLS
- **Frontend Developer:** React app, UI/UX, terminal integration (future)

## Resources

- **Synopsis:** `project_synopsis.pdf`
- **Architecture:** `campuscompute_architecture.drawio`
- **Presentation:** `PPT_CONTENT.md`
- **Backend README:** `backend/README.md`
- **Agent README:** `agent/README.md`
