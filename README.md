# CampusCompute - Campus-Aware Cloud Resource Pooling System

[![React](https://img.shields.io/badge/React-18.3.1-blue.svg)](https://reactjs.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green.svg)](https://spring.io/projects/spring-boot)
[![Python](https://img.shields.io/badge/Python-3.13-yellow.svg)](https://www.python.org/)
[![License](https://img.shields.io/badge/License-MIT-red.svg)](LICENSE)

**A novel multi-tenant cloud platform for educational institutions to pool idle lab computing resources and provide on-demand container access to students.**

---

## 📖 Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Novel Contributions](#novel-contributions)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
- [Project Structure](#project-structure)
- [API Documentation](#api-documentation)
- [Screenshots](#screenshots)
- [Academic Context](#academic-context)
- [License](#license)

---

## 🎯 Overview

CampusCompute transforms idle lab computers into a powerful cloud computing platform. Instead of letting campus resources sit unused outside lab hours, CampusCompute pools them together to provide students with on-demand Docker containers for coursework, projects, and experimentation.

### The Problem

- **Underutilized Resources**: Campus lab computers remain idle 60-80% of the time
- **Limited Student Access**: Students can't access computing resources remotely
- **Scalability Issues**: Existing solutions don't scale across multiple organizations
- **Manual Management**: No intelligent resource allocation

### The Solution

CampusCompute provides:
- **Resource Pooling**: Aggregate idle lab computers into a compute pool
- **Multi-Tenancy**: Support multiple colleges/universities on one platform
- **Smart Scheduling**: Adaptive algorithm for container placement
- **Student Self-Service**: On-demand container creation with web terminal
- **Admin Dashboard**: Real-time monitoring and management

---

## ✨ Key Features

### For Organizations (Colleges/Universities)

- **Organization Registration**: Self-service registration with admin account
- **Device Management**: Add lab machines via enrollment tokens
- **Student Management**: Bulk upload students via CSV
- **Real-time Monitoring**: Dashboard with resource utilization stats
- **Multi-tier Support**: FREE, BASIC, PREMIUM subscription tiers

### For Students

- **Container Management**: Create, start, stop, delete Docker containers
- **Image Flexibility**: Support for any Docker image (Ubuntu, Python, Node, etc.)
- **Resource Quotas**: CPU and RAM allocation per student
- **Web Terminal**: xterm.js-based browser terminal with real-time access
- **Usage Tracking**: Monitor personal resource consumption

### For System

- **Adaptive Scheduling**: Novel reservation-aware container placement algorithm
- **Load Balancing**: Distribute containers across available devices
- **Health Monitoring**: Automatic device health checks via heartbeat
- **Fault Tolerance**: Handle device failures gracefully
- **Security**: JWT authentication, role-based access control, Docker isolation

---

## 🚀 Novel Contributions

### 1. Adaptive Scheduling Algorithm

**Reservation-Aware Scoring System**:
```
Device Score = Base Score + Reservation Penalty
- Considers upcoming lab reservations
- Penalizes devices with near-term reservations
- Balances load and availability
```

**Dynamic Resource Allocation**:
- Real-time resource availability tracking
- Intelligent device selection based on current load
- Predictive scheduling for long-running containers

### 2. Multi-Organization Architecture

**True Multi-Tenancy**:
- Organization-level isolation
- Per-organization quotas and policies
- Shared infrastructure, isolated data
- Role-based access control (ROOT, ORG_ADMIN, STUDENT)

**Scalable Design**:
- Support for unlimited organizations
- Per-organization device pools
- Independent admin dashboards
- Cross-organization security guarantees

### 3. Agent-Based Device Management

**Lightweight Python Agent**:
- Automatic device enrollment via tokens
- Real-time heartbeat and metrics
- Docker container lifecycle management
- WebSocket-based bidirectional communication

**Zero-Config Deployment**:
- Single enrollment token
- Auto-registration on first connection
- Dynamic configuration updates
- Self-healing reconnection logic

---

## 🏗️ Architecture

### System Components

```
┌─────────────────────────────────────────────────────────────┐
│                         Frontend (React)                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Public Pages │  │ Admin Panel  │  │Student Panel │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ HTTP/WebSocket
                            │
┌─────────────────────────────────────────────────────────────┐
│                   Backend (Spring Boot)                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ REST API     │  │ WebSocket    │  │ Scheduler    │      │
│  │ Controllers  │  │ Handlers     │  │ Service      │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Auth Service │  │ Container    │  │Organization  │      │
│  │ (JWT)        │  │ Service      │  │ Service      │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
           │                    │                    │
           │                    │                    │
    ┌──────▼──────┐      ┌─────▼─────┐      ┌──────▼──────┐
    │ PostgreSQL  │      │   Redis   │      │  WebSocket  │
    │  Database   │      │   Cache   │      │   Broker    │
    └─────────────┘      └───────────┘      └──────┬──────┘
                                                    │
                                                    │
                    ┌───────────────────────────────┴────────┐
                    │                                        │
            ┌───────▼──────┐                        ┌───────▼──────┐
            │ Agent (LAB-1)│                        │ Agent (LAB-2)│
            │  - Heartbeat │                        │  - Heartbeat │
            │  - Metrics   │                        │  - Metrics   │
            │  - Docker    │                        │  - Docker    │
            └──────────────┘                        └──────────────┘
```

### Data Flow

**Container Creation**:
1. Student submits container request via frontend
2. Backend validates quotas and permissions
3. Scheduler selects optimal device
4. Backend sends creation command via WebSocket
5. Agent pulls image and starts container
6. Agent reports status back to backend
7. Frontend displays running container

**Terminal Access**:
1. Student clicks terminal icon
2. Frontend opens WebSocket to backend
3. Backend proxies to agent WebSocket
4. Agent attaches to container TTY
5. Bidirectional data streaming
6. Real-time terminal interaction

---

## 🛠️ Technology Stack

### Frontend
- **React 18.3** - UI library
- **Redux Toolkit** - State management
- **Material-UI 6** - Component library
- **React Router 6** - Routing
- **Axios** - HTTP client
- **xterm.js** - Terminal emulator
- **Vite** - Build tool

### Backend
- **Spring Boot 3.2** - Java framework
- **Spring Security** - Authentication & authorization
- **Spring WebSocket** - Real-time communication
- **PostgreSQL 15** - Relational database
- **Redis 7** - Caching & session storage
- **JJWT** - JWT token handling
- **Docker Java API** - Container management

### Agent
- **Python 3.13** - Programming language
- **asyncio** - Asynchronous I/O
- **websockets** - WebSocket client
- **docker-py** - Docker SDK
- **psutil** - System monitoring

### Infrastructure
- **Docker 24+** - Containerization
- **Docker Compose** - Multi-container orchestration
- **Git** - Version control

---

## 📚 Documentation

Comprehensive guides are available in the `/docs` directory:

- **[Admin User Guide](docs/ADMIN_GUIDE.md)** - Organization setup, device management, student management
- **[Student User Guide](docs/STUDENT_GUIDE.md)** - Container creation, terminal usage, best practices
- **[Installation Guide](docs/INSTALLATION.md)** - Production deployment, security, monitoring

---

## 🚀 Getting Started

### Prerequisites

- **Java Development Kit (JDK)**: 17 or higher
- **Node.js**: 18+ and npm
- **Python**: 3.11+
- **Docker**: 24+ with Docker Compose
- **PostgreSQL**: 15+ (or use Docker)
- **Redis**: 7+ (or use Docker)

### Quick Start with Docker Compose

1. **Clone the Repository**
   ```bash
   git clone https://github.com/rajrishu1401/CampusCompute.git
   cd CampusCompute
   ```

2. **Start Database Services**
   ```bash
   docker-compose up -d
   ```
   This starts PostgreSQL and Redis in containers.

3. **Start Backend**
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```
   Backend runs on http://localhost:8081

4. **Start Frontend**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   Frontend runs on http://localhost:3000

5. **Configure Agent** (on lab machines)
   ```bash
   cd agent
   pip install -r requirements.txt
   
   # Edit config.yaml with enrollment token
   # Get token from admin dashboard
   
   python src/main.py
   ```

### Environment Configuration

**Backend** (`backend/src/main/resources/application.properties`):
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/campuscompute
spring.datasource.username=campuscompute
spring.datasource.password=campuscompute123

spring.data.redis.host=localhost
spring.data.redis.port=6379

jwt.secret=your-secret-key-here
jwt.expiration=86400000
```

**Agent** (`agent/config.yaml`):
```yaml
broker:
  url: "ws://localhost:8081/ws/agent"
  enrollment_token: "your-token-here"
  organization_id: 1

device:
  device_id: "LAB-PC-001"
  hostname: "Lab Computer 1"

docker:
  socket: "unix:///var/run/docker.sock"  # Linux/Mac
  # socket: "npipe:////./pipe/docker_engine"  # Windows
```

---

## 📁 Project Structure

```
CampusCompute/
├── backend/                    # Spring Boot backend
│   ├── src/main/java/com/campuscompute/
│   │   ├── config/            # Security, CORS, WebSocket config
│   │   ├── controller/        # REST API controllers
│   │   ├── dto/               # Data Transfer Objects
│   │   ├── entity/            # JPA entities
│   │   ├── repository/        # Database repositories
│   │   ├── service/           # Business logic
│   │   ├── security/          # JWT utilities
│   │   ├── websocket/         # WebSocket handlers
│   │   └── exception/         # Custom exceptions
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
│
├── frontend/                   # React frontend
│   ├── src/
│   │   ├── components/        # Reusable components
│   │   ├── pages/             # Page components
│   │   │   ├── admin/         # Admin pages
│   │   │   └── student/       # Student pages
│   │   ├── services/          # API services
│   │   ├── store/             # Redux store
│   │   ├── App.jsx            # Main app
│   │   └── main.jsx           # Entry point
│   ├── public/
│   ├── package.json
│   └── vite.config.js
│
├── agent/                      # Python agent
│   ├── src/
│   │   ├── agent.py           # Main agent logic
│   │   ├── docker_manager.py  # Docker operations
│   │   ├── websocket_client.py # WebSocket connection
│   │   ├── system_monitor.py  # System metrics
│   │   └── main.py            # Entry point
│   ├── config.yaml
│   └── requirements.txt
│
├── docker-compose.yml          # Database services
├── .gitignore
└── README.md
```

---

## 📚 API Documentation

### Authentication

#### POST `/api/auth/login`
Login with username and password.
```json
Request:
{
  "username": "admin",
  "password": "password"
}

Response:
{
  "success": true,
  "data": {
    "token": "jwt-token-here",
    "user": {
      "id": 1,
      "username": "admin",
      "role": "ORG_ADMIN",
      "organizationId": 1
    }
  }
}
```

### Organization Management (Admin)

#### GET `/api/organizations/stats`
Get organization statistics (requires ORG_ADMIN role).
```json
Response:
{
  "success": true,
  "data": {
    "organizationName": "UPES Dehradun",
    "devices": { "total": 10, "online": 8, "offline": 2, "busy": 3 },
    "students": { "total": 150, "approved": 145, "pending": 5 },
    "containers": { "total": 45, "running": 32, "stopped": 10 },
    "resources": {
      "cpuTotal": 40,
      "cpuUsed": 28,
      "cpuAvailable": 12,
      "cpuUtilizationPercent": 70.0,
      "ramTotal": 160,
      "ramUsed": 112,
      "ramAvailable": 48,
      "ramUtilizationPercent": 70.0
    }
  }
}
```

#### GET `/api/organizations/devices`
List all devices for organization.

#### POST `/api/organizations/devices/token`
Generate device enrollment token.

#### GET `/api/organizations/students`
List all students for organization.

#### POST `/api/organizations/students/upload`
Bulk upload students via CSV.

### Container Management (Student)

#### POST `/api/containers`
Create a new container.
```json
Request:
{
  "imageName": "ubuntu:latest",
  "cpuCores": 2,
  "ramGb": 4
}

Response:
{
  "success": true,
  "data": {
    "id": 1,
    "imageName": "ubuntu:latest",
    "status": "PENDING",
    "cpuCores": 2,
    "ramGb": 4,
    "userId": 5
  }
}
```

#### GET `/api/containers`
List user's containers.

#### POST `/api/containers/{id}/stop`
Stop a running container.

#### DELETE `/api/containers/{id}`
Delete a container.

---

## 📸 Screenshots

### Landing Page
Modern hero section with features showcase and call-to-action.

### Admin Dashboard
Real-time statistics with resource utilization charts and device/student counts.

### Device Management
List of all devices with status, resources, and enrollment token generation.

### Student Dashboard
Overview of containers, resource quotas, and quick actions.

### Container Management
Create, start, stop, and delete containers with resource allocation.

### Web Terminal
Full xterm.js terminal emulator with real-time container access.

---

## 🎓 Academic Context

### Institution
**University of Petroleum and Energy Studies (UPES), Dehradun**  
**B.Tech Computer Science - Major Project**  
**Academic Year**: 2026-27 (7th Semester)

### Project Objectives

1. **Resource Optimization**: Maximize utilization of campus computing infrastructure
2. **Cloud Computing**: Demonstrate practical cloud computing concepts
3. **Multi-Tenancy**: Implement true multi-tenant SaaS architecture
4. **Novel Algorithm**: Develop adaptive scheduling algorithm
5. **Full-Stack Development**: Complete end-to-end system implementation

### Learning Outcomes

- Microservices architecture design
- RESTful API development
- WebSocket real-time communication
- React frontend development
- Database design and optimization
- Docker containerization
- JWT authentication and authorization
- Role-based access control
- Agent-based distributed systems
- Scheduling algorithms

---

## 📊 Project Phases

### Phase 1: Multi-Organization Backend ✅
- Organization entity and multi-tenancy
- User types and roles
- Device and lab management
- JWT authentication
- **Completion**: September 2026

### Phase 2: Agent Integration ✅
- Python agent development
- WebSocket communication
- Enrollment token system
- Docker management
- **Completion**: September 2026

### Phase 3: Statistics & Analytics ✅
- Dashboard statistics API
- Device monitoring
- Student usage tracking
- Resource utilization
- **Completion**: October 2026

### Phase 4: Frontend Development ✅
- React application
- Admin dashboard
- Student portal
- Web terminal (xterm.js)
- **Completion**: October 2026

### Phase 4.5: Windows Installer ✅
- Professional GUI installer
- Automatic dependency detection
- Windows service integration
- PyInstaller build system
- **Completion**: October 2026

### Phase 5: Documentation & Testing 🔄
- ✅ Admin User Guide (complete)
- ✅ Student User Guide (complete)
- ✅ Installation Guide (complete)
- 🔄 End-to-end testing (in progress)
- ⏳ Performance optimization
- ⏳ Cloud deployment
- **Target**: Mid-October 2026

---

## 🤝 Contributing

This is an academic project. Contributions, suggestions, and feedback are welcome!

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👥 Authors

**Rishu Raj**  
B.Tech Computer Science, UPES Dehradun  
Email: [your-email@example.com](mailto:your-email@example.com)  
GitHub: [@rajrishu1401](https://github.com/rajrishu1401)

---

## 🙏 Acknowledgments

- UPES Faculty for guidance and support
- Spring Boot and React communities
- Docker team for excellent documentation
- Material-UI for beautiful components
- xterm.js for terminal emulation

---

## 📞 Support

For issues, questions, or suggestions:
- **GitHub Issues**: [Report an issue](https://github.com/rajrishu1401/CampusCompute/issues)
- **Email**: [your-email@example.com](mailto:your-email@example.com)
- **Documentation**: See `/docs` directory

---

## 🗺️ Roadmap

### Current Version: 1.0.0 (✅ Feature Complete + Documentation)

**Completed:**
- ✅ Multi-organization backend architecture
- ✅ Python agent with enrollment system
- ✅ Statistics and analytics APIs
- ✅ React frontend with Material-UI
- ✅ Windows installer with GUI
- ✅ Complete user documentation
- ✅ Installation & deployment guides

**In Progress:**
- 🔄 End-to-end system testing
- 🔄 Performance optimization
- 🔄 Bug fixes and polish

### Future Enhancements (v2.0+)
- [ ] Container snapshots and backups
- [ ] GPU support for ML workloads
- [ ] Kubernetes integration
- [ ] Cost tracking per student
- [ ] Email notifications
- [ ] Mobile app (React Native)
- [ ] Advanced analytics dashboard
- [ ] API rate limiting
- [ ] Container templates library
- [ ] Automated scaling policies

---

**Built with ❤️ for educational institutions**

⭐ Star this repository if you find it helpful!
