# CampusCompute Agent - Windows Installer

One-click Windows installer for enrolling devices to CampusCompute platform.

---

## 🎯 Features

- **✨ Fully Automated Installation**: Zero manual configuration required
- **User-Friendly GUI**: Simple tkinter interface with progress tracking
- **Automatic Dependencies**: 
  - Automatically downloads and installs Docker Desktop (500 MB) if needed
  - Automatically downloads and installs Python 3.13 (25 MB) if needed
  - Downloads with real-time progress indicators
- **Smart Error Handling**: 
  - Gracefully handles failures with clear messages
  - Offers fallback options if service creation fails
  - Validates prerequisites before starting
- **Windows Service**: Runs agent as a background service
- **Auto-Start**: Agent starts automatically on system boot
- **One-Click Enrollment**: Just paste token and click Install

---

## 🚀 For End Users (Administrators)

### Quick Start

1. **Download** `CampusCompute-Agent-Installer.exe` from your admin dashboard
2. **Right-click** the installer → **Run as Administrator**
3. **Paste** the enrollment token from your dashboard
4. **Click** Install
5. **Wait** 2-5 minutes (depending on what needs to be installed)
6. **Done!** Device appears in your dashboard

### Screenshots

```
┌────────────────────────────────────────┐
│  CampusCompute Agent Installer         │
│  Enroll this device to your org        │
├────────────────────────────────────────┤
│                                        │
│  Enrollment Token:                     │
│  [Paste token here ________________]   │
│                                        │
│  Device Name (optional):               │
│  [LAB-PC-001 _______________________]  │
│                                        │
│  Backend URL (advanced):               │
│  [ws://localhost:8081/ws/agent _____]  │
│                                        │
│  Installation Options:                 │
│  [✓] Install Docker Desktop            │
│  [✓] Install Python 3.11               │
│                                        │
│  [████████████░░░░░░░░░░░] 60%         │
│  Installing dependencies...            │
│                                        │
│     [ Install ]     [ Cancel ]         │
└────────────────────────────────────────┘
```

### Requirements

- **Windows 10/11** (64-bit)
- **Administrator privileges**
- **Internet connection** (for downloads)
- **4GB RAM minimum** (8GB recommended)
- **10GB free disk space**

---

## 🛠️ For Developers

### Building the Installer

**Prerequisites:**
```bash
pip install -r requirements.txt
```

**Build .exe:**
```bash
python build_installer.py
```

**Output:**
- `dist/CampusCompute-Agent-Installer.exe` (standalone executable)
- Size: ~15-20MB (includes Python runtime)

### Development Setup

```bash
# Install dependencies
pip install -r requirements.txt

# Run installer in development mode
python installer.py
```

### Project Structure

```
agent-installer/
├── installer.py              # Main installer GUI
├── build_installer.py        # PyInstaller build script
├── requirements.txt          # Build dependencies
├── README.md                 # This file
└── icon.ico                  # Application icon (optional)
```

---

## 📋 What the Installer Does

### Step 1: Pre-Installation Checks (10%)
- ✓ Verify administrator privileges
- ✓ Check Docker installation
- ✓ Check Python installation
- ✓ Validate enrollment token format

### Step 2: Install Prerequisites (10-30%)
- 📦 Download & install Docker Desktop (if needed)
- 🐍 Download & install Python 3.11 (if needed)
- ⏸️ May require system restart

### Step 3: Install Agent (50%)
- 📁 Create `C:\Program Files\CampusCompute`
- 📥 Copy agent files
- 📝 Generate configuration file

### Step 4: Configure (60%)
- ⚙️ Create `config.yaml` with token
- 🔌 Configure Docker socket connection
- 📊 Set up logging directory

### Step 5: Install Dependencies (70%)
- 📦 Install Python packages via pip
- ✓ websockets, docker, psutil, etc.

### Step 6: Create Service (80%)
- 🔧 Register Windows Service
- ⚙️ Configure auto-start on boot
- 🚀 Start the service

