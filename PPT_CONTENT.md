# CampusCompute - Synopsis Presentation
## 12 Slides for Project Synopsis Defense

---

## SLIDE 1: TITLE SLIDE
**CampusCompute: Campus-Aware Cloud Resource Pooling System**

- Team Members: [4 Names + SAP IDs]
- Guide: [Guide Name]
- School of Computer Science
- UPES, Dehradun
- 2026-27

---

## SLIDE 2: PURPOSE OF THE PROJECT (Section 1.1)

**Problem: Massive Resource Wastage**

📊 **Current Lab Utilization:**
- CS Lab: 50 PCs → Only **18% average utilization**
- AI Lab: 40 PCs → Idle evenings/weekends
- Network Lab: 30 PCs → Underutilized off-hours

**Challenges:**
- Expensive infrastructure sitting idle 70%+ of time
- Students lack powerful computing resources at home
- No remote access to campus lab computers
- Wasted investment during nights, weekends, holidays

**Project Purpose:**
Convert idle campus lab computers into shared cloud platform accessible remotely

---

## SLIDE 3: TARGET BENEFICIARY & PROJECT SCOPE (Sections 1.2 & 1.3)

**Target Beneficiaries:**

🎓 **Students:**
- Free computing resources from anywhere
- Work on assignments remotely
- Access powerful lab computers from home

👨‍🏫 **Faculty:**
- Allocate resources for coursework
- Monitor student resource usage
- Manage lab reservations

🏛️ **Institution:**
- Maximize infrastructure ROI (60-70% utilization)
- No additional hardware costs
- Better resource distribution

**Project Scope:**
- ✅ Web-based portal (React frontend)
- ✅ Central broker (Spring Boot backend)
- ✅ Distributed agents on lab PCs (Python + Docker)
- ✅ Authentication & authorization
- ✅ Academic schedule integration
- ✅ Resource monitoring & quotas

---

## SLIDE 4: DESIGN DIAGRAMS - SYSTEM ARCHITECTURE (Section 2.7)

**Three-Layer Architecture**

**📊 USE DIAGRAM: campuscompute_architecture.drawio → Diagram 1 (System Overview)**

**Layer 1 - Client (Browser):**
- React web application
- Accessible from anywhere (home, mobile, cafe)
- Browser-based terminal (xterm.js)
- File manager interface

**Layer 2 - Campus Broker (Central Server):**
- Spring Boot + PostgreSQL + Redis
- JWT authentication
- Adaptive scheduler (academic-aware)
- Container lifecycle manager
- WebSocket gateway

**Layer 3 - Lab Computers (Agents):**
- Python agent + Docker Engine
- Agents connect outbound via mTLS
- Host isolated containers
- No inbound ports (firewall-friendly)

**Communication:** HTTPS/WSS (students) + mTLS (agents)

---

## SLIDE 5: REFERENCE ALGORITHM - ADAPTIVE SCHEDULER (Section 2.1)

**Novel Contribution: Academic-Aware Scheduling** ⭐

**Scoring Formula:**
```
Score(device) = w₁·CPU + w₂·RAM + w₃·Load 
              + w₄·ReservationProximity + w₅·Reliability
```

**Key Innovation: Reservation Proximity Factor**
- Traditional cloud: Only CPU, RAM, network
- CampusCompute adds: **Academic schedule awareness**

**Example Scenario:**
```
Time: 9:40 AM
Request: 6-hour Ubuntu container (2 CPU, 4GB RAM)

CS Lab:  Available now, but class at 10:00 AM ❌
         → Low reservation proximity score

AI Lab:  Available now, free until 6:00 PM ✅
         → High reservation proximity score
         → SELECTED by scheduler
```

**Impact:**
- Prevents classroom disruption
- Better long-term utilization
- Institutional context matters!

---

## SLIDE 7: PROJECT FEATURES (Section 2.4)

**For Students:**
- ✅ On-demand container creation (Ubuntu, Python, Java images)
- ✅ Browser-based terminal with xterm.js
- ✅ Persistent file storage (Docker volumes)
- ✅ Remote access from anywhere globally
- ✅ Resource quotas (fair distribution)

**For Administrators:**
- ✅ Device management dashboard
- ✅ Lab reservation scheduling system
- ✅ User & quota management
- ✅ Real-time resource monitoring
- ✅ Comprehensive audit logs

**System Features:**
- ✅ Multi-layer security (TLS, mTLS, JWT, container isolation)
- ✅ Automatic failure recovery & health checks
- ✅ Academic schedule integration
- ✅ WebSocket-based real-time updates

---

## SLIDE 10: SECURITY REQUIREMENTS (Section 4.2)

**Four-Layer Security Architecture**

**📊 USE DIAGRAM: campuscompute_network_access.drawio → Diagram 3 (Security Layers)**

