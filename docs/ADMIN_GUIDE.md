# CampusCompute - Administrator Guide

**Version**: 1.0  
**Last Updated**: October 3, 2026  
**Audience**: Organization Administrators  

---

## Table of Contents

1. [Introduction](#introduction)
2. [Getting Started](#getting-started)
3. [Organization Setup](#organization-setup)
4. [Dashboard Overview](#dashboard-overview)
5. [Managing Devices](#managing-devices)
6. [Managing Students](#managing-students)
7. [Monitoring & Analytics](#monitoring--analytics)
8. [Troubleshooting](#troubleshooting)
9. [Best Practices](#best-practices)

---

## Introduction

Welcome to CampusCompute! As an administrator, you have full control over your organization's cloud resource pool. This guide will help you:

- Set up your organization
- Add lab computers to the resource pool
- Manage student accounts
- Monitor resource utilization
- Troubleshoot common issues

### What You Can Do

✅ Register your organization  
✅ Enroll lab computers as compute resources  
✅ Upload and manage student accounts  
✅ Monitor system usage and performance  
✅ View real-time statistics and analytics  
✅ Control resource quotas and policies  

---

## Getting Started

### Prerequisites

Before you begin, ensure you have:
- ✅ Access to the CampusCompute web portal
- ✅ Lab computers with Docker installed
- ✅ Student email list (for bulk upload)
- ✅ Organization details (name, code)

### System Requirements

**For Admin Portal:**
- Modern web browser (Chrome, Firefox, Edge, Safari)
- Internet connection
- Screen resolution: 1280x720 or higher

**For Lab Computers:**
- Windows 10/11, Ubuntu 20.04+, or macOS 10.15+
- Docker Desktop (Windows/Mac) or Docker Engine (Linux)
- 4GB+ RAM recommended
- Python 3.8+ (for agent)

---

## Organization Setup

### Step 1: Register Your Organization

1. **Visit the CampusCompute Portal**
   ```
   http://your-campuscompute-url.com
   ```

2. **Click "Register Organization"**
   - Located on the landing page

3. **Fill in Organization Details**
   - **Organization Name**: Your institution's full name
     - Example: `University of Petroleum and Energy Studies`
   - **Organization Code**: Unique identifier (alphanumeric)
     - Example: `UPES2026`
     - Must be unique across the platform
   - **Admin Full Name**: Your name
     - Example: `Dr. John Smith`
   - **Admin Email**: Your official email
     - Example: `admin@upes.ac.in`
     - Must be valid - you'll use this to login
   - **Password**: Strong password (min 8 characters)
     - Must include: uppercase, lowercase, number, special char
     - Example: `MyP@ssw0rd123`

4. **Submit the Form**
   - Wait for success confirmation
   - You'll be redirected to the login page

5. **Login**
   - Use your email and password
   - You'll see the admin dashboard

**✅ Success Indicator**: You see the dashboard with your organization stats

---

## Dashboard Overview

### Main Dashboard Components

When you first login, you'll see:

```
┌─────────────────────────────────────────────────────┐
│  CampusCompute - UPES Dashboard                    │
├─────────────────────────────────────────────────────┤
│                                                      │
│  📊 Statistics Cards                                │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐         │
│  │ Devices  │  │ Students │  │Containers│         │
│  │    5     │  │   120    │  │    45    │         │
│  └──────────┘  └──────────┘  └──────────┘         │
│                                                      │
│  📈 Recent Activity                                 │
│  • Device LAB-01 came online (2 min ago)           │
│  • Student john@edu created container (5 min ago)  │
│                                                      │
│  💻 Device Status Overview                          │
│  Online: 4 | Offline: 1 | Busy: 2                  │
│                                                      │
└─────────────────────────────────────────────────────┘
```

### Statistics Cards

**1. Total Devices**
- Shows number of enrolled lab computers
- Click to view device details

**2. Total Students**
- Shows number of registered students
- Click to manage students

**3. Active Containers**
- Shows running containers across all devices
- Click to view container list

**4. Resource Utilization**
- CPU usage across all devices
- RAM usage
- Disk usage

### Navigation Sidebar

- **Dashboard**: Overview and statistics
- **Devices**: Manage lab computers
- **Students**: Manage student accounts
- **Settings**: Organization settings (coming soon)
- **Logout**: Sign out

---

## Managing Devices

### Overview

Devices are lab computers that you enroll into your resource pool. Each device runs a CampusCompute agent that:
- Communicates with the backend
- Manages Docker containers
- Reports system metrics
- Handles container lifecycle

### Adding a New Device

#### Method 1: Using Windows Installer (Recommended)

**Step 1: Generate Enrollment Token**

1. Go to **Devices** tab in admin panel
2. Click **"ADD DEVICE"** button (blue, top right)
3. A dialog appears with:
   ```
   Enrollment Token: eyJhbGc...xyz123
   Organization ID: 1
   Organization Code: UPES2026
   Expires At: 2026-10-04 20:30:00
   ```
4. Copy the enrollment token

**Step 2: Download Installer**

1. Download `CampusCompute_Agent_Installer.exe`
2. Transfer to the lab computer (USB, network share, etc.)

**Step 3: Run Installer on Lab Computer**

1. Double-click the installer
2. Fill in the form:
   - **Enrollment Token**: Paste the copied token
   - **Device Name**: Friendly name (e.g., "Lab 3 - PC 15")
   - **Backend URL**: Your CampusCompute server URL
3. Click **"Install"**
4. Wait for completion (2-5 minutes)
   - Installs dependencies if needed
   - Configures agent
   - Starts Windows service
   - Registers with backend

**Step 4: Verify in Admin Panel**

1. Return to admin panel
2. Refresh the page
3. New device should appear in the list with:
   - **Status**: 🟢 ONLINE
   - **CPU**: Available cores
   - **RAM**: Available memory
   - **Last Heartbeat**: Just now

**✅ Success**: Device shows as ONLINE within 30 seconds

#### Method 2: Manual Configuration

**If you prefer manual setup or need Linux/Mac support:**

**Step 1: Generate Token** (same as above)

**Step 2: Install Agent on Lab Computer**

```bash
# Navigate to agent directory
cd /path/to/campuscompute/agent

# Install dependencies
pip install -r requirements.txt

# Edit configuration
nano config.yaml
```

**Step 3: Configure Agent**

Edit `config.yaml`:

```yaml
broker:
  url: "ws://your-backend-url:8081/ws/agent"
  enrollment_token: "PASTE-YOUR-TOKEN-HERE"
  organization_id: 1

device:
  device_id: "LAB-03-PC-15"
  hostname: "Lab 3 - Computer 15"

docker:
  socket: "unix:///var/run/docker.sock"  # Linux/Mac
  # socket: "npipe:////./pipe/docker_engine"  # Windows

heartbeat_interval: 30
```

**Step 4: Start Agent**

```bash
# Start manually (for testing)
python src/main.py

# Or create systemd service (Linux)
sudo nano /etc/systemd/system/campuscompute-agent.service
```

**Step 5: Verify** (same as above)

### Understanding Device Status

| Status | Color | Meaning | Action Required |
|--------|-------|---------|-----------------|
| ONLINE | 🟢 Green | Device is connected and available | None |
| OFFLINE | 🔴 Red | Device is disconnected | Check network, restart agent |
| BUSY | 🟡 Yellow | Device is at capacity | Add more devices or wait |
| MAINTENANCE | 🔵 Blue | Manually marked for maintenance | None (planned) |

### Device Details

Click on any device to see:

**System Information:**
- Hostname
- Device ID
- Operating System
- Docker version
- Agent version

**Resource Metrics:**
- CPU: 4/8 cores available
- RAM: 8/16 GB available
- Disk: 120/256 GB free
- Network status

**Container Information:**
- Active containers on this device
- Resource consumption
- Container history

### Device Actions

**View Details**: Click device row  
**Remove Device**: Click trash icon (confirmation required)  
**Refresh Status**: Click refresh button  

### Important Notes

⚠️ **Enrollment Token Security**
- Tokens expire after 24 hours
- Each token can only be used once
- Generate new token for each device
- Don't share tokens publicly

⚠️ **Device Naming**
- Use descriptive names (include lab and PC number)
- Examples: "Lab-A-PC-01", "Library-Workstation-3"
- Helps with troubleshooting and monitoring

---

## Managing Students

### Overview

Students are users who can:
- Create and manage containers
- Access terminal for their containers
- View their usage statistics

### Adding Students

#### Method 1: Bulk Upload via CSV (Recommended)

**Step 1: Prepare Student Data**

Create a CSV with three columns (no header):
```
student_id,email,full_name
```

Example:
```
500101234,john.doe@upes.ac.in,John Doe
500101235,jane.smith@upes.ac.in,Jane Smith
500101236,bob.wilson@upes.ac.in,Bob Wilson
500101237,alice.brown@upes.ac.in,Alice Brown
```

**Format Requirements:**
- ✅ Student ID: Alphanumeric, unique
- ✅ Email: Valid email address (must be unique)
- ✅ Full Name: Student's complete name
- ❌ No header row
- ❌ No quotes around values
- ❌ No spaces after commas

**Step 2: Upload Students**

1. Go to **Students** tab
2. Click **"UPLOAD STUDENTS"** button
3. Paste CSV data in the text area
4. Click **"Upload"**
5. Wait for confirmation

**Step 3: Verify Upload**

- Students appear in the table
- Status shows "Pending" (not activated yet)
- Email shows correctly

**✅ Success**: All students appear in the list

#### Method 2: Individual Registration (Coming Soon)

Currently, individual student registration is planned for future versions.

### Student Status

| Status | Meaning | Action |
|--------|---------|--------|
| Pending | Account created, password not set | Student must complete first-time setup |
| Active | Account fully set up, can use system | None |
| Suspended | Account temporarily disabled | Can be reactivated |
| Inactive | Hasn't logged in for 90+ days | Send reminder email |

### Student First-Time Setup

**What Students Need to Do:**

1. Visit: `http://your-portal/first-time-setup`
2. Enter:
   - Student ID (provided by you)
   - Email (provided by you)
   - New password (they choose)
3. Submit
4. Auto-login to student dashboard

**As Admin:**
- Share the first-time setup URL with students
- Provide them their student ID
- Ensure they use the email you uploaded

### Viewing Student Details

Click on any student row to see:

**Account Information:**
- Student ID
- Email
- Full Name
- Status
- Created date

**Usage Statistics:**
- Total containers created
- Active containers
- CPU hours used
- Storage used
- Last login

**Container List:**
- All containers owned by student
- Current status
- Resource allocation

### Student Actions

**View Details**: Click student row  
**Suspend Account**: Click suspend icon  
**Reset Password**: Click reset icon (coming soon)  
**Delete Student**: Click trash icon (confirmation required)  

### Setting Quotas (Coming Soon)

Future feature: Set per-student limits
- Max concurrent containers
- Max CPU allocation
- Max RAM allocation
- Max storage

---

## Monitoring & Analytics

### Dashboard Statistics

**Total Resources:**
```
CPU:    Total: 32 cores | Used: 18 cores | Available: 14 cores
RAM:    Total: 128 GB  | Used: 64 GB   | Available: 64 GB
Disk:   Total: 2 TB    | Used: 800 GB  | Available: 1.2 TB
```

**Container Statistics:**
- Total containers created (all time)
- Active containers (currently running)
- Stopped containers
- Average container lifetime

**Device Statistics:**
- Total devices enrolled
- Online devices (real-time)
- Average device uptime
- Resource utilization per device

**Student Statistics:**
- Total students registered
- Active students (logged in last 30 days)
- Average containers per student
- Top resource consumers

### Real-Time Monitoring

**Device Heartbeat:**
- Devices send heartbeat every 30 seconds
- Dashboard updates automatically
- Status changes reflect immediately

**Container Status:**
- Container state changes (pending → running → stopped)
- Resource consumption updates
- Terminal connections

### Usage Trends (Coming Soon)

Future features:
- Daily/weekly/monthly usage graphs
- Peak usage times
- Resource forecasting
- Capacity planning recommendations

---

## Troubleshooting

### Common Issues

#### Issue 1: Device Shows OFFLINE

**Symptoms:**
- Device status is 🔴 OFFLINE
- Last heartbeat > 2 minutes ago

**Possible Causes & Solutions:**

1. **Agent Not Running**
   ```bash
   # On lab computer, check agent status
   # Windows:
   sc query CampusComputeAgent
   
   # Linux:
   systemctl status campuscompute-agent
   ```
   **Solution**: Restart the agent

2. **Network Issue**
   - Check firewall allows WebSocket connections
   - Verify backend URL is correct
   - Test: `ping your-backend-url`

3. **Backend Not Responding**
   - Check backend health: `http://backend:8081/api/health`
   - Restart backend if needed

4. **Token Expired**
   - Generate new enrollment token
   - Reconfigure agent with new token

#### Issue 2: Student Can't Complete First-Time Setup

**Symptoms:**
- "Invalid credentials" error
- "Student not found" error

**Solutions:**

1. **Verify Student ID and Email Match**
   - Check spelling in Students table
   - Ensure student uses exact email you uploaded

2. **Check Student Status**
   - If already "Active", they should use normal login
   - First-time setup is only for "Pending" status

3. **Re-upload Student**
   - Delete student record
   - Upload again with correct details

#### Issue 3: Container Creation Fails

**Symptoms:**
- Container stays in PENDING status
- Error message shown

**Solutions:**

1. **No Devices Available**
   - Check if any devices are ONLINE
   - Add more devices

2. **Insufficient Resources**
   - Check if requested resources exceed available
   - Reduce container resource request

3. **Docker Issue on Device**
   - Check agent logs on the device
   - Ensure Docker is running
   - Test: `docker ps` on device

#### Issue 4: Enrollment Token Not Working

**Symptoms:**
- Agent fails to register
- "Invalid token" error

**Solutions:**

1. **Token Expired**
   - Tokens expire after 24 hours
   - Generate new token

2. **Token Already Used**
   - Each token is single-use
   - Generate new token for each device

3. **Wrong Organization**
   - Verify organization ID matches
   - Check backend URL is correct

### Getting Help

**Check Logs:**

Backend logs:
```bash
cd backend
tail -f logs/application.log
```

Agent logs:
```bash
cd agent
tail -f logs/agent.log
```

**Contact Support:**
- Email: support@campuscompute.edu
- GitHub Issues: https://github.com/rajrishu1401/CampusCompute/issues
- Documentation: https://campuscompute.edu/docs

---

## Best Practices

### Device Management

✅ **Do:**
- Use descriptive device names
- Monitor device health regularly
- Keep devices in good physical condition
- Update agent software when available
- Document device locations

❌ **Don't:**
- Reuse enrollment tokens
- Share tokens publicly
- Ignore OFFLINE devices for extended periods
- Overload devices beyond capacity

### Student Management

✅ **Do:**
- Upload students in batches
- Verify email addresses before upload
- Set up quotas to prevent abuse
- Monitor usage regularly
- Communicate with students about policies

❌ **Don't:**
- Use duplicate student IDs
- Use invalid email formats
- Grant unlimited resources
- Ignore suspicious usage patterns

### Security

✅ **Do:**
- Use strong admin password
- Change password regularly
- Log out when done
- Monitor for unauthorized access
- Review student activity periodically

❌ **Don't:**
- Share admin credentials
- Leave admin panel open on public computers
- Use weak passwords
- Ignore security warnings

### Capacity Planning

**Recommended Ratios:**
- **Students to Devices**: 10:1 to 20:1
  - Example: 100 students → 5-10 devices
- **Containers per Device**: 5-10 concurrent
- **CPU per Container**: 1-2 cores average
- **RAM per Container**: 1-2 GB average

**Monitoring Guidelines:**
- If devices consistently > 80% utilized → Add more devices
- If many containers in PENDING → Add more devices
- If complaints about slow performance → Check resource allocation

---

## Appendix

### Quick Reference

**Common URLs:**
- Admin Login: `/login`
- Dashboard: `/admin/dashboard`
- Devices: `/admin/devices`
- Students: `/admin/students`

**Common Shortcuts:**
- Refresh dashboard: F5
- Logout: Profile icon → Logout

**Important Files:**
- Agent config: `agent/config.yaml`
- Backend logs: `backend/logs/application.log`
- Agent logs: `agent/logs/agent.log`

### Glossary

**Agent**: Python application running on lab computers  
**Container**: Isolated Docker container for student use  
**Device**: Lab computer enrolled in resource pool  
**Enrollment Token**: One-time token for device registration  
**Heartbeat**: Periodic status update from device  
**Organization**: Your institution in the platform  
**Quota**: Resource limit for students  
**Student**: User who can create and use containers  

### Further Reading

- [Student User Guide](STUDENT_GUIDE.md)
- [Installation Guide](INSTALLATION.md)
- [API Reference](API_REFERENCE.md)
- [Architecture Documentation](../MULTI_ORG_ARCHITECTURE.md)

---

**Need Help?**

If you encounter any issues not covered in this guide:
1. Check the [Troubleshooting](#troubleshooting) section
2. Review agent/backend logs
3. Contact support

**Thank you for using CampusCompute!** 🎓

---

*Last Updated: October 3, 2026*  
*Version: 1.0*  
*For: CampusCompute v1.0*