### Step 7: Verify (90-100%)
- ✓ Check service status
- ✓ Verify WebSocket connection
- ✅ Complete!

---

## 🔧 Advanced Configuration

### Custom Backend URL

If your backend is not on localhost, edit the Backend URL field:

```
Production: wss://api.campuscompute.io/ws/agent
Local Dev:  ws://localhost:8081/ws/agent
Custom:     ws://your-server:port/ws/agent
```

### Manual Service Management

```powershell
# Start service
python "C:\Program Files\CampusCompute\service_wrapper.py" start

# Stop service
python "C:\Program Files\CampusCompute\service_wrapper.py" stop

# Restart service
python "C:\Program Files\CampusCompute\service_wrapper.py" restart

# Check status
sc query CampusComputeAgent
```

### Logs Location

```
C:\Program Files\CampusCompute\logs\agent.log
```

View logs:
```powershell
Get-Content "C:\Program Files\CampusCompute\logs\agent.log" -Wait
```

---

## 🔍 Troubleshooting

### "Run as Administrator" Error

**Problem:** Installer fails immediately

**Solution:** 
- Right-click the .exe
- Select "Run as Administrator"
- Or: Open Command Prompt as Admin → Run installer from there

### Docker Installation Fails

**Problem:** Docker Desktop won't install

**Solution:**
- Enable WSL2 in Windows Features
- Enable Hyper-V in Windows Features
- Restart computer
- Run installer again

### Service Won't Start

**Problem:** Agent service fails to start

**Solution:**
```powershell
# Check if Docker is running
docker ps

# View service logs
Get-EventLog -LogName Application -Source CampusComputeAgent

# Start service manually
net start CampusComputeAgent
```

### Device Not Appearing in Dashboard

**Problem:** Installed but device doesn't show up

**Solution:**
1. Check token is correct (copy-paste carefully)
2. Check backend URL is correct
3. Check firewall isn't blocking WebSocket connection
4. View logs: `C:\Program Files\CampusCompute\logs\agent.log`

### Uninstall

```powershell
# Stop and remove service
python "C:\Program Files\CampusCompute\service_wrapper.py" remove

# Delete installation directory
Remove-Item -Recurse "C:\Program Files\CampusCompute"
```

---

## 🎓 For Academic Projects

### Demonstrating the Installer

1. **Preparation:**
   - Build the .exe installer
   - Have admin dashboard open and ready
   - Prepare a fresh Windows VM/machine

2. **Live Demo:**
   - Generate token in admin dashboard
   - Run installer on fresh machine
   - Show automatic installation process
   - Show device appearing in dashboard

3. **Highlight Features:**
   - One-click installation (vs. manual 8-step process)
   - Automatic dependency management
   - Professional GUI
   - Windows service integration
   - Error handling and validation

---

## 📊 Comparison

| Method | Steps | Time | Technical Knowledge |
|--------|-------|------|-------------------|
| **Manual Setup** | 8 | 15-30 min | High |
| **This Installer** | 3 | 3-5 min | None |

---

## 🚧 Known Limitations

- Windows 10/11 only (separate installer needed for Linux/Mac)
- Requires admin privileges (for service installation)
- Large download size (~15-20MB) due to embedded Python runtime
- May trigger Windows Defender (needs code signing certificate)

---

## 🔜 Future Enhancements

- [ ] Add progress percentage during Docker/Python installation
- [ ] Support for silent/unattended installation
- [ ] Automatic updates checking
- [ ] Multi-language support
- [ ] Code signing certificate
- [ ] NSIS installer (instead of PyInstaller)
- [ ] MSI package for enterprise deployment

---

## 📝 License

Part of the CampusCompute project - UPES Dehradun Major Project 2026-27

---

## 🙏 Credits

Built with:
- **Python**: Programming language
- **tkinter**: GUI framework
- **PyInstaller**: .exe packaging
- **pywin32**: Windows service integration
