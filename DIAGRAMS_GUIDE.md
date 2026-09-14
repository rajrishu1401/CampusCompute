# CampusCompute Architecture Diagrams Guide

## Overview

The `campuscompute_architecture.drawio` file contains **6 comprehensive diagrams** that cover every aspect of the system architecture, communication flows, and deployment topology.

## How to Open

1. **Online**: Go to https://app.diagrams.net/ and open the `.drawio` file
2. **VS Code**: Install the "Draw.io Integration" extension
3. **Desktop**: Download Draw.io desktop application from https://www.diagrams.net/

---

## Diagram 1: System Overview

**Purpose**: High-level three-layer architecture showing all major components

**What it shows**:
- **Client Layer**: React frontend (Student Portal, Admin Portal, xterm.js terminal)
- **Broker Layer**: Central Spring Boot application with all services
  - Nginx reverse proxy
  - Authentication, Scheduler, Container Manager, Device Manager, WebSocket Gateway
  - PostgreSQL database
  - Redis cache
  - Prometheus metrics
- **Lab Layer**: Multiple lab computers with agents and Docker containers
- Communication protocols between layers (HTTPS/WSS, mTLS)

**Key Insights**:
- Clear separation of concerns across three layers
- Central broker orchestrates everything
- Agents connect outbound (no inbound firewall rules needed)
- Multiple labs can participate in the resource pool

**Use this diagram for**:
- Project presentations
- Explaining overall architecture to stakeholders
- Understanding component relationships

---

## Diagram 2: Agent Initialization Flow

**Purpose**: Step-by-step process of installing and starting an agent on a lab PC

**What it shows**:
1. **Step 1**: Admin generates enrollment token via admin portal
2. **Step 2**: Install agent software on lab PC (commands shown)
3. **Step 3**: Configure agent with YAML config file (full example)
4. **Step 4**: Start agent as systemd service

**Agent Startup Sequence** (8 steps):
1. Load configuration
2. Verify Docker connection
3. Connect to broker via WSS
4. Enrollment/Authentication
5. Receive device certificate
6. Start heartbeat loop
7. Start resource monitor
8. Listen for commands → **AGENT READY**

**Key Insights**:
- Enrollment token is one-time use for security
- Agent gets a long-lived certificate after enrollment
- Agent uses systemd for automatic restart
- Complete configuration example provided

**Use this diagram for**:
- Lab administrator training
- Installation documentation
- Troubleshooting agent issues
- Understanding device registration

---

## Diagram 3: Container Creation Flow

**Purpose**: Complete end-to-end flow from student request to running container

**What it shows** (22 steps across 5 components):

### Student → Frontend (Steps 1-2)
1. Student requests container with specifications
2. Frontend builds API request with JWT token

### Frontend → Backend (Steps 3-6)
3. Validate JWT token
4. Check user quota (2 CPU requested, 4 CPU max allowed ✓)
5. Check lab reservations (no conflicts ✓)
6. Call scheduler

### Scheduler (Steps 7-9)
7. Filter devices (online, healthy, sufficient resources)
8. Score each device (PC-015: 0.82, PC-023: 0.91 ⭐, PC-007: 0.75)
9. Select best device: PC-023

### Backend (Steps 11-12)
11. Reserve resources (PostgreSQL transaction with lock)
12. Create database record (status: PROVISIONING)

### Backend → Agent (Step 13)
13. Send CREATE_CONTAINER command via WebSocket

### Agent (Steps 14-18)
14. Validate command
15. Create Docker volume (student-123-workspace)
16. Create container with security limits
17. Start container
18. Report success to broker

### Backend → Frontend → Student (Steps 21-22)
21. Update UI with container details
22. Student receives running container with terminal URL

**Total Time**: 3-8 seconds ⏱

**Key Insights**:
- Quota enforcement happens before scheduling
- Scheduler uses multi-factor scoring algorithm
- Resource reservation is atomic (PostgreSQL transaction)
- Agent validates all commands before executing
- Complete security policies applied to containers

**Use this diagram for**:
- Understanding request processing pipeline
- Performance optimization analysis
- Debugging container creation issues
- Demonstrating adaptive scheduling

---

## Diagram 4: Terminal Session Flow

**Purpose**: Real-time browser terminal communication from keystroke to output

**What it shows** (11 steps):

### Connection Establishment (Steps 1-4)
1. Student clicks "Open Terminal"
2. Initialize xterm.js in browser
3. Establish WebSocket connection with JWT
4. Connection accepted ✓

### Command Execution (Steps 5-11)
5. User types command: `ls -la`
6. Send via WebSocket to backend
7. Backend validates JWT and ownership
8. Backend routes to agent on PC-023
9. Agent executes `docker exec` command
10. Docker container runs command, produces output
11. Output flows back through agent → broker → browser → xterm.js

**Communication Path**:
```
Browser (xterm.js)
    ↕ WebSocket (WSS)
Backend (Spring Boot)
    ↕ WebSocket (mTLS)
Agent (Python)
    ↕ Docker Exec API
Container (Bash)
```

