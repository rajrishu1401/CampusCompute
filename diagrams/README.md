# CampusCompute - Technical Diagrams

This folder contains **11 professional draw.io diagrams** for the presentation and SRS documentation.

## 📊 Available Diagrams

### 1. System Architecture (`1_system_architecture.drawio`)
**Purpose**: High-level system overview  
**Shows**: Frontend, Backend, Agent layers with connections  
**Best For**: Introduction, explaining overall design  
**SRS Section**: 2.7 Design Diagrams

### 2. Container Lifecycle (`2_container_lifecycle.drawio`)
**Purpose**: AWS-style state machine  
**Shows**: All container states (PENDING → CREATING → RUNNING → STOPPING → STOPPED → RESTARTING → FAILED → DELETED)  
**Best For**: Explaining state management, intermediate states  
**SRS Section**: 2.7 Design Diagrams (State Diagram) - REQUIRED

### 3. Scheduling Algorithm (`3_scheduling_algorithm.drawio`)
**Purpose**: Adaptive device selection flowchart  
**Shows**: Algorithm steps, scoring factors, example calculation  
**Best For**: Technical deep-dive, explaining novel algorithm  
**SRS Section**: 2.1 Reference Algorithm

### 4. Container Creation Flow (`4_container_creation_flow.drawio`)
**Purpose**: Sequence diagram for container creation  
**Shows**: Student → Frontend → Backend → Scheduler → Agent → Docker  
**Best For**: Live demo explanation, timing breakdown  
**SRS Section**: 2.7 Design Diagrams (Activity/Sequence) - REQUIRED

### 5. Security Architecture (`5_security_architecture.drawio`)
**Purpose**: Multi-layer security model  
**Shows**: Authentication, Network, Container, Quota layers  
**Best For**: Security discussion, compliance questions  
**SRS Section**: 4.2 Security Requirements

### 6. Deployment Architecture (`6_deployment_architecture.drawio`)
**Purpose**: Production deployment layout  
**Shows**: DMZ, Internal zones, Infrastructure specs, Cost analysis  
**Best For**: Deployment discussion, scalability questions  
**SRS Section**: 2.7 Design Diagrams (Deployment) - REQUIRED for Major Project

### 7. IPC Communication (`7_ipc_communication.drawio`) ⭐ NEW
**Purpose**: Inter-Process Communication architecture  
**Shows**: 6 processes (Browser, Backend JVM, Agent Python, PostgreSQL, Docker Engine, Containers) with all IPC mechanisms  
**Details**: HTTP/REST, WebSocket, JDBC, Docker API, Unix Socket/Named Pipe, In-process calls  
**Metrics**: <50ms WebSocket latency, <100ms REST API, 1000 req/min throughput  
**Best For**: PPT presentation, technical architecture deep-dive  
**SRS Section**: 2.7 Design Diagrams, 3.2 Software Interface

### 8. Use Case Diagram (`8_use_case_diagram.drawio`) ⭐ NEW
**Purpose**: Level 2 UML Use Case Diagram  
**Shows**: 4 actors (Student, Faculty, Admin, Agent) with 20 use cases  
**Details**: Complete system functionality with include relationships  
**Best For**: Requirements overview, functionality demonstration  
**SRS Section**: 2.7 Design Diagrams (Use Case Level 2) - REQUIRED

### 9. Class Diagram (`9_class_diagram.drawio`) ⭐ NEW
**Purpose**: Domain model showing entities and relationships  
**Shows**: 7 main classes (Organization, User, Lab, Device, Container, EnrollmentToken, Reservation)  
**Details**: Attributes, methods, relationships (composition, references)  
**Best For**: Database design, OOP structure understanding  
**SRS Section**: 2.7 Design Diagrams (Class Diagram) - REQUIRED

### 10. Data Flow Diagram (`10_data_flow_diagram.drawio`) ⭐ NEW
**Purpose**: DFD Level 0 (Context Diagram)  
**Shows**: External entities and 8 bidirectional data flows with central system  
**Details**: Color-coded flows (Input, Output, Commands, Metrics)  
**Best For**: System boundary definition, data flow analysis  
**SRS Section**: 2.7 Design Diagrams (Data Flow Diagram) - REQUIRED

