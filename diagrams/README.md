# CampusCompute - Technical Diagrams

This folder contains professional draw.io diagrams for the presentation.

## 📊 Available Diagrams

### 1. System Architecture (`1_system_architecture.drawio`)
**Purpose**: High-level system overview  
**Shows**: Frontend, Backend, Agent layers with connections  
**Best For**: Introduction, explaining overall design

### 2. Container Lifecycle (`2_container_lifecycle.drawio`)
**Purpose**: AWS-style state machine  
**Shows**: All container states (PENDING → CREATING → RUNNING → STOPPING → STOPPED → RESTARTING → FAILED → DELETED)  
**Best For**: Explaining state management, intermediate states

### 3. Scheduling Algorithm (`3_scheduling_algorithm.drawio`)
**Purpose**: Adaptive device selection flowchart  
**Shows**: Algorithm steps, scoring factors, example calculation  
**Best For**: Technical deep-dive, explaining novel algorithm

### 4. Container Creation Flow (`4_container_creation_flow.drawio`)
**Purpose**: Sequence diagram for container creation  
**Shows**: Student → Frontend → Backend → Scheduler → Agent → Docker  
**Best For**: Live demo explanation, timing breakdown

### 5. Security Architecture (`5_security_architecture.drawio`)
**Purpose**: Multi-layer security model  
**Shows**: Authentication, Network, Container, Quota layers  
**Best For**: Security discussion, compliance questions

### 6. Deployment Architecture (`6_deployment_architecture.drawio`)
**Purpose**: Production deployment layout  
**Shows**: DMZ, Internal zones, Infrastructure specs, Cost analysis  
**Best For**: Deployment discussion, scalability questions

---

## 🎯 How to Use

### Opening Diagrams

1. **Online (Recommended)**:
   - Go to https://app.diagrams.net/
   - Click `File` → `Open from` → `Device`
   - Select the `.drawio` file
   - Edit and export as needed

2. **Desktop App**:
   - Download draw.io desktop: https://github.com/jgraph/drawio-desktop/releases
   - Open the `.drawio` file directly

### Exporting for Presentation

1. **As PNG (High Quality)**:
   - File → Export as → PNG
   - Scale: 200% (for crisp quality)
   - Transparent background: Optional
   - Border width: 10px

2. **As PDF (Vector)**:
   - File → Export as → PDF
   - Fit to: One page
   - Include: Copy of pages

3. **As SVG (Scalable)**:
   - File → Export as → SVG
   - Embed fonts: Yes
   - Include: Copy of diagram

---

## 📝 Presentation Tips

### Diagram 1: System Architecture
**When to show**: Introduction (5 min mark)  
**What to highlight**:
- Three distinct layers
- Real-time WebSocket communication
- Multiple lab computers

**Script**:
> "Here's our system architecture. We have a React frontend for users, Spring Boot backend as the broker, and Python agents running on each lab computer. Notice the WebSocket connections - this enables real-time communication between backend and agents."

---

### Diagram 2: Container Lifecycle
**When to show**: After live demo (15 min mark)  
**What to highlight**:
- Intermediate states (STOPPING, RESTARTING)
- AWS-style design
- Agent confirmation flow

**Script**:
> "This is our container state machine. Notice the intermediate states - when you click Stop, it shows STOPPING until the agent confirms the container is actually stopped. This is similar to how AWS EC2 works - states reflect reality, not intentions."

---

### Diagram 3: Scheduling Algorithm
**When to show**: Technical deep-dive (17 min mark)  
**What to highlight**:
- Five scoring factors
- Weighted calculation
- Example with two devices

**Script**:
> "Our adaptive scheduling algorithm considers five factors: CPU availability, RAM availability, current CPU load, RAM load, and reliability score. Each factor is weighted. In this example, Device 2 scores higher (0.655 vs 0.545) because it has more available resources and lower current load."

---

### Diagram 4: Container Creation Flow
**When to show**: During or after live demo  
**What to highlight**:
- Total time (10-13 seconds)
- Each component's role
- WebSocket confirmations

**Script**:
> "Let me walk through what just happened in the demo. When you clicked Create, the frontend sent a request to the backend. Backend created a PENDING container, checked your quota, called the scheduler to pick the best device, updated to CREATING, sent a WebSocket message to the agent, which pulled the image and created the container. The agent confirmed back, and status changed to RUNNING. Total time: about 10 seconds."

---

### Diagram 5: Security Architecture
**When to show**: When asked about security  
**What to highlight**:
- Four security layers
- Multiple enforcement points
- Container isolation

**Script**:
> "Security is multi-layered. Layer 1: JWT authentication and RBAC. Layer 2: HTTPS and WebSocket encryption. Layer 3: Docker container isolation with resource limits. Layer 4: Quota enforcement and audit logging. Every request passes through multiple security checks."

---

### Diagram 6: Deployment Architecture
**When to show**: When asked about scalability/production  
**What to highlight**:
- Load balanced frontend/backend
- Database replication
- 150 lab computers
- Cost savings (93%)

**Script**:
> "In production, we'd deploy with load-balanced frontend and backend servers, database replication for high availability, and Redis cluster for sessions. With 150 lab computers, that's 900+ CPU cores and 1.5TB RAM available to students. The infrastructure costs about ₹80,000 per year - that's 93% cheaper than AWS for 200 students, which would cost ₹12 lakhs annually."

---

## 🎨 Diagram Style Guide

All diagrams follow consistent styling:

### Colors
- **Blue** (#dae8fc): Frontend, User-facing
- **Green** (#d5e8d4): Backend, Success states
- **Yellow** (#fff2cc): Warning, Intermediate states
- **Orange** (#ffe6cc): Agent, Processing
- **Purple** (#e1d5e7): Services, Components
- **Red** (#f8cecc): Database, Error states

### Symbols
- **Rectangles**: Services, Servers, Components
- **Rounded Rectangles**: Processes, Functions
- **Ellipses**: States, Endpoints
- **Cylinders**: Databases, Storage
- **Clouds**: Internet, External
- **Diamonds**: Decisions, Conditionals

### Text
- **Titles**: 24pt, Bold
- **Labels**: 12-14pt, Bold
- **Details**: 9-10pt, Regular
- **Code/Tech**: Monospace when applicable

---

## 🔄 Updating Diagrams

If you need to modify diagrams:

1. Open in draw.io
2. Make changes
3. Export as PNG/PDF for presentation
4. Save `.drawio` file for future edits
5. Update this README if adding new diagrams

---

## 📦 Backup

All diagrams are also backed up in Git. To restore:

```bash
git checkout HEAD diagrams/
```

---

## ✅ Pre-Presentation Checklist

- [ ] All 6 diagrams open correctly in draw.io
- [ ] Diagrams exported as PNG (200% scale)
- [ ] PDFs exported for printing (if needed)
- [ ] Practiced explaining each diagram
- [ ] Know which diagram to show when
- [ ] Have backup copies on USB drive
- [ ] Diagrams loaded on presentation laptop

---

**Created**: October 5, 2026  
**Last Updated**: October 5, 2026  
**Status**: ✅ Ready for Presentation
