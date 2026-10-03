# Simplified Agent Deployment Strategy

**Goal**: Make device enrollment as simple as possible for administrators

---

## 🎯 Current Problems

**Manual Process:**
1. ❌ Admin must manually edit config.yaml
2. ❌ Admin needs to know YAML syntax
3. ❌ Admin must manually install dependencies
4. ❌ Admin needs to understand Docker paths
5. ❌ Too many steps = high error rate

**Better Approach:**
- ✅ One-click installer (Windows/Linux/Mac)
- ✅ Interactive prompts (just paste token)
- ✅ Automatic dependency installation
- ✅ No technical knowledge required

---

## 🚀 Proposed Solutions

### **Solution 1: Desktop Installer (Recommended)**

#### **Windows (.exe Installer)**

**Technologies:**
- Inno Setup or NSIS for installer
- Python embedded runtime
- Docker Desktop auto-installer
- Windows Service wrapper

**User Experience:**
```
1. Admin downloads: CampusCompute-Agent-Setup.exe (50MB)
2. Double-click to run
3. Enter enrollment token (copy-paste)
4. Click "Install"
5. Wait 2 minutes
6. Done! Device shows up in dashboard
```

**What Installer Does:**
```python
# Pseudo-code for installer logic

def install_agent():
    # Step 1: Check Prerequisites
    if not is_docker_installed():
        print("Installing Docker Desktop...")
        download_and_install("https://docker.com/docker-desktop.exe")
        wait_for_docker_start()
    
    # Step 2: Prompt for Token
    token = show_dialog("Enter Enrollment Token:")
    device_name = show_dialog("Device Name (optional):", default=get_hostname())
    
    # Step 3: Install Agent
    create_directory("C:\\Program Files\\CampusCompute")
    extract_agent_files()
    
    # Step 4: Configure
    config = {
        "broker": {
            "url": fetch_broker_url_from_token(token),
            "enrollment_token": token
        },
        "device": {
            "device_id": device_name,
            "hostname": device_name
        }
    }
    write_config(config)
    
    # Step 5: Register as Service
    create_windows_service(
        name="CampusComputeAgent",
        display="CampusCompute Agent",
        executable="python.exe",
        args="C:\\Program Files\\CampusCompute\\agent\\src\\main.py"
    )
    
    # Step 6: Start Service
    start_service("CampusComputeAgent")
    
    # Step 7: Verify Connection
    if wait_for_connection(timeout=30):
        show_success("Device registered successfully!")
    else:
        show_error("Failed to connect. Check firewall settings.")
```

#### **Linux/Mac (Shell Script)**

```bash
#!/bin/bash
# install.sh - One-line installer

# Usage: curl -sSL https://install.campuscompute.io | bash -s -- <TOKEN>

TOKEN=$1
DEVICE_NAME=${2:-$(hostname)}

echo "🚀 CampusCompute Agent Installer"
echo "================================"

# Step 1: Check Docker
if ! command -v docker &> /dev/null; then
    echo "📦 Installing Docker..."
    curl -fsSL https://get.docker.com | sh
    sudo systemctl enable docker
    sudo systemctl start docker
fi

# Step 2: Check Python
if ! command -v python3 &> /dev/null; then
    echo "🐍 Installing Python..."
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        sudo apt-get update && sudo apt-get install -y python3 python3-pip
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        brew install python3
    fi
fi

# Step 3: Download Agent
echo "⬇️  Downloading agent..."
sudo mkdir -p /opt/campuscompute
cd /opt/campuscompute
sudo curl -L https://github.com/campuscompute/agent/releases/latest/download/agent.tar.gz | sudo tar xz

# Step 4: Install Dependencies
echo "📚 Installing dependencies..."
cd agent
sudo pip3 install -r requirements.txt

# Step 5: Configure
echo "⚙️  Configuring agent..."
sudo tee config.yaml > /dev/null <<EOF
broker:
  url: "ws://api.campuscompute.io/ws/agent"
  enrollment_token: "$TOKEN"

device:
  device_id: "$DEVICE_NAME"
  hostname: "$DEVICE_NAME"

docker:
  socket: "unix:///var/run/docker.sock"

logging:
  level: "INFO"
  file: "/var/log/campuscompute/agent.log"
EOF

# Step 6: Create Systemd Service
echo "🔧 Setting up service..."
sudo tee /etc/systemd/system/campuscompute-agent.service > /dev/null <<EOF
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

# Step 7: Start Service
echo "🚀 Starting agent..."
sudo systemctl daemon-reload
sudo systemctl enable campuscompute-agent
sudo systemctl start campuscompute-agent

# Step 8: Verify
sleep 3
if systemctl is-active --quiet campuscompute-agent; then
    echo "✅ Agent installed successfully!"
    echo "   Device '$DEVICE_NAME' should appear in your dashboard."
else
    echo "❌ Failed to start agent. Check logs:"
    echo "   sudo journalctl -u campuscompute-agent -f"
fi
```

---

### **Solution 2: Docker-Only Deployment (Simplest)**

**Requirement:** Only Docker installed (most labs have it)

```bash
# Single command to run agent in container
docker run -d \
  --name campuscompute-agent \
  --restart always \
  -e ENROLLMENT_TOKEN="your-token-here" \
  -e DEVICE_NAME="$(hostname)" \
  -v /var/run/docker.sock:/var/run/docker.sock \
  campuscompute/agent:latest
```