**Key Insights**:
- Three-layer proxy ensures security
- Student never directly accesses Docker
- Backend verifies ownership on every connection
- Real-time bidirectional communication
- All terminal I/O flows through secure channels

**Use this diagram for**:
- Understanding terminal architecture
- Debugging terminal connectivity issues
- Security audit and review
- Performance latency analysis

---

## Diagram 5: Agent-Broker Communication

**Purpose**: WebSocket connection patterns and all message types

**What it shows**:

### Connection Establishment
- Agent initiates outbound WSS connection
- mTLS mutual authentication (both present certificates)
- Persistent connection with auto-reconnect
- Exponential backoff: 1s, 2s, 4s, 8s... max 60s

### Message Types (with full JSON payloads):

**1. HEARTBEAT** (Agent → Broker, every 10 seconds)
```json
{
  "type": "HEARTBEAT",
  "device_id": "device-pc023",
  "timestamp": "2026-08-17T15:30:00Z",
  "resources": {
    "cpu_usage_percent": 17.5,
    "ram_usage_percent": 42.3,
    "available_cpu_cores": 6,
    "available_ram_mb": 10240,
    "available_storage_gb": 382
  },
  "docker": {
    "healthy": true,
    "version": "24.0.5",
    "containers_running": 3
  }
}
```

**2. CREATE_CONTAINER** (Broker → Agent)
- Full container specification with security policies
- Resource limits (CPU, RAM, storage, PIDs)
- Volume mounts
- Network configuration
- Expiration time

**3. STATUS_UPDATE** (Agent → Broker)
- Container lifecycle status changes
- Docker container ID
- IP address assigned
- Timestamps

**4. Other Commands**:
- START_CONTAINER
- STOP_CONTAINER
- DELETE_CONTAINER
- EXEC_TERMINAL
- GET_STATS
- GET_LOGS

**Key Insights**:
- Agent always initiates connection (outbound)
- mTLS ensures both parties are authenticated
- Heartbeat provides continuous health monitoring
- All commands have structured JSON format
- Message validation on both sides

**Use this diagram for**:
- Protocol documentation
- Agent development
- Message format specification
- Debugging communication issues

---

## Diagram 6: Complete Network Architecture

**Purpose**: Full deployment topology with all network zones, IP addresses, and security

**What it shows**:

### Network Zones:

**1. Internet/WAN**
- Public access point
- Student/admin browsers connect here

**2. Campus Firewall**
- Allowed ports: 443 (HTTPS/WSS), 22 (SSH admin only)
- All other traffic blocked

**3. DMZ/Server Zone (10.0.1.0/24)**
- **Load Balancer** (optional): 10.0.1.9
- **Nginx**: 10.0.1.10:443 (TLS termination)
- **Spring Boot**: 10.0.1.11:8080 (internal HTTP)
- **PostgreSQL**: 10.0.1.12:5432
- **Redis**: 10.0.1.13:6379

**4. Campus Internal Network (10.0.0.0/16)**
- **CS Lab 1**: 10.0.10.0/24 (50 PCs)
- **AI Lab**: 10.0.20.0/24 (40 PCs)
- **Network Lab**: 10.0.30.0/24 (30 PCs)
- Each PC runs agent + Docker with multiple containers

**5. Monitoring Zone (Optional)**
- **Prometheus**: 10.0.1.20:9090 (metrics collection)
- **Grafana**: 10.0.1.21:3000 (dashboards)
- **Alert Manager**: Email/Slack notifications

### Network Protocols:
- 🔴 **HTTPS/WSS (TLS 1.3)**: Internet ↔ Firewall ↔ DMZ
- 🟢 **WebSocket + mTLS**: DMZ ↔ Lab PCs (mutual authentication)
- ⚫ **HTTP/TCP**: Internal DMZ communication (not encrypted)

### Security Highlights:
1. All external traffic encrypted with TLS 1.3
2. Agent authentication via mTLS certificates (not passwords)
3. Student authentication via JWT tokens
4. Database and Redis only accessible from internal network
5. Containers isolated with resource limits and dropped capabilities
6. No direct Docker API exposure to students
7. Firewall allows only ports 443 and 22

**Key Insights**:
- Defense in depth with multiple security layers
- Clear network segmentation
- Agents initiate outbound connections (NAT/firewall friendly)
- Monitoring infrastructure is optional but recommended
- Scalable to hundreds of lab computers

**Use this diagram for**:
- Network architecture documentation
- Security audits
- Deployment planning
- Firewall rule configuration
- Infrastructure cost estimation

---

## Common Use Cases

### For Project Presentation
**Start with**: Diagram 1 (Overview) → Diagram 6 (Network Architecture)
- Shows big picture first
- Demonstrates enterprise-grade architecture

### For Technical Documentation
**Use**: All diagrams in order (1→2→3→4→5→6)
- Provides complete system understanding
- Each diagram builds on previous

### For Development Team
**Focus on**: Diagram 3 (Container Creation) + Diagram 4 (Terminal) + Diagram 5 (Agent Communication)
- Shows implementation details
- Message formats and APIs

### For Security Review
**Use**: Diagram 2 (Agent Init) + Diagram 4 (Terminal) + Diagram 6 (Network)
- Authentication mechanisms
- Network security boundaries
- Container isolation