### 11. Collaboration Diagram (`11_collaboration_diagram.drawio`) ⭐ NEW
**Purpose**: UML Collaboration Diagram for Container Creation scenario  
**Shows**: 11 objects with 15 numbered messages in sequence  
**Details**: Complete interaction flow from request to container creation  
**Best For**: Detailed flow explanation, object interaction understanding  
**SRS Section**: 2.7 Design Diagrams (Collaboration) - REQUIRED for Major Project

---

## ✅ SRS Compliance Status

All **REQUIRED** diagrams for UPES SRS template (Section 2.7) are complete:

- ✅ Use Case Diagram (Level 2)
- ✅ Class Diagram
- ✅ Activity Diagram
- ✅ Sequence Diagram
- ✅ Data Flow Diagram
- ✅ State Diagram
- ✅ Collaboration Diagram (Major Project)
- ✅ Deployment Diagram (Major Project)

**Bonus diagrams**: System Architecture, Scheduling Algorithm, Security Architecture, IPC Communication

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

3. **VS Code Extension**:
   - Install "Draw.io Integration" extension
   - Right-click `.drawio` file → Open With → Draw.io Editor

### Exporting for Presentation

1. **As PNG (High Quality for PPT)**:
   - File → Export as → PNG
   - Scale: 200% (for crisp quality)
   - Transparent background: Optional
   - Border width: 10px
   - Width: 3000px (for large screens)

2. **As PDF (Vector for LaTeX)**:
   - File → Export as → PDF
   - Fit to: One page
   - Include: Copy of pages

3. **As SVG (Scalable)**:
   - File → Export as → SVG
   - Embed fonts: Yes
   - Include: Copy of diagram

---

## 📝 Presentation Tips

### Opening Sequence (0-5 min):
1. **System Architecture** - Big picture overview
2. **IPC Communication** - Technical architecture depth

### Functionality Demo (5-15 min):
3. **Use Case Diagram** - What the system can do
4. **Container Creation Flow** - Live demo walkthrough
5. **Container Lifecycle** - State management explanation

### Technical Deep-Dive (15-20 min):
6. **Class Diagram** - Data model and entities
7. **Collaboration Diagram** - Object interactions
8. **Scheduling Algorithm** - Intelligent resource allocation

### Closing (20-25 min):
9. **Security Architecture** - Multi-layer security
10. **Deployment Architecture** - Production setup + cost savings
11. **Data Flow Diagram** - System boundaries (if time permits)

---

### Script for IPC Communication Diagram (NEW):

**When to show**: Technical architecture section (3-5 min mark)  
**What to highlight**:
- 6 different processes communicating
- Multiple IPC mechanisms (HTTP/REST, WebSocket, JDBC, Docker API, Unix Socket)
- Performance metrics (<50ms latency)
- Process boundaries

**Script**:
> "Let me show you how different components communicate in our system. We have 6 processes: the browser running React, backend JVM running Spring Boot, Python agent process, PostgreSQL database, Docker Engine, and the containers themselves. They use different IPC mechanisms - HTTP REST for API calls, WebSocket for real-time updates, JDBC for database queries, and Docker API over Unix sockets. The system achieves sub-50ms WebSocket latency and handles 1000 API requests per minute with 500 concurrent WebSocket connections."

---

### Script for Use Case Diagram (NEW):

**When to show**: After introduction (5-7 min mark)  
**What to highlight**:
- 4 types of users
- 20 use cases covering all functionality
- Include relationships

**Script**:
> "This is our complete use case diagram showing what different users can do. Students can create, manage, and access containers through a web terminal. Faculty can view student activity and reserve lab resources. Admins handle user management, device management, and system configuration. And the agent system automatically reports metrics, executes commands, and sends heartbeats. Notice the include relationships - when you create a container, it automatically includes the 'Execute Container Commands' use case on the agent side."

---

### Script for Class Diagram (NEW):

**When to show**: Technical Q&A or database design discussion  
**What to highlight**:
- 7 main entities
- Relationships (1-to-many, references)
- Key attributes

**Script**:
> "Here's our domain model. At the top, we have Organization which can have multiple Users and Labs. Each Lab has multiple Devices, and each Device can run multiple Containers. Users create Containers. We also have EnrollmentTokens for one-time agent registration and Reservations for faculty to book lab time. The composition relationships show ownership - an Organization owns its Users and Labs. The reference relationships show foreign keys in the database."