**Layer 1 - Internet Boundary:**
- TLS 1.3 encryption for all traffic
- JWT-based authentication & authorization
- Campus firewall (only port 443 open)
- Rate limiting & DDoS protection

**Layer 2 - Agent Authentication:**
- Mutual TLS (mTLS) certificates
- Agents connect **outbound only** (firewall-friendly)
- Certificate rotation & revocation
- No inbound ports on lab PCs

**Layer 3 - Container Isolation:**
- CPU/RAM/PID limits enforced by Docker
- Dropped Linux capabilities (no privileged mode)
- Read-only root filesystem
- Network isolation between containers

**Layer 4 - Network Segmentation:**
- DMZ zone: Public-facing broker
- Internal zone: Lab PCs (never internet-exposed)
- Students access globally, but PCs stay secure

---

## SLIDE 8: DESIGN & IMPLEMENTATION CONSTRAINTS (Section 2.6)

**Design Constraints:**

**Technology Stack:**
- Frontend: React 18 + TypeScript + Tailwind CSS
- Backend: Spring Boot 3.2 (Java 17) + PostgreSQL 15
- Agent: Python 3.11 + Docker SDK
- Infrastructure: Docker Engine 24+, Nginx

**Architectural Constraints:**
- Must use existing campus infrastructure
- Agents connect outbound only (firewall compliance)
- Lab PCs never exposed to internet directly
- Must support 100+ concurrent students

**Performance Constraints:**
- Container startup: < 10 seconds (P95)
- Terminal latency: < 100ms (P95)
- System availability: 99%+ uptime

**Security Constraints:**
- TLS 1.3 for all internet traffic
- mTLS for agent-broker communication
- Container resource limits enforced
- No privileged containers allowed

---

## SLIDE 6: SWOT ANALYSIS (Section 2.3)

**Strengths:**
- ✅ Novel academic-aware scheduling algorithm
- ✅ Uses existing infrastructure (zero hardware cost)
- ✅ Browser-based (no client installation)
- ✅ Strong multi-layer security model

**Weaknesses:**
- ⚠️ Depends on lab PC availability
- ⚠️ Requires agent installation on all machines
- ⚠️ Limited to campus resource capacity

**Opportunities:**
- 🚀 Multi-campus federation
- 🚀 GPU support for AI/ML workloads
- 🚀 LMS integration (Moodle/Canvas)
- 🚀 Research publication potential

**Threats:**
- ⚡ Commercial cloud alternatives (AWS Educate, Azure)
- ⚡ Security incidents affecting trust
- ⚡ IT department deployment resistance

---

## SLIDE 9: SOFTWARE INTERFACE (Section 3.2)

**Technology Stack Details:**

**Frontend Technologies:**
- React 18 with TypeScript
- Vite (build tool)
- Tailwind CSS (styling)
- xterm.js (terminal emulator)
- WebSocket client

**Backend Technologies:**
- Spring Boot 3.2 (Java 17)
- Spring Security + JWT
- Spring WebSocket
- PostgreSQL 15 (database)
- Redis 7 (session cache)

**Agent Technologies:**
- Python 3.11
- Docker SDK for Python
- psutil (system monitoring)
- WebSocket client
- systemd integration

**Communication Protocols:**
- HTTPS (client ↔ broker)
- WebSocket Secure (real-time)
- mTLS (agent ↔ broker)
- Docker API (agent ↔ containers)

---

## SLIDE 11: PERFORMANCE REQUIREMENTS (Section 4.1)

**Performance Targets:**

**Latency Requirements:**
- ⚡ Container startup time: **< 10 seconds** (P95 percentile)
- ⚡ Terminal response latency: **< 100ms** (P95 percentile)
- ⚡ API response time: **< 500ms** (P95 percentile)
- ⚡ Agent heartbeat interval: **30 seconds**

**Throughput Requirements:**
- 📊 Support **100+ concurrent users**
- 📊 Handle **500+ containers** across campus
- 📊 Process **10,000+ terminal messages/sec**

**Availability Requirements:**
- 🔒 System uptime: **99%+** (< 7 hours downtime/month)
- 🔒 Broker high availability (active-passive)
- 🔒 Automatic agent reconnection on failure

**Resource Utilization:**
- 📈 Lab utilization: **18% → 60-70%** (3-4x improvement)
- 📈 Per-student quota: 4 CPU cores, 8GB RAM max
- 📈 Container lifetime: 1 hour to 7 days

---

## SLIDE 12: CONCLUSION & Q&A

**Project Summary:**
CampusCompute transforms idle campus lab computers into a cloud platform with academic-aware scheduling

**Key Highlights:**
- ✅ **Problem Solved:** 18% → 60-70% utilization
- ✅ **Novel Contribution:** Academic schedule-aware scheduler
- ✅ **Global Access:** Students work from anywhere securely
- ✅ **Zero Cost:** Uses existing infrastructure
- ✅ **Strong Security:** 4-layer protection model

