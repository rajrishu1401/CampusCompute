# CampusCompute - Project Summary & Quick Reference

## 📁 All Documentation Files

1. **README.md** - Complete project overview, architecture, features
2. **IMPLEMENTATION_PLAN.md** - 12-week detailed implementation plan (THIS IS YOUR ROADMAP)
3. **GETTING_STARTED.md** - Day 1 setup instructions (START HERE)
4. **TECH_STACK_GUIDE.md** - Technology decisions and architecture patterns
5. **PPT_CONTENT.md** - 12-slide synopsis presentation content
6. **project_synopsis.tex** - 41-page LaTeX synopsis document
7. **project_synopsis.pdf** - Compiled synopsis PDF
8. **campuscompute_architecture.drawio** - 6 technical diagrams
9. **campuscompute_network_access.drawio** - 3 network/security diagrams

---

## 🎯 Project Overview (30-Second Pitch)

**Problem**: College lab computers sit idle 70-80% of the time (nights, weekends, holidays)

**Solution**: CampusCompute pools idle lab PCs and lets students request Docker containers remotely via browser

**Novel Contribution**: Academic-aware scheduler that avoids scheduling long-running containers on lab PCs with upcoming classes

**Tech Stack**: Spring Boot (backend) + React (frontend) + Python (agent) + Docker

**Impact**: 18% → 60-70% utilization (3-4x improvement)

---

## 🏗️ Architecture (3-Layer)

```
┌─────────────────────────────────────────┐
│  LAYER 1: Client (Browser)              │
│  React + TypeScript                     │
│  Student Portal + Admin Dashboard       │
└──────────────┬──────────────────────────┘
               │ HTTPS/WebSocket
               ▼
┌─────────────────────────────────────────┐
│  LAYER 2: Campus Broker (Server)        │
│  Spring Boot + PostgreSQL + Redis       │
│  Auth, Scheduler, Container Manager     │
└──────────────┬──────────────────────────┘
               │ WebSocket (mTLS)
      ┌────────┼────────┐
      ▼        ▼        ▼
┌──────────────────────────────────────┐
│  LAYER 3: Lab PCs (Agents)           │
│  Python + Docker Engine              │
│  Create/manage containers            │
└──────────────────────────────────────┘
```

---

## 🛣️ Implementation Order

### Phase 1: Backend Foundation (Week 1-3)
- ✅ Authentication (JWT)
- ✅ Device management
- ✅ Container APIs
- ✅ Basic scheduler

### Phase 2: Agent Development (Week 4-5)
- ✅ Python agent
- ✅ Docker integration
- ✅ WebSocket connection
- ✅ Heartbeat system

### Phase 3: Advanced Backend (Week 6-7)
- ✅ Lab reservations
- ✅ Monitoring & stats
- ✅ Terminal WebSocket proxy
- ✅ File upload/download

### Phase 4: Frontend (Week 8-10)
- ✅ Student portal (containers, terminal)
- ✅ Admin portal (devices, labs, users)
- ✅ Authentication UI
- ✅ Monitoring dashboard

### Phase 5: Research & Polish (Week 11-12)
- ✅ Adaptive scheduler (novel contribution)
- ✅ Performance benchmarking
- ✅ Security hardening
- ✅ Documentation

---

## 🚀 How to Start (First Day)

### Step 1: Install Prerequisites (2 hours)
```bash
# Java 17+ (OpenJDK)
java -version

# Maven
mvn -version

# PostgreSQL (via Docker)
docker run --name campuscompute-db -e POSTGRES_PASSWORD=password123 -p 5432:5432 -d postgres:15

# Redis (via Docker)
docker run --name campuscompute-redis -p 6379:6379 -d redis:7

# Python 3.11+
python --version

# Node.js 18+
node -v
```

### Step 2: Create Project Structure (30 minutes)
```bash
mkdir campuscompute
cd campuscompute
mkdir backend frontend agent infrastructure docs
```