---

### Script for Collaboration Diagram (NEW):

**When to show**: Detailed technical explanation after demo  
**What to highlight**:
- 15 numbered messages
- Complete interaction sequence
- Object collaboration

**Script**:
> "This collaboration diagram shows exactly what happens when a student creates a container. Step 1: Student submits request. Step 2: Frontend POSTs to backend. Step 3: Controller calls Service. Step 4: Service asks Scheduler to find best device. Steps 5-6: Scheduler queries database for online devices. Step 7: Calculates scores. Step 8-9: Service saves container to database. Steps 10-11: WebSocket sends command to Agent. Steps 12-13: Agent tells Docker to create container. Step 14: Agent confirms success. Step 15: Frontend receives confirmation. All in 10 seconds."

---

## 🎨 Diagram Style Guide

All diagrams follow consistent styling:

### Colors
- **Blue** (#dae8fc/#6c8ebf): Frontend, Student, User-facing
- **Green** (#d5e8d4/#82b366): Backend, Faculty, Success states
- **Yellow** (#fff2cc/#FFD700): Warning, Intermediate states
- **Orange** (#ffe6cc/#d79b00): Agent, Admin, Processing
- **Purple** (#e1d5e7/#9673a6): Services, Docker, WebSocket
- **Red** (#f8cecc/#b85450): Database, Error states
- **Gray** (#f5f5f5/#666666): System boundaries, Processes

### Symbols (UML Standard)
- **Rectangles**: Classes, Services, Servers, External Entities
- **Rounded Rectangles**: Processes, Use Cases
- **Ellipses**: States, System Processes (DFD)
- **Cylinders**: Databases, Storage
- **Clouds**: Internet, External networks
- **Diamonds**: Decisions, Conditionals
- **Actors**: Stick figures for human users

### Text
- **Titles**: 24pt, Bold
- **Section Headers**: 14pt, Bold
- **Labels**: 11-12pt, Bold
- **Details**: 9-10pt, Regular
- **Code/Tech**: Monospace when applicable

---

## 🔄 Updating Diagrams

If you need to modify diagrams:

1. Open in draw.io
2. Make changes (maintain consistent style)
3. Export as PNG (200% scale) and PDF for documentation
4. Save `.drawio` file for future edits
5. Update this README if adding new diagrams
6. Commit to Git with descriptive message

---

## 📦 Backup

All diagrams are version-controlled in Git. To restore:

```bash
git checkout HEAD diagrams/
```

Or restore specific diagram:
```bash
git checkout HEAD diagrams/8_use_case_diagram.drawio
```

---

## 📄 Exporting for LaTeX SRS

Include diagrams in LaTeX document:

```latex
\begin{figure}[h]
\centering
\includegraphics[width=\textwidth]{diagrams/8_use_case_diagram.pdf}
\caption{Use Case Diagram - CampusCompute System}
\label{fig:usecase}
\end{figure}
```

For better quality, export as PDF (vector format) rather than PNG.

---

## ✅ Pre-Presentation Checklist

- [ ] All 11 diagrams open correctly in draw.io
- [ ] Diagrams exported as PNG (200% scale, 3000px width)
- [ ] PDFs exported for LaTeX SRS document
- [ ] Practiced explaining each diagram
- [ ] Know which diagram to show when
- [ ] Prepared scripts for new diagrams (7, 8, 9, 10, 11)
- [ ] Have backup copies on USB drive
- [ ] Diagrams loaded on presentation laptop
- [ ] SRS LaTeX document includes all required diagrams

---

## 📊 Diagram Count Summary

**Total Diagrams**: 11  
**SRS Required**: 8 (all complete ✅)  
**Bonus Diagrams**: 3 (Architecture, Algorithm, Security)  
**New Diagrams**: 5 (IPC, Use Case, Class, DFD, Collaboration)  

---

**Created**: October 5, 2026  
**Last Updated**: October 6, 2026  
**Status**: ✅ Complete - Ready for Presentation & SRS Submission

All diagrams are professional-quality, SRS-compliant, and ready for use in:
- PowerPoint presentations
- LaTeX SRS document
- Technical interviews
- GitHub documentation
- Printed materials
