# CampusCompute - Student User Guide

**Version**: 1.0  
**Last Updated**: October 3, 2026  
**Audience**: Students  

---

## Table of Contents

1. [Introduction](#introduction)
2. [Getting Started](#getting-started)
3. [Your Dashboard](#your-dashboard)
4. [Creating Containers](#creating-containers)
5. [Using the Terminal](#using-the-terminal)
6. [Managing Containers](#managing-containers)
7. [Best Practices](#best-practices)
8. [Troubleshooting](#troubleshooting)
9. [FAQ](#faq)

---

## Introduction

Welcome to CampusCompute! 🎓

CampusCompute gives you access to cloud computing resources for your projects and coursework. You can:

✅ Create Docker containers with your favorite tools  
✅ Access containers via web-based terminal  
✅ Run code, install packages, and develop projects  
✅ Use resources from campus lab computers  

### What is a Container?

Think of a container as your own virtual computer that:
- Has its own operating system (Linux)
- Comes with pre-installed software
- Is isolated from other students' containers
- Can be accessed from anywhere via web browser
- Persists your files and settings

### Popular Use Cases

- **Programming Projects**: Python, Java, Node.js development
- **Data Science**: Run Jupyter notebooks, analyze datasets
- **Web Development**: Host local web servers
- **Machine Learning**: Train models, run experiments
- **DevOps Practice**: Learn Docker, CI/CD pipelines
- **System Administration**: Practice Linux commands

---

## Getting Started

### Step 1: First-Time Account Setup

Your administrator has created an account for you. You need to activate it:

1. **Get Your Credentials**
   - Ask your administrator for:
     - Student ID (e.g., `500101234`)
     - Email address (e.g., `john.doe@upes.ac.in`)
     - First-time setup URL

2. **Visit First-Time Setup Page**
   ```
   http://your-campuscompute-url/first-time-setup
   ```

3. **Fill in the Form**
   - **Student ID**: Enter the ID provided by admin
   - **Email**: Enter your email (must match admin's record)
   - **Password**: Create a strong password
     - Minimum 8 characters
     - Include uppercase, lowercase, number, special character
     - Example: `MySecure@Pass123`
   - **Confirm Password**: Re-enter your password

4. **Submit**
   - Click "Complete Setup"
   - You'll automatically be logged in
   - You'll see your student dashboard

**✅ Success**: You're now on your dashboard!

### Step 2: Login (After First-Time Setup)

For subsequent logins:

1. Visit: `http://your-campuscompute-url/login`
2. Enter:
   - **Email**: Your registered email
   - **Password**: The password you set
3. Click "Login"
4. You're redirected to your dashboard

---

## Your Dashboard

### Dashboard Overview

When you login, you'll see:

```
┌─────────────────────────────────────────────────┐
│  Welcome, John! 👋                              │
├─────────────────────────────────────────────────┤
│                                                  │
│  📊 Your Statistics                             │
│  ┌────────────────┐  ┌────────────────┐       │
│  │ Total          │  │ Running        │       │
│  │ Containers: 3  │  │ Containers: 2  │       │
│  └────────────────┘  └────────────────┘       │
│                                                  │
│  📦 Your Containers                             │
│  ┌────────────────────────────────────────┐   │
│  │ Name       Status    Actions           │   │
│  ├────────────────────────────────────────┤   │
│  │ my-ubuntu  RUNNING   Terminal | Stop   │   │
│  │ python-dev RUNNING   Terminal | Stop   │   │
│  │ test-env   STOPPED   Start | Delete    │   │
│  └────────────────────────────────────────┘   │
│                                                  │
└─────────────────────────────────────────────────┘
```

### Statistics Cards

**Total Containers**
- All containers you've created
- Includes running, stopped, and pending

**Running Containers**
- Containers currently active
- Using system resources
- Can be accessed via terminal

### Navigation

- **Dashboard**: Overview and quick actions
- **Containers**: Detailed container management
- **Profile**: Your account settings (coming soon)
- **Logout**: Sign out

---

## Creating Containers

### Step 1: Choose Container Image

A container image is like a template with pre-installed software.

**Popular Images:**

| Image | Description | Use For |
|-------|-------------|---------|
| `ubuntu:latest` | Latest Ubuntu Linux | General Linux practice |
| `ubuntu:20.04` | Ubuntu 20.04 LTS | Stable Linux environment |
| `python:3.11` | Python 3.11 pre-installed | Python development |
| `node:18` | Node.js 18 pre-installed | JavaScript/Node.js projects |
| `jupyter/scipy-notebook` | Jupyter + Scientific Python | Data science, ML |
| `postgres:15` | PostgreSQL database | Database learning |
| `nginx:latest` | NGINX web server | Web hosting practice |
| `redis:7` | Redis cache | Caching, queues |

**Where to Find More Images:**
- Docker Hub: https://hub.docker.com
- Search for: programming languages, tools, frameworks

### Step 2: Create Container

1. **Go to Containers Page**
   - Click "Containers" in sidebar
   - Or click "CREATE CONTAINER" on dashboard

2. **Click "CREATE CONTAINER" Button**

3. **Fill in Container Details**

   **Container Name** (optional)
   - Friendly name for your container
   - Example: `my-python-project`, `web-dev-env`
   - If left blank, auto-generated

   **Image**
   - Docker image to use
   - Example: `ubuntu:latest`
   - Must be valid Docker Hub image

   **CPU Cores**
   - Number of CPU cores to allocate
   - Range: 1-4 (typically)
   - Recommendation:
     - Light tasks (terminal, text editing): 1 core
     - Development, compilation: 2 cores
     - Heavy computation: 4 cores

   **RAM (GB)**
   - Memory to allocate
   - Range: 1-8 GB (typically)
   - Recommendation:
     - Basic Linux: 1 GB
     - Development: 2 GB
     - Data science, ML: 4-8 GB

   **Disk (GB)**
   - Storage space
   - Range: 5-50 GB (typically)
   - Recommendation:
     - Testing: 5-10 GB
     - Projects: 20 GB
     - Large datasets: 50 GB

4. **Submit**
   - Click "Create"
   - Wait for confirmation

5. **Watch Status**
   - Container appears with **PENDING** status
   - Backend finds available device
   - Docker pulls image (if needed)
   - Container starts
   - Status changes to **RUNNING**
   - Takes 10-60 seconds depending on image size

**✅ Success**: Status shows RUNNING with green indicator

### Example: Creating Ubuntu Container

```
Container Name: my-first-ubuntu
Image: ubuntu:latest
CPU Cores: 2
RAM: 2 GB
Disk: 10 GB

[Create Button]
```

Result:
- Container ID: `abc123def456`
- Status: RUNNING
- Device: LAB-03-PC-15
- Terminal button available

---

## Using the Terminal

### Accessing Terminal

1. **Find Your Running Container**
   - Go to Containers page
   - Look for container with RUNNING status

2. **Click "Terminal" Button**
   - Opens new tab/page
   - Shows terminal interface

3. **Wait for Connection**
   - Terminal connects via WebSocket
   - Shows prompt: `root@containerid:/#`

4. **Start Using**
   - Type commands
   - Press Enter to execute

### Terminal Interface

```
┌─────────────────────────────────────────────┐
│  Container: my-first-ubuntu                 │
│  Status: Connected                           │
├─────────────────────────────────────────────┤
│                                              │
│  root@abc123def456:/# ls                    │
│  bin   dev  home  lib64  mnt  proc  run ... │
│  root@abc123def456:/# pwd                   │
│  /                                           │
│  root@abc123def456:/# whoami                │
│  root                                        │
│  root@abc123def456:/# ▊                     │
│                                              │
└─────────────────────────────────────────────┘
```

### Basic Commands

**Navigation:**
```bash
pwd              # Show current directory
ls               # List files
ls -la           # List all files with details
cd /home         # Change directory
cd ..            # Go up one directory
```

**File Operations:**
```bash
touch file.txt   # Create empty file
nano file.txt    # Edit file (Ctrl+X to exit)
cat file.txt     # View file contents
cp file1 file2   # Copy file
mv file1 file2   # Move/rename file
rm file.txt      # Delete file
mkdir mydir      # Create directory
```

**System Information:**
```bash
whoami           # Current user
hostname         # Container hostname
uname -a         # System information
df -h            # Disk usage
free -h          # Memory usage
top              # Process list (press Q to quit)
```

**Package Management (Ubuntu):**
```bash
apt update                  # Update package list
apt install python3         # Install Python
apt install python3-pip     # Install pip
apt install git             # Install Git
apt search nginx            # Search for packages
```

**Example: Setting Up Python Environment**
```bash
# Update system
apt update

# Install Python and pip
apt install -y python3 python3-pip

# Verify installation
python3 --version
pip3 --version

# Install packages
pip3 install numpy pandas matplotlib

# Create a script
nano hello.py

# In nano, type:
print("Hello from CampusCompute!")

# Save: Ctrl+X, Y, Enter

# Run script
python3 hello.py
```

### Terminal Tips

**Keyboard Shortcuts:**
- `Ctrl + C`: Cancel current command
- `Ctrl + D`: Exit terminal (logout)
- `Ctrl + L`: Clear screen (or type `clear`)
- `Ctrl + A`: Move cursor to start of line
- `Ctrl + E`: Move cursor to end of line
- `Tab`: Auto-complete commands
- `↑` / `↓`: Navigate command history

**Useful Tricks:**
```bash
# Run command in background
python3 server.py &

# Check running processes
ps aux

# Kill process by PID
kill <pid>

# Download files
wget https://example.com/file.zip

# Extract archives
unzip file.zip
tar -xzf file.tar.gz

# Check network
ping google.com
curl https://api.github.com

# View command history
history

# Search previous commands
Ctrl + R, then type keyword
```

---

## Managing Containers

### Container Actions

#### View Container Details

Click on container row to see:
- Container ID
- Image used
- Resource allocation
- Creation date
- Status
- Device location

#### Start Container

For stopped containers:
1. Find container with STOPPED status
2. Click "Start" button
3. Wait for status to change to RUNNING
4. Terminal button becomes available

#### Stop Container

For running containers:
1. Find container with RUNNING status
2. Click "Stop" button
3. Confirm action
4. Container state is saved
5. Status changes to STOPPED

**Note:** Stopping a container:
- ✅ Saves all your files and changes
- ✅ Frees up resources for others
- ✅ Can be started again later
- ❌ Terminates running processes

#### Delete Container

**⚠️ Warning**: This permanently deletes the container and all data!

1. Find container (any status)
2. Click "Delete" button
3. Confirm deletion
4. Container removed from list

**Before deleting:**
- Back up important files
- Copy code to your local machine
- Export data you need

**How to backup:**
```bash
# In terminal, compress your files
tar -czf backup.tar.gz /path/to/important/files

# Download using:
# (Feature coming soon - currently manual)
```

### Container Lifecycle

```
CREATE → PENDING → RUNNING → STOPPED → DELETED
          ↓          ↓          ↓
        [wait]   [use it]   [restart]
                    ↓
                  STOP
```

**PENDING**
- Waiting for device assignment
- Pulling Docker image
- Starting container
- Typically 10-60 seconds

**RUNNING**
- Container is active
- Can access terminal
- Resources allocated
- Processes can run

**STOPPED**
- Container paused
- Files preserved
- No resources used
- Can be restarted

**DELETED**
- Permanently removed
- All data lost
- Cannot be recovered

---

## Best Practices

### Resource Usage

✅ **Do:**
- Request only what you need
- Stop containers when not in use
- Delete old containers you don't need
- Monitor your usage
- Be mindful that resources are shared

❌ **Don't:**
- Request max resources "just in case"
- Leave containers running 24/7 unnecessarily
- Create multiple containers doing the same thing
- Run CPU-intensive tasks indefinitely
- Store large unnecessary files

### Security

✅ **Do:**
- Use strong passwords
- Log out when done
- Only install software you need
- Keep your work organized
- Back up important files

❌ **Don't:**
- Share your credentials
- Install suspicious software
- Run untrusted scripts
- Leave terminal open on public computers
- Store sensitive personal data

### Efficiency

✅ **Do:**
- Use appropriate images (don't use full Ubuntu if you just need Python)
- Clean up old files regularly
- Stop containers between work sessions
- Plan your resource needs
- Use version control (Git) for code

❌ **Don't:**
- Download huge files unnecessarily
- Install every package "just in case"
- Keep multiple copies of large files
- Run multiple heavy processes simultaneously

### Academic Integrity

✅ **Do:**
- Use containers for legitimate coursework
- Collaborate as permitted by instructor
- Cite tools and resources you use
- Follow your institution's policies

❌ **Don't:**
- Use for non-academic purposes
- Share solutions inappropriately
- Violate course policies
- Misuse computing resources

---

## Troubleshooting

### Issue 1: Container Stuck in PENDING

**Symptoms:**
- Container shows PENDING for > 2 minutes
- Never transitions to RUNNING

**Solutions:**

1. **Wait Longer**
   - Large images (>1GB) take time to download
   - First-time image pulls are slower
   - Wait up to 5 minutes

2. **Check Image Name**
   - Verify image exists on Docker Hub
   - Check spelling: `ubuntu:latest` not `ubunto:latest`
   - Try common images: `ubuntu:20.04`, `python:3.11`

3. **Reduce Resource Request**
   - Try fewer CPU cores (1-2)
   - Try less RAM (1-2 GB)
   - Check if resources are available

4. **Contact Admin**
   - If still stuck after 5 minutes
   - May be no devices available

### Issue 2: Can't Connect to Terminal

**Symptoms:**
- Terminal button doesn't work
- "Connection failed" error
- Terminal shows blank screen

**Solutions:**

1. **Check Container Status**
   - Must be RUNNING (not PENDING or STOPPED)
   - Wait for container to fully start

2. **Refresh Page**
   - Close terminal tab
   - Go back to containers list
   - Click Terminal again

3. **Check Browser**
   - Some browsers block WebSockets
   - Try Chrome or Firefox
   - Disable browser extensions temporarily

4. **Check Network**
   - Campus network may have restrictions
   - Try different network if possible

### Issue 3: Commands Not Working in Terminal

**Symptoms:**
- Command not found errors
- Package installation fails

**Solutions:**

1. **Update Package List**
   ```bash
   apt update
   ```

2. **Install Missing Packages**
   ```bash
   apt install <package-name>
   ```

3. **Check Image**
   - Some minimal images don't have all tools
   - Try full images: `ubuntu:latest` instead of `ubuntu:minimal`

4. **Check Permissions**
   - You're root by default, but some operations may still fail
   - Check disk space: `df -h`

### Issue 4: Lost My Work

**Symptoms:**
- Container was deleted
- Files disappeared

**Solutions:**

**Prevention:**
- Back up regularly
- Use Git for code
- Copy important files to local machine
- Don't delete containers with important data

**Recovery:**
- Unfortunately, deleted containers cannot be recovered
- Always backup before deleting

### Issue 5: Can't Login

**Symptoms:**
- "Invalid credentials" error
- Forgot password

**Solutions:**

1. **Check Email/Password**
   - Verify email is correct
   - Check Caps Lock is off
   - Try copying/pasting

2. **First-Time Setup**
   - If you haven't set up your account yet
   - Use first-time setup page (not login page)

3. **Contact Admin**
   - For password reset
   - To verify account status

---

## FAQ

### General Questions

**Q: How many containers can I create?**  
A: Check with your administrator. Typically 2-5 concurrent containers.

**Q: Can I access my container from home?**  
A: Yes! Containers are accessible from anywhere via web browser.

**Q: Will my files be saved?**  
A: Yes, as long as you don't delete the container. Files persist even if you stop and start it.

**Q: Can I run Windows programs?**  
A: No, containers run Linux. Use Wine or alternatives for Windows software.

**Q: Can other students access my container?**  
A: No, containers are isolated and private to you.

### Technical Questions

**Q: What's the difference between Stop and Delete?**  
A: Stop pauses the container (files saved). Delete permanently removes it (files lost).

**Q: Can I install any software?**  
A: Yes, within your container. You have root access.

**Q: How much storage do I have?**  
A: Based on what you requested when creating the container (typically 10-20 GB).

**Q: Can I run servers/websites?**  
A: Yes, within your container. External access may be limited (coming soon).

**Q: Can I use GPU?**  
A: Not currently. GPU support is planned for future versions.

### Quota & Limits

**Q: Is there a time limit?**  
A: Check with your admin. Some organizations set max runtime per container.

**Q: Can I request more resources?**  
A: Yes, create a new container with higher resources (within limits).

**Q: What happens if I exceed quota?**  
A: You won't be able to create new containers until you stop/delete existing ones.

**Q: Are there usage fees?**  
A: No, CampusCompute uses your institution's existing resources.

---

## Quick Reference Card

### Essential Commands

```bash
# Navigation
ls                    # List files
cd <directory>        # Change directory
pwd                   # Current directory

# File Operations
nano <file>           # Edit file
cat <file>            # View file
cp <src> <dest>       # Copy
mv <src> <dest>       # Move/rename
rm <file>             # Delete

# System
apt update            # Update packages
apt install <pkg>     # Install package
df -h                 # Disk usage
free -h               # Memory usage

# Python
python3 --version     # Check version
pip3 install <pkg>    # Install package
python3 script.py     # Run script

# Help
man <command>         # Manual pages
<command> --help      # Help for command
```

### Container Status Colors

- 🟢 **RUNNING**: Active and accessible
- 🟡 **PENDING**: Starting up
- 🔴 **STOPPED**: Paused (files saved)
- ⚫ **DELETED**: Permanently removed

### Getting Help

- **Technical Issues**: Contact your administrator
- **Usage Questions**: Refer to this guide
- **Docker Help**: https://docs.docker.com
- **Linux Commands**: https://linux.die.net

---

## Next Steps

Now that you know the basics:

1. **Create your first container** 
   - Try: `ubuntu:latest`
   - Allocate: 2 CPU, 2GB RAM, 10GB disk

2. **Explore the terminal**
   - Practice basic Linux commands
   - Install your favorite tools

3. **Start a project**
   - Set up development environment
   - Install programming languages
   - Write and run code

4. **Learn Docker**
   - Understand containers better
   - Explore different images
   - Experiment with configurations

**Happy Computing!** 🚀

---

*Need help? Ask your administrator or refer to the troubleshooting section.*

*Last Updated: October 3, 2026*  
*Version: 1.0*  
*For: CampusCompute v1.0*