**Expected Impact:**
- Better student learning experience
- Maximize institutional ROI
- Research publication potential
- Real-world deployment ready

**Future Work:**
- GPU support for AI/ML
- Multi-campus federation
- LMS integration

---

**Questions?**

**Thank You!**

---

# PRESENTATION GUIDE

## Timing (10-12 minutes)
- **Slide 1:** Title (30 sec)
- **Slide 2:** Purpose/Problem (1 min)
- **Slide 3:** Beneficiary & Scope (1 min)
- **Slide 4:** Architecture Diagram (1.5 min) ⭐ **SHOW DIAGRAM**
- **Slide 5:** Novel Algorithm (1.5 min)
- **Slide 6:** SWOT Analysis (1 min)
- **Slide 7:** Features (1 min)
- **Slide 8:** Design Constraints (1 min)
- **Slide 9:** Software Interface (1 min)
- **Slide 10:** Security (1.5 min) ⭐ **SHOW DIAGRAM**
- **Slide 11:** Performance Requirements (1 min)
- **Slide 12:** Conclusion & Q&A (1 min)

**Total: 12 slides | 10-12 minutes**

---

## Key Diagrams to Export (2 Essential)

### Diagram 1: System Architecture
- **Source:** `campuscompute_architecture.drawio` → Diagram 1
- **Use in:** Slide 4
- **Export as:** PNG, 300 DPI
- **Shows:** 3-layer architecture (Client, Broker, Agents)

### Diagram 2: Security Layers
- **Source:** `campuscompute_network_access.drawio` → Diagram 3
- **Use in:** Slide 10
- **Export as:** PNG, 300 DPI
- **Shows:** 4-layer security model

---

## Synopsis Sections Mapped to Slides

✅ **1.1 Purpose** → Slide 2
✅ **1.2 Target Beneficiary** → Slide 3
✅ **1.3 Project Scope** → Slide 3
✅ **2.1 Reference Algorithm** → Slide 5 ⭐ (Novel contribution)
✅ **2.3 SWOT Analysis** → Slide 6
✅ **2.4 Project Features** → Slide 7
✅ **2.6 Design Constraints** → Slide 8
✅ **2.7 Design Diagrams** → Slide 4
✅ **3.2 Software Interface** → Slide 9
✅ **4.1 Performance Requirements** → Slide 11
✅ **4.2 Security Requirements** → Slide 10

**Coverage:** 11 key synopsis sections in 12 slides

---

## Speaking Tips for Synopsis Defense

### Opening (Slides 1-3)
- Start with the problem: "70-80% of lab PCs sit idle"
- Show real numbers: "CS Lab: only 18% utilization"
- Emphasize beneficiaries: students, faculty, institution

### Technical Core (Slides 4-9)
- **Slide 4:** Point to layers in diagram as you explain
- **Slide 5:** Emphasize this is YOUR novel contribution
- **Slide 6:** Be honest about weaknesses, confident about strengths
- **Slide 7-9:** Keep concise, don't read bullets

### Security & Performance (Slides 10-11)
- **Slide 10:** Address concerns: "Students can't hack lab PCs"
- **Slide 11:** Numbers matter: "< 10 seconds, 99% uptime"

### Closing (Slide 12)
- Reinforce impact: "3-4x better utilization, zero cost"
- Show confidence: "Ready for deployment"
- Prepare for Q&A

---

## Common Questions to Prepare

**Q1:** Why not just use AWS/Azure for students?
**A:** Cost, institutional control, privacy, academic integration

**Q2:** What if a student tries to hack other containers?
**A:** Docker isolation, resource limits, dropped capabilities, separate networks

**Q3:** What happens during class hours?
**A:** Reservation system + adaptive scheduler avoids scheduling conflicts

**Q4:** How do you handle agent failures?
**A:** Automatic reconnection, health checks, container migration

**Q5:** What's the novel contribution?
**A:** Academic-aware scheduling with reservation proximity factor

---

## Presentation Checklist

### Before Presentation:
- [ ] Add team member names + SAP IDs to Slide 1
- [ ] Add guide name to Slide 1
- [ ] Export 2 diagrams as PNG (300 DPI)
- [ ] Insert diagrams into Slides 4 and 10
- [ ] Practice timing (aim for 10-11 minutes, leave 1-2 for Q&A)
- [ ] Test PowerPoint on presentation computer
- [ ] Have backup PDF version ready

### During Presentation:
- [ ] Speak clearly, not too fast
- [ ] Make eye contact with panel
- [ ] Point to diagrams when explaining
- [ ] Don't read slides word-for-word
- [ ] Show enthusiasm for the project
- [ ] Handle questions confidently

### Content Guidelines:
- Keep text minimal (max 6-7 bullets per slide)
- Use 24pt+ font size
- Consistent color scheme from diagrams
- Add icons/emojis for visual appeal (already included)
- Avoid animation (for synopsis defense)

---

**END OF PRESENTATION CONTENT**