### Step 3: Initialize Backend (1 hour)
- Go to https://start.spring.io/
- Configure: Maven, Java 17, Spring Boot 3.2
- Add dependencies: Web, Security, JPA, PostgreSQL, WebSocket, Validation
- Extract to `backend/` folder
- Configure `application.yml` (database, JWT secret)
- Run: `mvn spring-boot:run`

### Step 4: Initialize Frontend (30 minutes)
```bash
cd frontend
npm create vite@latest . -- --template react-ts
npm install
npm install react-router-dom axios @tanstack/react-query tailwindcss
npm run dev
```

### Step 5: Initialize Agent (30 minutes)
```bash
cd agent
python -m venv venv
venv\Scripts\activate  # Windows
pip install docker websocket-client psutil pyyaml
python main.py
```

**See GETTING_STARTED.md for detailed instructions**

---

## 📊 Key Features by User Role

### Students Can:
- ✅ Request containers (Ubuntu, Python, Java images)
- ✅ Use browser-based terminal
- ✅ Upload/download files
- ✅ Start/stop/delete containers
- ✅ View resource usage

### Admins Can:
- ✅ Register lab computers
- ✅ Approve/disable devices
- ✅ Create lab reservations
- ✅ Manage user quotas
- ✅ View campus-wide statistics
- ✅ View audit logs

### System Does:
- ✅ Schedules containers on best available device
- ✅ Enforces resource quotas
- ✅ Isolates containers (Docker)
- ✅ Respects lab schedules
- ✅ Monitors device health
- ✅ Logs all actions

---

## 🗄️ Core Database Tables

```sql
users           -- Students, faculty, admins
departments     -- CS, AI, Networks, etc.
labs            -- CS Lab, AI Lab, Network Lab
devices         -- Individual lab PCs
containers      -- Student containers
volumes         -- Persistent storage
reservations    -- Lab class schedules
quotas          -- Per-user resource limits
heartbeats      -- Device health metrics
audit_logs      -- All system actions
```

---

## 🔐 Security Model

### 4 Layers of Security

**Layer 1: Internet Boundary**
- TLS 1.3 encryption
- JWT authentication
- Firewall (port 443 only)

**Layer 2: Agent Authentication**
- mTLS certificates
- Agents connect outbound (no inbound ports on lab PCs)

**Layer 3: Container Isolation**
- CPU/RAM/PID limits
- Dropped capabilities (no privileged mode)
- Separate networks

**Layer 4: Network Segmentation**
- DMZ zone: Public-facing broker
- Internal zone: Lab PCs (not internet-exposed)

---

## 🎨 Frontend Pages

### Student Portal
- `/login` - Login page
- `/dashboard` - Container count, resource usage
- `/containers` - List of containers
- `/containers/:id` - Container details
- `/containers/:id/terminal` - Browser terminal
- `/profile` - User settings

### Admin Portal
- `/admin/dashboard` - Campus statistics
- `/admin/devices` - Device management
- `/admin/labs` - Lab management
- `/admin/reservations` - Reservation calendar
- `/admin/users` - User management
- `/admin/monitoring` - Real-time metrics

---

## 📡 API Endpoints (Key Ones)

### Authentication
```
POST /api/auth/login
POST /api/auth/logout
GET  /api/auth/me
```

### Containers (Student)
```
GET    /api/containers
POST   /api/containers
GET    /api/containers/:id
PATCH  /api/containers/:id/start
PATCH  /api/containers/:id/stop
DELETE /api/containers/:id
WS     /ws/terminal/:id
```

### Devices (Admin)
```
GET    /api/admin/devices
POST   /api/admin/devices/enroll
PATCH  /api/admin/devices/:id/enable
PATCH  /api/admin/devices/:id/disable
```

### Reservations (Admin)
```
GET    /api/admin/reservations
POST   /api/admin/reservations
DELETE /api/admin/reservations/:id
```

---

## 🔄 Complete Request Flow (Container Creation)

