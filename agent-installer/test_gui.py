"""
Simple test version of the installer GUI
Run this to see what the installer will look like
"""

import tkinter as tk
from tkinter import ttk, messagebox
import socket

class InstallerPreview:
    def __init__(self):
        self.root = tk.Tk()
        self.root.title("CampusCompute Agent Installer - Preview")
        self.root.geometry("600x550")
        self.root.resizable(False, False)
        
        self.setup_ui()
        
    def setup_ui(self):
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
        
        self.token_var = tk.StringVar()
        tk.Entry(
            content_frame,
            textvariable=self.token_var,
            width=60,
            font=("Arial", 10)
        ).grid(row=2, column=0, pady=(0, 15), ipady=5)
        
        # Device name input
        tk.Label(
            content_frame,
            text="Device Name (optional):",
            font=("Arial", 10, "bold")
        ).grid(row=3, column=0, sticky=tk.W, pady=(0, 5))
        
        self.device_name_var = tk.StringVar(value=socket.gethostname())
        tk.Entry(
            content_frame,
            textvariable=self.device_name_var,
            width=60,
            font=("Arial", 10)
        ).grid(row=4, column=0, pady=(0, 15), ipady=5)
        
        # Backend URL
        tk.Label(
            content_frame,
            text="Backend URL (advanced):",
            font=("Arial", 10, "bold")
        ).grid(row=5, column=0, sticky=tk.W, pady=(0, 5))
        
        self.backend_url_var = tk.StringVar(value="ws://localhost:8081/ws/agent")
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
        
        self.install_docker_var = tk.BooleanVar(value=True)
        tk.Checkbutton(
            content_frame,
            text="Install Docker Desktop (if not installed)",
            variable=self.install_docker_var,
            font=("Arial", 9)
        ).grid(row=8, column=0, sticky=tk.W)
        
        self.install_python_var = tk.BooleanVar(value=True)
        tk.Checkbutton(
            content_frame,
            text="Install Python 3.11 (if not installed)",
            variable=self.install_python_var,
            font=("Arial", 9)
        ).grid(row=9, column=0, sticky=tk.W, pady=(0, 15))
        
        # Progress bar
        self.progress_var = tk.DoubleVar()
        ttk.Progressbar(
            content_frame,
            variable=self.progress_var,
            maximum=100,
            length=560
        ).grid(row=10, column=0, pady=(10, 5))
        
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
        
        tk.Button(
            button_frame,
            text="Install (Demo)",
            command=self.demo_install,
            bg="#1976d2",
            fg="white",
            font=("Arial", 10, "bold"),
            width=15,
            height=2
        ).pack(side=tk.LEFT, padx=5)
        
        tk.Button(
            button_frame,
            text="Cancel",
            command=self.root.quit,
            font=("Arial", 10),
            width=15,
            height=2
        ).pack(side=tk.LEFT, padx=5)
        
    def demo_install(self):
        """Simulate installation process"""
        if not self.token_var.get().strip():
            messagebox.showerror("Error", "Please enter an enrollment token")
            return
            
        # Simulate progress
        import time
        steps = [
            (10, "Checking Docker installation..."),
            (20, "Checking Python installation..."),
            (40, "Installing agent files..."),
            (60, "Installing dependencies..."),
            (80, "Creating Windows service..."),
            (100, "Installation complete!")
        ]
        
        for progress, status in steps:
            self.progress_var.set(progress)
            self.status_label.config(text=status)
            self.root.update()
            time.sleep(0.5)
        
        messagebox.showinfo(
            "Success!",
            f"CampusCompute Agent installed successfully!\n\n"
            f"Device Name: {self.device_name_var.get()}\n"
            f"The agent is now running as a Windows service.\n\n"
            f"Check your admin dashboard to see this device."
        )
        
    def run(self):
        self.root.mainloop()

if __name__ == "__main__":
    print("🎨 CampusCompute Installer - Preview Mode")
    print("=" * 50)
    print("This is a preview of the installer GUI.")
    print("Click 'Install (Demo)' to see the installation simulation.")
    print("=" * 50)
    print()
    
    app = InstallerPreview()
    app.run()
