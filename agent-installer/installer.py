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
        # Check for single instance
        self.single_instance = SingleInstance("CampusComputeAgentInstaller_Mutex")
        if self.single_instance.is_already_running():
            messagebox.showerror(
                "Already Running",
                "CampusCompute Agent Installer is already running.\n\n"
                "Please close the existing installer window first."
            )
            sys.exit(1)
        
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
            text="Installation Options:",
            font=("Arial", 10, "bold")
        ).grid(row=7, column=0, sticky=tk.W, pady=(0, 5))
        
        tk.Checkbutton(
            content_frame,
            text="Install Docker Desktop (if not installed)",
            variable=self.install_docker_var,
            font=("Arial", 9)
        ).grid(row=8, column=0, sticky=tk.W)
        
        tk.Checkbutton(
            content_frame,
            text="Install Python 3.13 (if not installed)",
            variable=self.install_python_var,
            font=("Arial", 9)
        ).grid(row=9, column=0, sticky=tk.W, pady=(0, 15))
        
        # Progress bar
        self.progress_var = tk.DoubleVar()
        self.progress_bar = ttk.Progressbar(
            content_frame,
            variable=self.progress_var,
            maximum=100,
            length=560
        )
        self.progress_bar.grid(row=10, column=0, pady=(10, 5))
        
        # Status label
        self.status_label = tk.Label(
            content_frame,
            text="Ready to install",
            font=("Arial", 9),
            fg="gray"
        )
        self.status_label.grid(row=11, column=0, pady=(0, 15))
        
        # Buttons
        button_frame = tk.Frame(content_frame)
        button_frame.grid(row=12, column=0)
        
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
        self.update_status("Downloading Docker Desktop...", 20)
        
        docker_installer = self.install_dir.parent / "Docker-Desktop-Installer.exe"
        docker_url = "https://desktop.docker.com/win/stable/Docker%20Desktop%20Installer.exe"
        
        try:
            urllib.request.urlretrieve(docker_url, docker_installer)
            
            self.update_status("Installing Docker Desktop (this may take a few minutes)...", 30)
            
            # Use hidden window to prevent spawning issues
            startupinfo = subprocess.STARTUPINFO()
            startupinfo.dwFlags |= subprocess.STARTF_USESHOWWINDOW
            startupinfo.wShowWindow = subprocess.SW_HIDE
            
            subprocess.run(
                [str(docker_installer), "install", "--quiet"],
                check=True,
                startupinfo=startupinfo,
                creationflags=subprocess.CREATE_NO_WINDOW
            )
            
            docker_installer.unlink()
            
            messagebox.showinfo(
                "Docker Installed",
                "Docker Desktop has been installed. Please restart your computer and run this installer again."
            )
            sys.exit(0)
            
        except Exception as e:
            messagebox.showerror("Error", f"Failed to install Docker: {e}")
            return False
            
        return True
        
    def install_python(self):
        """Download and install Python"""
        self.update_status("Downloading Python 3.13...", 20)
        
        python_installer = self.install_dir.parent / "python-3.13-installer.exe"
        # Python 3.13.3 (latest stable as of Oct 2026)
        python_url = "https://www.python.org/ftp/python/3.13.3/python-3.13.3-amd64.exe"
        
        try:
            # Download with progress
            self.update_status("Downloading Python 3.13... (30 MB)", 20)
            urllib.request.urlretrieve(python_url, python_installer)
            
            self.update_status("Installing Python 3.13... This may take 2-3 minutes", 30)
            
            # Install Python with all features
            # Use CREATE_NEW_CONSOLE to prevent spawning issues
            startupinfo = subprocess.STARTUPINFO()
            startupinfo.dwFlags |= subprocess.STARTF_USESHOWWINDOW
            startupinfo.wShowWindow = subprocess.SW_HIDE
            
            result = subprocess.run(
                [
                    str(python_installer),
                    "/quiet",                    # Silent installation
                    "InstallAllUsers=1",         # Install for all users
                    "PrependPath=1",             # Add to PATH
                    "Include_pip=1",             # Include pip
                    "Include_test=0",            # Skip tests
                    "Include_doc=0",             # Skip docs
                    "Include_dev=0",             # Skip dev files
                    "AssociateFiles=1",          # Associate .py files
                ],
                capture_output=True,
                text=True,
                startupinfo=startupinfo,
                creationflags=subprocess.CREATE_NO_WINDOW
            )
            
            # Clean up installer
            if python_installer.exists():
                python_installer.unlink()
            
            if result.returncode != 0:
                raise Exception(f"Python installer returned code {result.returncode}")
                
            # Verify installation
            self.update_status("Verifying Python installation...", 40)
            if not self.check_python():
                raise Exception("Python was installed but cannot be found. You may need to restart.")
            
            messagebox.showinfo(
                "Python Installed",
                "Python 3.13 has been installed successfully!"
            )
            
        except Exception as e:
            messagebox.showerror("Error", f"Failed to install Python: {e}\n\nPlease install Python manually from python.org")
            return False
            
        return True
        
    def create_config(self):
        """Create agent configuration file"""
        config_content = f"""broker:
  url: "{self.backend_url_var.get()}"
  enrollment_token: "{self.token_var.get()}"

device:
  device_id: "{self.device_name_var.get()}"
  hostname: "{self.device_name_var.get()}"

docker:
  socket: "npipe:////./pipe/docker_engine"

logging:
  level: "INFO"
  file: "{str(self.install_dir / 'logs' / 'agent.log')}"
"""
        
        self.config_file.parent.mkdir(parents=True, exist_ok=True)
        self.config_file.write_text(config_content)
        
    def install_agent_files(self):
        """Copy agent files to installation directory"""
        self.update_status("Installing agent files...", 50)
        
        # Create directories
        (self.install_dir / "agent").mkdir(parents=True, exist_ok=True)
        (self.install_dir / "logs").mkdir(parents=True, exist_ok=True)
        
        # Get the bundled agent files from PyInstaller temp directory
        if getattr(sys, 'frozen', False):
            # Running as compiled exe - use PyInstaller's temp directory
            bundle_dir = Path(sys._MEIPASS)
            agent_src_dir = bundle_dir / "agent" / "src"
            requirements_src = bundle_dir / "agent" / "requirements.txt"
        else:
            # Running as script - use relative path from project
            agent_src_dir = Path("agent/src")
            requirements_src = Path("agent/requirements.txt")
        
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
        
        # Install NSSM (Non-Sucking Service Manager) or use Python service wrapper
        service_script = self.install_dir / "service_wrapper.py"
        
        service_code = f"""import win32serviceutil
import win32service
import win32event
import servicemanager
import subprocess
import sys

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
        
        self.process = subprocess.Popen([
            sys.executable,
            r"{str(self.install_dir / 'agent' / 'src' / 'main.py')}"
        ])
        
        win32event.WaitForSingleObject(self.hWaitStop, win32event.INFINITE)
        
if __name__ == '__main__':
    win32serviceutil.HandleCommandLine(CampusComputeAgent)
"""
        
        service_script.write_text(service_code)
        
        try:
            # Install pywin32 if not already installed
            subprocess.run(
                ["python", "-m", "pip", "install", "pywin32"],
                check=True,
                capture_output=True
            )
            
            # Install service
            subprocess.run(
                ["python", str(service_script), "install"],
                check=True
            )
            
            # Start service
            subprocess.run(
                ["python", str(service_script), "start"],
                check=True
            )
            
        except subprocess.CalledProcessError as e:
            messagebox.showwarning(
                "Service Creation Failed",
                "Failed to create Windows service. You can start the agent manually."
            )
            return False
            
        return True
        
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
                    "Please run this installer as Administrator"
                )
                return
                
            self.install_button.config(state=tk.DISABLED)
            
            # Check Docker
            self.update_status("Checking Docker installation...", 10)
            if not self.check_docker():
                if self.install_docker_var.get():
                    if not self.install_docker():
                        return
                else:
                    messagebox.showerror(
                        "Docker Required",
                        "Docker is not installed. Please install Docker Desktop or enable the option to install it."
                    )
                    return
                    
            # Check Python
            self.update_status("Checking Python installation...", 15)
            if not self.check_python():
                if self.install_python_var.get():
                    if not self.install_python():
                        return
                else:
                    messagebox.showerror(
                        "Python Required",
                        "Python is not installed. Please install Python 3.11+ or enable the option to install it."
                    )
                    return
                    
            # Install agent
            if not self.install_agent_files():
                self.install_button.config(state=tk.NORMAL)
                return
            
            # Create configuration
            self.update_status("Creating configuration...", 55)
            self.create_config()
            
            # Install dependencies
            if not self.install_dependencies():
                return
                
            # Create Windows service
            self.create_windows_service()
            
            # Success!
            self.update_status("Installation complete!", 100)
            
            messagebox.showinfo(
                "Success!",
                f"CampusCompute Agent installed successfully!\n\n"
                f"Device Name: {self.device_name_var.get()}\n"
                f"The agent is now running as a Windows service.\n\n"
                f"Check your admin dashboard to see this device."
            )
            
            self.root.quit()
            
        except Exception as e:
            messagebox.showerror("Installation Error", f"An error occurred: {e}")
            self.install_button.config(state=tk.NORMAL)
            self.update_status("Installation failed", 0)
            
    def start_installation(self):
        """Start installation in a thread"""
        thread = threading.Thread(target=self.perform_installation)
        thread.daemon = True
        thread.start()
        
    def run(self):
        """Start the installer GUI"""
        self.root.mainloop()

if __name__ == "__main__":
    installer = AgentInstaller()
    installer.run()