```
1. Student clicks "Create Container" in UI
2. Frontend sends: POST /api/containers {image, cpu, ram}
3. Backend authenticates JWT
4. Backend checks quota (max 4 CPU allowed?)
5. Backend checks reservations (any conflicts?)
6. Scheduler selects best device (PC-23)
7. Backend saves to database (status=PENDING)
8. Backend sends WebSocket message to Agent-23
9. Agent creates Docker container with limits
10. Agent reports success
11. Backend updates database (status=RUNNING)
12. Frontend receives response
13. Student clicks "Open Terminal"
14. Frontend opens WebSocket to /ws/terminal/:id
15. Terminal session starts
```

**Total time: 3-8 seconds**

---

## 🧮 Adaptive Scheduler (Novel Contribution)

### Traditional Scheduler
```
Score = CPU + RAM + Network
```

### CampusCompute Scheduler (Academic-Aware)
```
Score = CPU + RAM + Load + ReservationProximity + Reliability
                            ↑
                      NEW FACTOR!
```

### Reservation Proximity Algorithm
```python
def calculate_proximity_score(device, duration):
    next_reservation = find_next_reservation(device.lab)
    
    if no next_reservation:
        return 1.0  # Best score
    
    time_until_reservation = next_reservation.start - now
    
    if container_end_time > next_reservation.start:
        return 0.1  # Conflict! Avoid this device
    
    # More time = better score
    return min(1.0, time_until_reservation / 24_hours)
```

**Example**:
- 9:40 AM: Student requests 6-hour container
- CS Lab has class at 10:00 AM → Score: 0.1 ❌
- AI Lab free until 6:00 PM → Score: 0.9 ✅
- Scheduler selects AI Lab

---

## 📈 Expected Outcomes

### Performance Targets
- ⚡ Container startup: < 10 seconds (P95)
- ⚡ Terminal latency: < 100ms (P95)
- ⚡ API response: < 500ms (P95)
- 🔒 System uptime: 99%+

### Utilization Improvement
- Before: 18% average lab utilization
- After: 60-70% utilization
- Impact: 3-4x better resource usage

### Research Metrics
- Reservation conflict avoidance rate
- Scheduling algorithm effectiveness
- Comparison with baseline (first-available) scheduler

---

## 🛠️ Development Tools

### Backend
- **IDE**: IntelliJ IDEA Community (free) or VS Code
- **Build**: Maven
- **Database**: PostgreSQL + pgAdmin (GUI)
- **Testing**: JUnit, Mockito, Testcontainers
- **API Testing**: Postman or Insomnia

### Frontend
- **IDE**: VS Code with extensions (ESLint, Prettier, Tailwind)
- **Dev Server**: Vite
- **Testing**: Vitest, React Testing Library
- **Browser**: Chrome DevTools

### Agent
- **IDE**: VS Code or PyCharm Community
- **Package Manager**: pip + venv
- **Testing**: pytest
- **Docker**: Docker Desktop (Windows)

### Database
- **Client**: pgAdmin, DBeaver, or TablePlus
- **Migrations**: Flyway or Liquibase (optional)

---

## 🐛 Common Issues & Solutions

### "Port 8080 already in use"
**Solution**: Kill process or change port in `application.yml`

### "Database connection refused"
**Solution**: Check if PostgreSQL is running: `docker ps`

### "Maven dependencies not downloading"
**Solution**: Check internet, try `mvn clean install -U`

### "Cannot connect to Docker daemon"
**Solution**: Start Docker Desktop, check if Docker is running

### "CORS error in browser"
**Solution**: Add frontend URL to `cors.allowed-origins` in `application.yml`

### "JWT token invalid"
**Solution**: Check if token is expired, clear localStorage and login again

---

## 📚 Learning Resources

### Spring Boot
- Official Docs: https://docs.spring.io/spring-boot/
- Tutorial: https://spring.io/guides/gs/spring-boot/
- Video: Spring Boot Complete Course (YouTube)

### React + TypeScript
- Official Docs: https://react.dev/
- TypeScript Docs: https://www.typescriptlang.org/
- Tutorial: React TypeScript Cheatsheet

### Docker
- Official Docs: https://docs.docker.com/
- Python SDK: https://docker-py.readthedocs.io/

### PostgreSQL
- Official Docs: https://www.postgresql.org/docs/
- Tutorial: PostgreSQL Tutorial (PostgreSQL Tutorial website)