**Advantages:**
- ✅ No Python installation needed
- ✅ No dependency management
- ✅ Consistent across all OSes
- ✅ Easy to update: `docker pull campuscompute/agent:latest`
- ✅ Easy to remove: `docker rm -f campuscompute-agent`

**Agent Dockerfile:**
```dockerfile
FROM python:3.11-slim

WORKDIR /app

# Install dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy agent code
COPY src/ ./src/

# Environment variables (token passed at runtime)
ENV ENROLLMENT_TOKEN=""
ENV DEVICE_NAME=""
ENV BACKEND_URL="ws://api.campuscompute.io/ws/agent"

# Generate config from environment
COPY entrypoint.sh .
RUN chmod +x entrypoint.sh

CMD ["./entrypoint.sh"]
```

**entrypoint.sh:**
```bash
#!/bin/bash

# Generate config from environment variables
cat > config.yaml <<EOF
broker:
  url: "$BACKEND_URL"
  enrollment_token: "$ENROLLMENT_TOKEN"

device:
  device_id: "${DEVICE_NAME:-$(hostname)}"
  hostname: "${DEVICE_NAME:-$(hostname)}"

docker:
  socket: "unix:///var/run/docker.sock"

logging:
  level: "INFO"
EOF

# Start agent
python3 src/main.py
```

---

### **Solution 3: Web-Based Installer Generator**

**Admin Workflow:**
```
1. Admin clicks "ADD DEVICE" in dashboard
2. System shows:
   ┌────────────────────────────────────────┐
   │  Choose Installation Method:           │
   │                                        │
   │  [Windows] Download .exe installer     │
   │  [Linux]   Copy install command        │
   │  [Mac]     Copy install command        │
   │  [Docker]  Copy docker run command     │
   │                                        │
   │  Token embedded in download/command!   │
   └────────────────────────────────────────┘

3. Download/command includes token automatically
4. No manual copy-paste needed!
```

**Backend generates custom installer:**
```java
@PostMapping("/devices/installer")
public ResponseEntity<byte[]> generateInstaller(
    @RequestParam String os,
    HttpServletRequest request
) {
    Long orgId = (Long) request.getAttribute("organizationId");
    
    // Generate token
    String token = deviceService.generateEnrollmentToken(orgId);
    
    // Generate custom installer with embedded token
    byte[] installerBytes;
    
    if (os.equals("windows")) {
        installerBytes = buildWindowsInstaller(token, orgId);
    } else if (os.equals("linux") || os.equals("mac")) {
        installerBytes = buildShellScript(token, orgId);
    } else if (os.equals("docker")) {
        installerBytes = buildDockerCommand(token, orgId);
    }
    
    return ResponseEntity.ok()
        .header("Content-Disposition", "attachment; filename=install-agent." + getExtension(os))
        .body(installerBytes);
}
```

---

## 🔑 Why Token is Essential

### **Without Token (Insecure):**
```
❌ Any random device can connect
❌ Devices could join wrong organization
❌ No way to track which admin authorized device
❌ Security nightmare!
```

### **With Token (Secure):**
```
✅ Only authorized devices can join
✅ Device automatically assigned to correct org
✅ Audit trail: "Admin X enrolled Device Y at time Z"
✅ Token expires = limited attack window
✅ Single-use = can't reuse stolen tokens
```

**Token Flow:**
```
Admin clicks "Add Device"
   ↓
Backend generates unique token:
   - Includes: orgId, expiryTime, signature
   - Stores in DB: {token, orgId, used: false, expiresAt}
   ↓
Admin enters token in installer
   ↓
Agent connects with token
   ↓
Backend validates:
   ✓ Token exists?
   ✓ Not expired?
   ✓ Not used before?
   ✓ Signature valid?
   ↓
Backend registers device:
   - Creates Device record
   - Links to organization
   - Marks token as used
   ↓
Agent connected! Dashboard updated!
```

---

## 📊 Comparison

| Method | Ease of Use | Security | Maintenance | Best For |
|--------|-------------|----------|-------------|----------|
| **Manual Config** | ⭐ | ⭐⭐⭐ | ⭐ | Developers |
| **Shell Script** | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ | Linux Admins |
| **Windows Installer** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ | Non-technical |
| **Docker Run** | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | Modern Infra |
| **Web Installer** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ | Enterprise |

---

## 🎯 Recommended Approach for CampusCompute

**Phase 1 (MVP - Current):** Manual configuration ✅ Done
**Phase 2 (Better UX):** Shell script installer
**Phase 3 (Best UX):** Windows/Mac installers + Docker image
**Phase 4 (Enterprise):** Web-based custom installer generator

---

## 🚀 Next Steps

1. **Create shell script installer** (1 day)
2. **Build Docker image** for agent (1 day)
3. **Update frontend** to show install commands (2 hours)
4. **Test on fresh machines** (1 day)
5. **Document** in user guide (2 hours)

Total: ~3 days to dramatically improve UX!

---

**Bottom Line:** Token is essential for security, but we hide the complexity from users with smart installers. User just needs to:
1. Copy token
2. Run one command (or double-click installer)
3. Done!

This is how professional platforms (Kubernetes, Docker Swarm, Tailscale) handle node enrollment.
