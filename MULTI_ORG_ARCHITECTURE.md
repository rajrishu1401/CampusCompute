# Multi-Organization Architecture Design

## Overview
Transform CampusCompute into a multi-tenant platform where multiple organizations (colleges, universities) can register, manage their infrastructure, and provide container access to their students.

---

## User Roles

### 1. Root/Super Admin (Future)
- Platform administrator
- Can view all organizations
- Manage platform-wide settings
- Monitor system health

### 2. Organization Admin
- Register organization
- Add/manage lab machines (install agents)
- Define student list (bulk upload)
- View organization statistics
- Manage quotas and policies

### 3. Student
- Login with organization credentials
- Create/manage containers (within quota)
- Access terminal
- View usage statistics

---

## Database Schema Updates

### New Tables

#### `organizations` table
```sql
CREATE TABLE organizations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,  -- e.g., 'UPES', 'MIT'
    domain VARCHAR(100),  -- e.g., 'upes.ac.in'
    contact_email VARCHAR(255) NOT NULL,
    contact_phone VARCHAR(20),
    address TEXT,
    logo_url VARCHAR(500),
    active BOOLEAN DEFAULT true,
    subscription_tier VARCHAR(50) DEFAULT 'FREE',  -- FREE, BASIC, PREMIUM
    max_devices INT DEFAULT 10,
    max_students INT DEFAULT 100,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### Update `users` table
```sql
ALTER TABLE users ADD COLUMN organization_id BIGINT REFERENCES organizations(id);
ALTER TABLE users ADD COLUMN user_type VARCHAR(20) DEFAULT 'STUDENT';  -- ORG_ADMIN, STUDENT
ALTER TABLE users ADD COLUMN student_id VARCHAR(50);  -- Roll number
ALTER TABLE users ADD COLUMN approved BOOLEAN DEFAULT false;  -- For first-time login
```

#### Update `devices` table
```sql
ALTER TABLE devices ADD COLUMN organization_id BIGINT REFERENCES organizations(id);
```

#### Update `labs` table
```sql
ALTER TABLE labs ADD COLUMN organization_id BIGINT REFERENCES organizations(id);
```

#### `student_uploads` table (for bulk student registration)
```sql
CREATE TABLE student_uploads (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT REFERENCES organizations(id),
    uploaded_by BIGINT REFERENCES users(id),
    file_name VARCHAR(255),
    total_students INT,
    processed_students INT,
    failed_students INT,
    status VARCHAR(20),  -- PROCESSING, COMPLETED, FAILED
    error_log TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## Registration & Onboarding Flow

### Organization Registration

```
1. Org Admin visits platform
   ↓
2. Click "Register Organization"
   ↓
3. Fill form:
   - Organization name
   - Code (unique identifier)
   - Domain (for email validation)
   - Contact details
   ↓
4. Email verification
   ↓
5. Create org admin account
   ↓
6. Dashboard access granted
```

### Machine/Agent Setup

```
1. Org Admin navigates to "Devices" section
   ↓
2. Click "Add New Device"
   ↓
3. System generates:
   - Device enrollment token
   - Installation script
   ↓
4. Admin downloads install script
   ↓
5. Admin runs script on lab machine:
   $ wget https://campuscompute.io/install.sh
   $ sudo bash install.sh <enrollment-token>
   ↓
6. Script does:
   - Install Docker (if not present)
   - Configure Docker security
   - Install Python agent
   - Configure agent with token
   - Start agent service
   - Register with backend
   ↓
7. Device appears in admin dashboard
```

### Student Registration

```
1. Org Admin navigates to "Students" section
   ↓
2. Upload CSV file with columns:
   - student_id (roll number)
   - email
   - full_name
   - department (optional)
   ↓
3. System validates:
   - Email format (matches org domain)
   - Duplicate check
   - Within org student limit
   ↓
4. Students added to database (approved=false)
   ↓
5. Students receive email with:
   - Login URL
   - Temporary password / first-time setup link
```

### Student First Login

```
1. Student visits platform
   ↓
2. Enter student_id + email
   ↓
3. System checks:
   - Student exists in org
   - approved=false (first time)
   ↓
4. Prompt to set password
   ↓
5. Set approved=true
   ↓
6. Redirect to dashboard
```

---

## Updated User Workflows

### Organization Admin Workflow

```
Dashboard
├── Overview
│   ├── Total devices (online/offline)
│   ├── Total students
│   ├── Active containers
│   └── Resource utilization
│
├── Devices
│   ├── List all devices
│   ├── Add new device (get install script)
│   ├── View device details
│   └── Remove device
│
├── Students
│   ├── Upload student list (CSV)
│   ├── View all students
│   ├── Individual student details
│   ├── Approve/reject students
│   └── Reset student password
│
├── Labs
│   ├── Create labs
│   ├── Assign devices to labs
│   └── Schedule reservations
│
├── Settings
│   ├── Organization profile
│   ├── Default quotas
│   ├── Policies
│   └── Notifications
│
└── Reports
    ├── Usage statistics
    ├── Student activity
    └── Resource trends
```

### Student Workflow

```
Dashboard
├── My Containers
│   ├── Create new container
│   ├── List my containers
│   ├── Start/stop/delete
│   └── Access terminal
│
├── Resources
│   ├── Current usage
│   ├── Available quota
│   └── History
│
└── Profile
    ├── Personal info
    ├── Change password
    └── Activity log
```

---

## API Endpoints (New/Updated)

### Organization Management

```
POST   /api/organizations/register          # Public - Register new org
GET    /api/organizations/me                # Get current org details
PUT    /api/organizations/me                # Update org details
GET    /api/organizations/stats             # Get org statistics
```

### Device Management (Org Admin)

```
POST   /api/organizations/devices/token     # Generate enrollment token
GET    /api/organizations/devices           # List org devices
GET    /api/organizations/devices/{id}      # Device details
DELETE /api/organizations/devices/{id}      # Remove device
```

### Student Management (Org Admin)

```
POST   /api/organizations/students/upload   # Upload CSV
GET    /api/organizations/students          # List students
GET    /api/organizations/students/{id}     # Student details
PUT    /api/organizations/students/{id}     # Update student
DELETE /api/organizations/students/{id}     # Remove student
POST   /api/organizations/students/{id}/approve  # Approve student
POST   /api/organizations/students/{id}/reset-password  # Reset password
```

### Authentication (Updated)

```
POST   /api/auth/register-org               # Register org admin
POST   /api/auth/register-student           # Student first-time setup
POST   /api/auth/login                      # Login (checks org context)
POST   /api/auth/reset-password             # Password reset
```

---

## Security & Isolation

### Data Isolation
- Each query filters by `organization_id`
- Students can only see their own containers
- Org admins can only see their org's data
- Cross-org data access prevented at DB level

### Device Security
- Each device has unique enrollment token
- Token expires after first use
- Device can only register to one org
- mTLS authentication (future)

### Student Access Control
- Students can only access containers they created
- Cannot see other students' containers
- Quotas enforced per student
- API access rate-limited

---

## Configuration Files

### Agent Installation Script (`install.sh`)

```bash
#!/bin/bash
# CampusCompute Agent Installer
# Usage: sudo bash install.sh <enrollment-token>

ENROLLMENT_TOKEN=$1
BACKEND_URL="https://api.campuscompute.io"

# Check if running as root
if [ "$EUID" -ne 0 ]; then
    echo "Please run as root"
    exit 1
fi

# Install Docker
if ! command -v docker &> /dev/null; then
    echo "Installing Docker..."
    curl -fsSL https://get.docker.com | sh
    systemctl enable docker
    systemctl start docker
fi

# Install Python
apt-get update
apt-get install -y python3 python3-pip

# Download agent
wget ${BACKEND_URL}/download/agent.tar.gz
tar -xzf agent.tar.gz -C /opt/campuscompute

# Install dependencies
cd /opt/campuscompute/agent
pip3 install -r requirements.txt

# Configure agent
cat > config.yaml <<EOF
broker:
  url: "wss://api.campuscompute.io/ws/agent"
  enrollment_token: "${ENROLLMENT_TOKEN}"

docker:
  socket: "unix:///var/run/docker.sock"

logging:
  level: "INFO"
  file: "/var/log/campuscompute/agent.log"
EOF

# Create systemd service
cat > /etc/systemd/system/campuscompute-agent.service <<EOF
[Unit]
Description=CampusCompute Agent
After=docker.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/campuscompute/agent
ExecStart=/usr/bin/python3 /opt/campuscompute/agent/src/main.py
Restart=always

[Install]
WantedBy=multi-user.target
EOF

# Start service
systemctl daemon-reload
systemctl enable campuscompute-agent
systemctl start campuscompute-agent

echo "CampusCompute Agent installed successfully!"
echo "Device should appear in dashboard shortly."
```

---

## Frontend Structure

```
frontend/
├── public/
│   ├── index.html
│   └── assets/
│
├── src/
│   ├── App.jsx
│   ├── main.jsx
│   │
│   ├── pages/
│   │   ├── Landing.jsx              # Public landing page
│   │   ├── RegisterOrg.jsx          # Org registration
│   │   ├── Login.jsx                # Universal login
│   │   ├── StudentSetup.jsx         # First-time password setup
│   │   │
│   │   ├── admin/                   # Org Admin pages
│   │   │   ├── Dashboard.jsx
│   │   │   ├── Devices.jsx
│   │   │   ├── DeviceAdd.jsx
│   │   │   ├── Students.jsx
│   │   │   ├── StudentUpload.jsx
│   │   │   ├── Labs.jsx
│   │   │   └── Settings.jsx
│   │   │
│   │   └── student/                 # Student pages
│   │       ├── Dashboard.jsx
│   │       ├── Containers.jsx
│   │       ├── ContainerCreate.jsx
│   │       ├── Terminal.jsx
│   │       └── Profile.jsx
│   │
│   ├── components/
│   │   ├── Navbar.jsx
│   │   ├── Sidebar.jsx
│   │   ├── ContainerCard.jsx
│   │   ├── DeviceCard.jsx
│   │   ├── StudentTable.jsx
│   │   └── Terminal.jsx             # xterm.js component
│   │
│   ├── services/
│   │   ├── api.js                   # Axios instance
│   │   ├── auth.js
│   │   ├── containers.js
│   │   ├── devices.js
│   │   ├── students.js
│   │   └── websocket.js
│   │
│   ├── store/                       # State management
│   │   ├── authSlice.js
│   │   ├── containerSlice.js
│   │   └── store.js
│   │
│   └── utils/
│       ├── constants.js
│       ├── validators.js
│       └── formatters.js
│
└── package.json
```

---

## Implementation Roadmap

### Phase 1: Database & Backend (Week 1-2)
- [ ] Create Organization entity
- [ ] Update User entity with org_id and user_type
- [ ] Update Device/Lab entities with org_id
- [ ] Create OrganizationService
- [ ] Create enrollment token system
- [ ] Update AuthController for org context
- [ ] Add org-level filtering to all queries

### Phase 2: Student Management (Week 2-3)
- [ ] CSV upload endpoint
- [ ] Student bulk registration
- [ ] First-time login flow
- [ ] Email notifications
- [ ] Student approval system

### Phase 3: Frontend Foundation (Week 3-4)
- [ ] Setup React + Vite
- [ ] Landing page
- [ ] Org registration form
- [ ] Login page (universal)
- [ ] Routing & auth guards

### Phase 4: Admin Dashboard (Week 4-5)
- [ ] Admin dashboard layout
- [ ] Device management UI
- [ ] Student management UI
- [ ] CSV upload interface
- [ ] Statistics & charts

### Phase 5: Student Dashboard (Week 5-6)
- [ ] Student dashboard layout
- [ ] Container list/create UI
- [ ] Terminal component (xterm.js)
- [ ] Resource usage display
- [ ] Profile management

### Phase 6: Polish & Deploy (Week 6-7)
- [ ] Error handling
- [ ] Loading states
- [ ] Responsive design
- [ ] Testing
- [ ] Documentation
- [ ] Deployment setup

---

## Technology Stack

### Frontend
- **Framework**: React 18 + Vite
- **Routing**: React Router v6
- **State**: Redux Toolkit
- **UI Library**: Material-UI (MUI) or Ant Design
- **Terminal**: xterm.js
- **Charts**: Recharts
- **HTTP**: Axios
- **WebSocket**: native WebSocket API

### Backend
- **Framework**: Spring Boot 3.2
- **Database**: PostgreSQL 15
- **Cache**: Redis 7
- **WebSocket**: Spring WebSocket
- **Auth**: JWT (JJWT)
- **Security**: Spring Security

### Agent
- **Language**: Python 3.11+
- **Docker SDK**: docker-py
- **WebSocket**: websockets
- **Async**: asyncio

---

## Next Steps

1. **Review & Approve Architecture**
2. **Create Git branch**: `feature/multi-org`
3. **Start Phase 1**: Database schema updates
4. **Implement Organization entity & service**
5. **Update authentication flow**
6. **Begin frontend scaffold**

---

**Created**: 2026-09-27  
**Version**: 1.0  
**Status**: Design Phase