### WebSocket
- MDN: https://developer.mozilla.org/en-US/docs/Web/API/WebSocket
- Spring WebSocket: https://docs.spring.io/spring-framework/reference/web/websocket.html

---

## ✅ Week 1 Checklist (Your First Week)

### Day 1-2: Setup
- [ ] Install Java, Maven, PostgreSQL, Redis, Python, Node.js
- [ ] Create project folders
- [ ] Initialize Git repository
- [ ] Setup Spring Boot project
- [ ] Configure database connection
- [ ] Run backend successfully

### Day 3-4: Authentication
- [ ] Create User entity and repository
- [ ] Implement registration and login
- [ ] Generate and validate JWT
- [ ] Configure Spring Security
- [ ] Test with Postman

### Day 5: RBAC
- [ ] Create Role enum
- [ ] Add @PreAuthorize annotations
- [ ] Test different user roles

### Weekend: Review
- [ ] Review code
- [ ] Write documentation
- [ ] Plan Week 2

---

## 🎯 Success Criteria

### MVP (Minimum Viable Product)
- ✅ Students can login
- ✅ Students can create Ubuntu containers
- ✅ Students can use browser terminal
- ✅ Admins can register devices
- ✅ System schedules containers to available devices
- ✅ Resource limits enforced

### Complete System
- ✅ All MVP features
- ✅ Lab reservation system
- ✅ Quota management
- ✅ File upload/download
- ✅ Monitoring dashboard
- ✅ Adaptive scheduler
- ✅ Audit logging

### Research Contribution
- ✅ Academic-aware scheduling algorithm
- ✅ Performance benchmarking
- ✅ Comparison with baseline
- ✅ Research paper draft

---

## 📞 Team Coordination Tips

### Daily Standup (15 minutes)
- What did I do yesterday?
- What will I do today?
- Any blockers?

### Weekly Planning (Monday, 1 hour)
- Review last week's progress
- Assign tasks for this week
- Discuss architecture decisions

### Weekly Demo (Saturday, 1 hour)
- Each member demos their work
- Code review
- Discuss next steps

### Communication
- **Urgent**: Voice/video call
- **Questions**: Slack/Discord
- **Code Review**: GitHub pull requests
- **Documentation**: Google Docs/Notion

---

## 🚀 You're Ready!

### What to do RIGHT NOW:

1. **Read GETTING_STARTED.md** - Install all prerequisites
2. **Follow the setup** - Create project structure
3. **Open IMPLEMENTATION_PLAN.md** - Start Phase 1, Week 1
4. **Join your team** - Coordinate with team members
5. **Start coding!** - Begin with authentication system

### Timeline

- **Week 1-3**: Backend foundation ← You are here
- **Week 4-5**: Agent development
- **Week 6-7**: Advanced backend
- **Week 8-10**: Frontend
- **Week 11-12**: Research & polish

### Remember

- ✅ Test frequently (don't wait until the end)
- ✅ Commit small changes (not one big commit)
- ✅ Document as you go (don't leave it for later)
- ✅ Ask for help when stuck (team is there)
- ✅ Focus on MVP first (don't add extra features)

---

**Good luck! You've got everything you need to build an amazing project! 🎓🚀**

---

## 📋 Quick Command Reference

### Backend
```bash
cd backend
mvn spring-boot:run           # Run
mvn clean package             # Build
mvn test                      # Test
```

### Frontend
```bash
cd frontend
npm run dev                   # Run
npm run build                 # Build
npm run lint                  # Lint
```

### Agent
```bash
cd agent
venv\Scripts\activate         # Activate venv (Windows)
python main.py                # Run
pytest                        # Test
```

### Database
```bash
# Connect
docker exec -it campuscompute-db psql -U postgres -d campuscompute

# View tables
\dt

# Quit
\q
```

### Infrastructure
```bash
cd infrastructure
docker-compose up -d          # Start
docker-compose down           # Stop
docker-compose logs -f        # View logs
```

---

**End of Summary. Start building! 💪**