### For Lab Administrators
**Start with**: Diagram 2 (Agent Initialization)
- Step-by-step installation guide
- Configuration examples

### For Students (User Documentation)
**Use**: Diagram 1 (Overview) + simplified version of Diagram 3
- How the system works
- What happens when they request resources

---

## Technical Details Summary

### Technologies Shown:
- **Frontend**: React, TypeScript, xterm.js, WebSocket
- **Backend**: Spring Boot, JWT, PostgreSQL, Redis
- **Agent**: Python, Docker SDK, WebSocket client, mTLS
- **Container**: Docker Engine, resource limits, security policies
- **Network**: Nginx, TLS 1.3, mTLS, HTTPS, WSS
- **Monitoring**: Prometheus, Grafana, Alert Manager

### Communication Protocols:
1. **Browser ↔ Broker**: HTTPS/WSS with JWT Bearer token
2. **Broker ↔ Agent**: WSS with mTLS (mutual certificate auth)
3. **Agent ↔ Docker**: Unix socket or TCP (Docker API)
4. **Backend ↔ PostgreSQL**: JDBC over TCP
5. **Backend ↔ Redis**: Redis protocol over TCP

### Security Mechanisms:
- **Authentication**: JWT (students), mTLS certificates (agents)
- **Authorization**: Role-Based Access Control (RBAC)
- **Encryption**: TLS 1.3 for all external traffic
- **Container Isolation**: Resource limits, capability dropping, no-new-privileges
- **Network**: Firewall, DMZ, internal network segmentation

---

## Color Coding Legend

The diagrams use consistent color coding:

| Color | Component Type |
|-------|---------------|
| 🔵 Blue (`#dae8fc`) | Client/Browser layer |
| 🟣 Purple (`#e1d5e7`) | User interfaces and portals |
| 🟢 Green (`#d5e8d4`) | Backend services and broker |
| 🟡 Yellow (`#fff2cc`) | Middleware and proxies |
| 🟠 Orange (`#ffe6cc`) | Agent software |
| 🔷 Cyan (`#b0e3e6`) | Docker containers |
| ⚪ Gray (`#f5f5f5`) | Databases and storage |
| 🔴 Red arrows | HTTPS/TLS encrypted |
| 🟢 Green arrows | WebSocket mTLS |
| 🟣 Purple arrows | WebSocket WSS |
| ⚫ Black arrows | Internal communication |

---

## Diagram Statistics

- **Total Diagrams**: 6
- **Total Components Shown**: 50+
- **Communication Flows**: 30+
- **Code Examples**: 15+
- **Network Zones**: 5
- **Lab Locations**: 3 examples
- **Message Types**: 7 documented

---

## Tips for Using These Diagrams

### In Documentation:
- Export as PNG/SVG for embedding in documents
- Use high DPI (300+) for print quality
- Include diagram number and title as caption

### In Presentations:
- Show diagrams in sequence for story flow
- Zoom in on specific sections for detail
- Use annotation tools to highlight during explanation

### For Code Development:
- Keep diagrams open while coding
- Refer to message formats in Diagram 5
- Use network addresses from Diagram 6

### For Testing:
- Follow flows in Diagram 3 and 4
- Test each step independently
- Verify message formats match Diagram 5

---

## Updating Diagrams

If you need to modify the diagrams:

1. Open in Draw.io
2. Select the diagram tab at bottom
3. Edit components/flows
4. Maintain consistent styling:
   - Font: Helvetica/Arial
   - Size: 10-12pt for text, 13-14pt for titles
   - Colors: Use existing palette
   - Arrows: 2-3pt thickness
5. Export updated versions

---

## Related Documentation

- **README.md**: Project overview and features
- **project_synopsis.tex**: Complete academic documentation
- **API Documentation**: (to be created) - REST API endpoints
- **Deployment Guide**: (to be created) - Server setup instructions

---

## Questions Answered by Each Diagram

### Diagram 1: System Overview
- What are the main components?
- How do they communicate?
- What technologies are used?

### Diagram 2: Agent Initialization
- How do we add a lab PC to the system?
- What configuration is needed?
- How does enrollment work?

### Diagram 3: Container Creation
- What happens when a student requests resources?
- How long does it take?
- What security checks are performed?

### Diagram 4: Terminal Session
- How does browser terminal work?
- Is it secure?
- What's the communication path?

### Diagram 5: Agent Communication
- What messages do agents send?
- How often do they communicate?
- What's the message format?

### Diagram 6: Network Architecture
- Where are components deployed?
- What IP addresses are used?
- How is the network secured?

---

## Next Steps

After reviewing these diagrams, you can:

1. **Share with team members** - Everyone gets complete picture
2. **Use in project presentation** - Visual aids for explanation
3. **Reference during development** - Keep diagrams handy
4. **Update as needed** - System evolves, diagrams should too
5. **Generate additional views** - Sequence diagrams, class diagrams, etc.

---

**Created**: August 17, 2026
**Project**: CampusCompute - Campus-Aware Cloud Resource Pooling System
**File**: campuscompute_architecture.drawio (6 diagrams)
