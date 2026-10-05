"""
CampusCompute Agent - Windows Installer
One-click installer with GUI for enrolling devices
"""

import tkinter as tk
from tkinter import ttk, messagebox, scrolledtext
import subprocess
import sys
import os
import json
import winreg
import urllib.request
import zipfile
import shutil
from pathlib import Path
import threading
import socket
import ctypes
from ctypes import wintypes

# Single instance check using mutex
class SingleInstance:
    def __init__(self, mutex_name):
        self.mutex_name = mutex_name
        self.mutex = ctypes.windll.kernel32.CreateMutexW(None, False, mutex_name)
        self.last_error = ctypes.windll.kernel32.GetLastError()
        
    def is_already_running(self):
        return self.last_error == 183  # ERROR_ALREADY_EXISTS

class AgentInstaller:
    def __init__(self):
        # Check for single instance - DISABLED FOR TESTING
        # self.single_instance = SingleInstance("CampusComputeAgentInstaller_Mutex")
        # if self.single_instance.is_already_running():
        #     messagebox.showerror(
        #         "Already Running",
        #         "CampusCompute Agent Installer is already running.\n\n"
        #         "Please close the existing installer window first."
        #     )
        #     sys.exit(1)
        
        self.root = tk.Tk()
        self.root.title("CampusCompute Agent Installer")
        self.root.geometry("600x550")
        self.root.resizable(False, False)
        
        # Installation paths
        self.install_dir = Path("C:/Program Files/CampusCompute")
        self.config_file = self.install_dir / "agent" / "config.yaml"
        
        # Variables
        self.token_var = tk.StringVar()
        self.device_name_var = tk.StringVar(value=socket.gethostname())
        self.backend_url_var = tk.StringVar(value="ws://localhost:8081/ws/agent")
        self.install_docker_var = tk.BooleanVar(value=True)
        self.install_python_var = tk.BooleanVar(value=True)
        
        self.setup_ui()
        
    def setup_ui(self):
        """Create the installer UI"""
        
        # Header
        header_frame = tk.Frame(self.root, bg="#1976d2", height=80)
        header_frame.pack(fill=tk.X)
        header_frame.pack_propagate(False)
        
        tk.Label(
            header_frame, 
            text="CampusCompute Agent Installer",
            font=("Arial", 16, "bold"),
            bg="#1976d2",
            fg="white"
        ).pack(pady=10)
        
        tk.Label(
            header_frame,
            text="Enroll this device to your CampusCompute organization",
            font=("Arial", 10),
            bg="#1976d2",
            fg="white"
        ).pack()
        
        # Main content
        content_frame = tk.Frame(self.root, padx=20, pady=20)
        content_frame.pack(fill=tk.BOTH, expand=True)
        
        # Token input
        tk.Label(
            content_frame,
            text="Enrollment Token:",
            font=("Arial", 10, "bold")
        ).grid(row=0, column=0, sticky=tk.W, pady=(0, 5))
        
        tk.Label(
            content_frame,
            text="Get this from your admin dashboard (Add Device button)",
            font=("Arial", 8),
            fg="gray"
        ).grid(row=1, column=0, sticky=tk.W, pady=(0, 5))
        
        token_entry = tk.Entry(
            content_frame,
            textvariable=self.token_var,
            width=60,
            font=("Arial", 10)
        )
        token_entry.grid(row=2, column=0, pady=(0, 15), ipady=5)
        token_entry.focus()
        
        # Device name input
        tk.Label(
            content_frame,
            text="Device Name (optional):",
            font=("Arial", 10, "bold")
        ).grid(row=3, column=0, sticky=tk.W, pady=(0, 5))
        
        tk.Entry(
            content_frame,
            textvariable=self.device_name_var,
            width=60,
            font=("Arial", 10)
        ).grid(row=4, column=0, pady=(0, 15), ipady=5)
        
        # Backend URL (advanced)
        tk.Label(
            content_frame,
            text="Backend URL (advanced):",
            font=("Arial", 10, "bold")
        ).grid(row=5, column=0, sticky=tk.W, pady=(0, 5))
        
        tk.Entry(
            content_frame,
            textvariable=self.backend_url_var,
            width=60,
            font=("Arial", 10)
        ).grid(row=6, column=0, pady=(0, 15), ipady=5)
        
        # Installation options
        tk.Label(
            content_frame,
            text="Automatic Installation (Recommended):",
            font=("Arial", 10, "bold")
        ).grid(row=7, column=0, sticky=tk.W, pady=(0, 5))
        
        tk.Checkbutton(
            content_frame,
            text="✓ Automatically install Docker Desktop if needed (~500 MB, requires restart)",
            variable=self.install_docker_var,
            font=("Arial", 9)
        ).grid(row=8, column=0, sticky=tk.W)
        
        tk.Checkbutton(
            content_frame,
            text="✓ Automatically install Python 3.13 if needed (~25 MB)",
            variable=self.install_python_var,
            font=("Arial", 9)
        ).grid(row=9, column=0, sticky=tk.W, pady=(0, 5))
        
        tk.Label(
            content_frame,
            text="Recommended: Keep both options checked for fully automated installation",
            font=("Arial", 8),
            fg="green"
        ).grid(row=10, column=0, sticky=tk.W, pady=(0, 15))
        
        # Progress bar
        self.progress_var = tk.DoubleVar()
        self.progress_bar = ttk.Progressbar(
            content_frame,
            variable=self.progress_var,
            maximum=100,
            length=560
        )
        self.progress_bar.grid(row=11, column=0, pady=(10, 5))
        
        # Status label
        self.status_label = tk.Label(
            content_frame,
            text="✓ Ready to install - Click 'Install' to begin",
            font=("Arial", 9),
            fg="gray"
        )
        self.status_label.grid(row=12, column=0, pady=(0, 15))
        
        # Buttons
        button_frame = tk.Frame(content_frame)
        button_frame.grid(row=13, column=0)
        
        self.install_button = tk.Button(
            button_frame,
            text="Install",
            command=self.start_installation,
            bg="#1976d2",
            fg="white",
            font=("Arial", 10, "bold"),
            width=15,
            height=2
        )
        self.install_button.pack(side=tk.LEFT, padx=5)
        
        tk.Button(
            button_frame,
            text="Cancel",
            command=self.root.quit,
            font=("Arial", 10),
            width=15,
            height=2
        ).pack(side=tk.LEFT, padx=5)
        
    def update_status(self, message, progress=None):
        """Update status label and progress bar"""
        self.status_label.config(text=message)
        if progress is not None:
            self.progress_var.set(progress)
        self.root.update()
        
    def check_admin(self):
        """Check if running with admin privileges"""
        try:
            return os.getuid() == 0
        except AttributeError:
            import ctypes
            return ctypes.windll.shell32.IsUserAnAdmin() != 0
            
    def check_docker(self):
        """Check if Docker is installed"""
        try:
            result = subprocess.run(
                ["docker", "--version"],
                capture_output=True,
                text=True
            )
            return result.returncode == 0
        except FileNotFoundError:
            return False
            
    def check_python(self):
        """Check if Python is installed"""
        # Try multiple Python commands
        for cmd in ["py", "python", "python3"]:
            try:
                result = subprocess.run(
                    [cmd, "--version"],
                    capture_output=True,
                    text=True
                )
                if result.returncode == 0:
                    return True
            except FileNotFoundError:
                continue
        return False
            
    def install_docker(self):
        """Download and install Docker Desktop"""
        self.update_status("Downloading Docker Desktop (500 MB)...", 20)
        
        # Use temp directory for downloads
        temp_dir = Path(os.environ.get('TEMP', 'C:/Windows/Temp'))
        docker_installer = temp_dir / "Docker-Desktop-Installer.exe"
        docker_url = "https://desktop.docker.com/win/main/amd64/Docker%20Desktop%20Installer.exe"
        
        try:
            # Download with progress callback
            def download_progress(block_num, block_size, total_size):
                downloaded = block_num * block_size
                if total_size > 0:
                    percent = min(int((downloaded / total_size) * 100), 100)
                    self.update_status(f"Downloading Docker Desktop: {percent}% ({downloaded // (1024*1024)} MB / {total_size // (1024*1024)} MB)", 20 + (percent * 0.15))
                self.root.update()
            
            urllib.request.urlretrieve(docker_url, docker_installer, reporthook=download_progress)
            
            self.update_status("Installing Docker Desktop (this will take 5-10 minutes)...", 35)
            
            # Install Docker Desktop silently
            # Docker Desktop installer accepts: install --quiet --accept-license
            result = subprocess.run(
                [str(docker_installer), "install", "--quiet", "--accept-license"],
                capture_output=True,
                text=True,
                timeout=600  # 10 minute timeout
            )
            
            # Clean up installer
            if docker_installer.exists():
                try:
                    docker_installer.unlink()
                except:
                    pass
            
            if result.returncode != 0:
                error_msg = result.stderr if result.stderr else result.stdout
                raise Exception(f"Docker installer returned code {result.returncode}: {error_msg}")
            
            # Docker requires a reboot to work properly
            response = messagebox.askyesno(
                "Docker Installed - Reboot Required",
                "Docker Desktop has been installed successfully!\n\n"
                "A system restart is required for Docker to work.\n\n"
                "Would you like to restart now?\n\n"
                "(After restart, please run this installer again to complete the agent setup)"
            )
            
            if response:
                # Reboot the system
                subprocess.run(["shutdown", "/r", "/t", "10", "/c", "Rebooting for Docker Desktop installation..."])
                messagebox.showinfo(
                    "Rebooting...",
                    "Your computer will restart in 10 seconds.\n\n"
                    "Please run the CampusCompute Agent Installer again after reboot."
                )
            else:
                messagebox.showinfo(
                    "Restart Required",
                    "Please restart your computer manually and run this installer again to complete the setup."
                )
            
            sys.exit(0)
            
        except subprocess.TimeoutExpired:
            messagebox.showerror(
                "Installation Timeout",
                "Docker installation is taking too long. Please install Docker Desktop manually from:\n\n"
                "https://www.docker.com/products/docker-desktop"
            )
            return False
        except Exception as e:
            messagebox.showerror(
                "Docker Installation Failed",
                f"Failed to install Docker Desktop: {e}\n\n"
                "You can install it manually from:\n"
                "https://www.docker.com/products/docker-desktop\n\n"
                "After installation, restart your computer and run this installer again."
            )
            return False
            
        return True
        
    def install_python(self):
        """Download and install Python"""
        self.update_status("Downloading Python 3.13 (25 MB)...", 20)
        
        temp_dir = Path(os.environ.get('TEMP', 'C:/Windows/Temp'))
        python_installer = temp_dir / "python-3.13-installer.exe"
        # Python 3.13.1 (latest stable as of Oct 2026)
        python_url = "https://www.python.org/ftp/python/3.13.1/python-3.13.1-amd64.exe"
        
        try:
            # Download with progress
            def download_progress(block_num, block_size, total_size):
                downloaded = block_num * block_size
                if total_size > 0:
                    percent = min(int((downloaded / total_size) * 100), 100)
                    self.update_status(f"Downloading Python 3.13: {percent}% ({downloaded // (1024*1024)} MB / {total_size // (1024*1024)} MB)", 20 + (percent * 0.1))
                self.root.update()
                
            urllib.request.urlretrieve(python_url, python_installer, reporthook=download_progress)
            
            self.update_status("Installing Python 3.13 (this will take 2-3 minutes)...", 30)
            
            # Install Python with all features silently
            result = subprocess.run(
                [
                    str(python_installer),
                    "/quiet",                    # Silent installation
                    "InstallAllUsers=1",         # Install for all users (requires admin)
                    "PrependPath=1",             # Add to PATH
                    "Include_pip=1",             # Include pip
                    "Include_test=0",            # Skip tests
                    "Include_doc=0",             # Skip docs
                    "Include_dev=0",             # Skip dev files
                    "AssociateFiles=1",          # Associate .py files
                    "Shortcuts=1",               # Create shortcuts
                ],
                capture_output=True,
                text=True,
                timeout=300  # 5 minute timeout
            )
            
            # Clean up installer
            if python_installer.exists():
                try:
                    python_installer.unlink()
                except:
                    pass
            
            if result.returncode != 0:
                # Sometimes returns non-zero even on success, so check if Python is now available
                self.update_status("Verifying Python installation...", 40)
                
                # Refresh PATH to pick up newly installed Python
                import winreg
                try:
                    with winreg.OpenKey(winreg.HKEY_LOCAL_MACHINE, r"System\CurrentControlSet\Control\Session Manager\Environment") as key:
                        path_value = winreg.QueryValueEx(key, "Path")[0]
                        os.environ["PATH"] = path_value + ";" + os.environ.get("PATH", "")
                except:
                    pass
                
                # Check if Python is now available
                if not self.check_python():
                    error_msg = result.stderr if result.stderr else result.stdout
                    raise Exception(f"Python installer returned code {result.returncode}. Python still not found. Details: {error_msg[:200]}")
            
            # Verify installation again
            self.update_status("Verifying Python installation...", 45)
            
            # Give it a moment for PATH to refresh
            import time
            time.sleep(2)
            
            if not self.check_python():
                # Try one more time with full path check
                possible_paths = [
                    r"C:\Program Files\Python313\python.exe",
                    r"C:\Python313\python.exe",
                    os.path.expanduser(r"~\AppData\Local\Programs\Python\Python313\python.exe")
                ]
                
                python_found = False
                for python_path in possible_paths:
                    if Path(python_path).exists():
                        # Add to PATH
                        python_dir = str(Path(python_path).parent)
                        os.environ["PATH"] = python_dir + ";" + os.environ.get("PATH", "")
                        python_found = True
                        break
                
                if not python_found:
                    messagebox.showwarning(
                        "Python Installation",
                        "Python was installed but may not be available until you restart.\n\n"
                        "Please restart your computer and run this installer again."
                    )
                    sys.exit(0)
            
            messagebox.showinfo(
                "Python Installed",
                "Python 3.13 has been installed successfully!"
            )
            
        except subprocess.TimeoutExpired:
            messagebox.showerror(
                "Installation Timeout",
                "Python installation is taking too long. Please install Python manually from:\n\n"
                "https://www.python.org/downloads/"
            )
            return False
        except Exception as e:
            messagebox.showerror(
                "Python Installation Failed",
                f"Failed to install Python: {e}\n\n"
                "Please install Python 3.11 or later manually from:\n"
                "https://www.python.org/downloads/\n\n"
                "Make sure to check 'Add Python to PATH' during installation."
            )
            return False
            
        return True
        
    def create_config(self):
        """Create agent configuration file"""
        # Use ProgramData for logs (writable by services)
        log_dir = Path("C:/ProgramData/CampusCompute/logs")
        log_dir.mkdir(parents=True, exist_ok=True)
        # Convert to string and replace backslashes with forward slashes for YAML
        log_file_path = str(log_dir / "agent.log").replace('\\', '/')
        
        config_content = f"""broker:
  url: "{self.backend_url_var.get()}"
  enrollment_token: "{self.token_var.get()}"

device:
  device_id: "{self.device_name_var.get()}"
  hostname: "{self.device_name_var.get()}"
  lab_name: "Default Lab"
  organization_id: 1

docker:
  socket: "npipe:////./pipe/docker_engine"

logging:
  level: "INFO"
  file: "{log_file_path}"

monitoring:
  heartbeat_interval: 30
  metrics_interval: 60
  container_check_interval: 10
"""
        
        self.config_file.parent.mkdir(parents=True, exist_ok=True)
        self.config_file.write_text(config_content)
        
    def install_agent_files(self):
        """Copy agent files to installation directory"""
        self.update_status("Installing agent files...", 50)
        
        # Create directories
        (self.install_dir / "agent").mkdir(parents=True, exist_ok=True)
        (self.install_dir / "logs").mkdir(parents=True, exist_ok=True)
        
        # Get the bundled agent files from PyInstaller temp directory OR parent directory
        if getattr(sys, 'frozen', False):
            # Running as compiled exe - use PyInstaller's temp directory
            bundle_dir = Path(sys._MEIPASS)
            agent_src_dir = bundle_dir / "agent" / "src"
            requirements_src = bundle_dir / "agent" / "requirements.txt"
        else:
            # Running as script - use parent directory (../agent)
            script_dir = Path(__file__).parent
            agent_src_dir = script_dir.parent / "agent" / "src"
            requirements_src = script_dir.parent / "agent" / "requirements.txt"
        
        # Copy agent source files
        agent_dst = self.install_dir / "agent" / "src"
        
        if agent_src_dir.exists():
            shutil.copytree(agent_src_dir, agent_dst, dirs_exist_ok=True)
        else:
            messagebox.showerror(
                "Installation Error",
                f"Agent source files not found!\n\nExpected at: {agent_src_dir}\n\n"
                "Please contact support or download the full installer package."
            )
            return False
            
        # Copy requirements.txt
        requirements_dst = self.install_dir / "agent" / "requirements.txt"
        
        if requirements_src.exists():
            shutil.copy(requirements_src, requirements_dst)
        else:
            messagebox.showerror(
                "Installation Error",
                f"Requirements file not found!\n\nExpected at: {requirements_src}"
            )
            return False
            
        return True
            
    def install_dependencies(self):
        """Install Python dependencies"""
        self.update_status("Installing dependencies...", 60)
        
        requirements_file = self.install_dir / "agent" / "requirements.txt"
        
        # Try different Python commands
        python_commands = ["py", "python", sys.executable]
        
        for python_cmd in python_commands:
            try:
                result = subprocess.run(
                    [
                        python_cmd, "-m", "pip", "install", "-r",
                        str(requirements_file)
                    ],
                    check=True,
                    capture_output=True,
                    text=True
                )
                # Success!
                return True
            except (subprocess.CalledProcessError, FileNotFoundError) as e:
                # Try next command
                continue
        
        # All attempts failed - offer manual installation
        response = messagebox.askyesno(
            "Dependency Installation",
            "Automatic dependency installation failed.\n\n"
            "Would you like to continue anyway?\n\n"
            "You can manually install dependencies later by running:\n"
            f"pip install -r {requirements_file}\n\n"
            "Click YES to continue, NO to cancel installation."
        )
        
        return response
        
    def create_windows_service(self):
        """Create Windows service for agent"""
        self.update_status("Creating Windows service...", 70)
        
        # Create ProgramData directory for logs (writable by services)
        programdata_dir = Path("C:/ProgramData/CampusCompute/logs")
        programdata_dir.mkdir(parents=True, exist_ok=True)
        
        # Try to install pywin32 first
        try:
            self.update_status("Installing service dependencies...", 72)
            result = subprocess.run(
                [sys.executable, "-m", "pip", "install", "pywin32"],
                capture_output=True,
                text=True,
                timeout=60
            )
            
            if result.returncode != 0:
                log_error = result.stderr if result.stderr else result.stdout
                raise Exception(f"Failed to install pywin32: {log_error}")
                
        except subprocess.TimeoutExpired:
            raise Exception("Timeout while installing pywin32")
        except Exception as e:
            messagebox.showwarning(
                "Service Dependency Failed",
                f"Failed to install service dependencies: {e}\n\n"
                "The agent was installed but the Windows service could not be created.\n"
                "You can start the agent manually by running:\n"
                f"python \"{self.install_dir / 'agent' / 'src' / 'main.py'}\""
            )
            return False
        
        # Create service wrapper script
        service_script = self.install_dir / "service_wrapper.py"
        
        service_code = f"""import win32serviceutil
import win32service
import win32event
import servicemanager
import subprocess
import sys
import os

class CampusComputeAgent(win32serviceutil.ServiceFramework):
    _svc_name_ = "CampusComputeAgent"
    _svc_display_name_ = "CampusCompute Agent"
    _svc_description_ = "CampusCompute resource pooling agent"
    
    def __init__(self, args):
        win32serviceutil.ServiceFramework.__init__(self, args)
        self.hWaitStop = win32event.CreateEvent(None, 0, 0, None)
        self.process = None
        
    def SvcStop(self):
        self.ReportServiceStatus(win32service.SERVICE_STOP_PENDING)
        win32event.SetEvent(self.hWaitStop)
        if self.process:
            self.process.terminate()
            
    def SvcDoRun(self):
        servicemanager.LogMsg(
            servicemanager.EVENTLOG_INFORMATION_TYPE,
            servicemanager.PYS_SERVICE_STARTED,
            (self._svc_name_, '')
        )
        
        # Set working directory to agent folder
        os.chdir(r"{str(self.install_dir / 'agent')}")
        
        # Start agent process
        self.process = subprocess.Popen([
            sys.executable,
            "src/main.py"
        ])
        
        win32event.WaitForSingleObject(self.hWaitStop, win32event.INFINITE)
        
if __name__ == '__main__':
    win32serviceutil.HandleCommandLine(CampusComputeAgent)
"""
        
        service_script.write_text(service_code)
        
        try:
            # Install service
            self.update_status("Registering Windows service...", 75)
            result = subprocess.run(
                [sys.executable, str(service_script), "install"],
                capture_output=True,
                text=True,
                timeout=30
            )
            
            if result.returncode != 0:
                log_error = result.stderr if result.stderr else result.stdout
                raise Exception(f"Service installation failed: {log_error}")
            
            # Configure service to start automatically
            self.update_status("Configuring service to start automatically...", 80)
            subprocess.run(
                ["sc", "config", "CampusComputeAgent", "start=", "auto"],
                capture_output=True,
                check=False  # Don't fail if this doesn't work
            )
            
            # Start service
            self.update_status("Starting CampusCompute Agent service...", 85)
            result = subprocess.run(
                [sys.executable, str(service_script), "start"],
                capture_output=True,
                text=True,
                timeout=30
            )
            
            if result.returncode != 0:
                # Service might already be running or needs manual start
                log_error = result.stderr if result.stderr else result.stdout
                messagebox.showinfo(
                    "Service Start",
                    f"Service installed but failed to start automatically.\n\n"
                    f"You can start it manually from Services (services.msc)\n"
                    f"or run: python \"{self.install_dir / 'agent' / 'src' / 'main.py'}\"\n\n"
                    f"Details: {log_error[:200]}"
                )
                return True  # Still consider installation successful
            
        except subprocess.TimeoutExpired:
            messagebox.showwarning(
                "Service Timeout",
                "Service creation timed out. The agent is installed but you'll need to start it manually."
            )
            return False
        except Exception as e:
            messagebox.showwarning(
                "Service Creation Failed",
                f"Failed to create Windows service: {e}\n\n"
                "The agent was installed successfully but needs to be started manually.\n\n"
                "You can run it with:\n"
                f"python \"{self.install_dir / 'agent' / 'src' / 'main.py'}\""
            )
            return False
            
        return True
    
    def start_agent_directly(self):
        """Start agent directly without service (fallback option)"""
        try:
            agent_main = self.install_dir / "agent" / "src" / "main.py"
            
            # Change to agent directory
            os.chdir(self.install_dir / "agent")
            
            # Start agent process in background
            startupinfo = subprocess.STARTUPINFO()
            startupinfo.dwFlags |= subprocess.STARTF_USESHOWWINDOW
            startupinfo.wShowWindow = subprocess.SW_HIDE
            
            subprocess.Popen(
                [sys.executable, str(agent_main)],
                cwd=str(self.install_dir / "agent"),
                startupinfo=startupinfo,
                creationflags=subprocess.CREATE_NEW_CONSOLE | subprocess.DETACHED_PROCESS
            )
            
            return True
        except Exception as e:
            messagebox.showerror("Start Failed", f"Failed to start agent: {e}")
            return False
        
    def perform_installation(self):
        """Main installation logic"""
        try:
            # Validate token
            if not self.token_var.get().strip():
                messagebox.showerror("Error", "Please enter an enrollment token")
                return
                
            # Check admin privileges
            if not self.check_admin():
                messagebox.showerror(
                    "Admin Required",
                    "This installer requires Administrator privileges.\n\n"
                    "Please right-click the installer and select 'Run as Administrator'."
                )
                return
                
            self.install_button.config(state=tk.DISABLED)
            
            # Check Docker
            self.update_status("Checking for Docker Desktop...", 10)
            docker_installed = self.check_docker()
            
            if not docker_installed:
                if self.install_docker_var.get():
                    response = messagebox.askyesno(
                        "Install Docker?",
                        "Docker Desktop is not installed.\n\n"
                        "Docker Desktop (500 MB) will be downloaded and installed.\n"
                        "This will take 10-15 minutes and require a restart.\n\n"
                        "Continue with Docker installation?"
                    )
                    
                    if response:
                        if not self.install_docker():
                            # Installation failed or user cancelled
                            self.install_button.config(state=tk.NORMAL)
                            self.update_status("Installation cancelled", 0)
                            return
                        # install_docker() exits the program after successful installation
                    else:
                        messagebox.showinfo(
                            "Docker Required",
                            "Docker Desktop is required for CampusCompute Agent.\n\n"
                            "Please install Docker Desktop manually from:\n"
                            "https://www.docker.com/products/docker-desktop\n\n"
                            "Then restart your computer and run this installer again."
                        )
                        self.install_button.config(state=tk.NORMAL)
                        return
                else:
                    messagebox.showerror(
                        "Docker Required",
                        "Docker Desktop is not installed.\n\n"
                        "Please either:\n"
                        "1. Enable 'Install Docker Desktop' option and try again, OR\n"
                        "2. Install Docker Desktop manually from docker.com"
                    )
                    self.install_button.config(state=tk.NORMAL)
                    return
            
            self.update_status("✓ Docker Desktop is installed", 15)
                    
            # Check Python
            self.update_status("Checking for Python 3.11+...", 15)
            python_installed = self.check_python()
            
            if not python_installed:
                if self.install_python_var.get():
                    response = messagebox.askyesno(
                        "Install Python?",
                        "Python 3.11+ is not installed.\n\n"
                        "Python 3.13 (25 MB) will be downloaded and installed.\n"
                        "This will take 2-3 minutes.\n\n"
                        "Continue with Python installation?"
                    )
                    
                    if response:
                        if not self.install_python():
                            # Installation failed
                            self.install_button.config(state=tk.NORMAL)
                            self.update_status("Installation cancelled", 0)
                            return
                    else:
                        messagebox.showinfo(
                            "Python Required",
                            "Python 3.11+ is required for CampusCompute Agent.\n\n"
                            "Please install Python manually from:\n"
                            "https://www.python.org/downloads/\n\n"
                            "Make sure to check 'Add Python to PATH' during installation.\n\n"
                            "Then run this installer again."
                        )
                        self.install_button.config(state=tk.NORMAL)
                        return
                else:
                    messagebox.showerror(
                        "Python Required",
                        "Python 3.11+ is not installed.\n\n"
                        "Please either:\n"
                        "1. Enable 'Install Python' option and try again, OR\n"
                        "2. Install Python manually from python.org"
                    )
                    self.install_button.config(state=tk.NORMAL)
                    return
            
            self.update_status("✓ Python is installed", 45)
                    
            # Install agent
            self.update_status("Installing CampusCompute Agent files...", 50)
            if not self.install_agent_files():
                self.install_button.config(state=tk.NORMAL)
                return
            
            # Create configuration
            self.update_status("Creating agent configuration...", 55)
            self.create_config()
            
            # Install dependencies
            self.update_status("Installing Python dependencies...", 60)
            if not self.install_dependencies():
                self.install_button.config(state=tk.NORMAL)
                return
                
            # Create Windows service
            self.update_status("Setting up Windows service...", 70)
            service_created = self.create_windows_service()
            
            # If service creation failed, offer to run directly
            if not service_created:
                response = messagebox.askyesno(
                    "Start Agent Now?",
                    "Windows service could not be created.\n\n"
                    "Would you like to start the agent now in the background?\n\n"
                    "Note: You'll need to start it manually after each reboot."
                )
                
                if response:
                    self.update_status("Starting agent...", 90)
                    if self.start_agent_directly():
                        self.update_status("Agent started!", 95)
                    else:
                        self.update_status("Agent installation complete (not running)", 95)
            
            # Success!
            self.update_status("✓ Installation complete!", 100)
            
            status_text = "running as a Windows service" if service_created else "installed (start it manually)"
            
            messagebox.showinfo(
                "🎉 Installation Complete!",
                f"CampusCompute Agent has been installed successfully!\n\n"
                f"📍 Device Name: {self.device_name_var.get()}\n"
                f"📂 Installation Path: {self.install_dir}\n"
                f"🔧 Status: Agent is {status_text}\n\n"
                f"✅ Your device should now appear in the admin dashboard.\n\n"
                f"Need help? Check the logs at:\n"
                f"C:\\ProgramData\\CampusCompute\\logs\\agent.log"
            )
            
            self.root.quit()
            
        except Exception as e:
            messagebox.showerror(
                "Installation Error",
                f"An unexpected error occurred:\n\n{e}\n\n"
                f"Please try again or contact support."
            )
            self.install_button.config(state=tk.NORMAL)
            self.update_status("Installation failed", 0)
            
    def start_installation(self):
        """Start installation in a thread"""
        # Show pre-flight check
        needs_docker = not self.check_docker()
        needs_python = not self.check_python()
        
        if needs_docker or needs_python:
            install_list = []
            if needs_docker and self.install_docker_var.get():
                install_list.append("• Docker Desktop (~500 MB, requires restart)")
            if needs_python and self.install_python_var.get():
                install_list.append("• Python 3.13 (~25 MB)")
            
            if install_list:
                install_text = "\n".join(install_list)
                response = messagebox.askyesno(
                    "Ready to Install",
                    f"The following will be automatically installed:\n\n{install_text}\n\n"
                    f"• CampusCompute Agent\n\n"
                    f"This may take 10-20 minutes depending on your internet speed.\n\n"
                    f"Continue?"
                )
                
                if not response:
                    return
        
        thread = threading.Thread(target=self.perform_installation)
        thread.daemon = True
        thread.start()
        
    def run(self):
        """Start the installer GUI"""
        self.root.mainloop()

if __name__ == "__main__":
    installer = AgentInstaller()
    installer.run()
