"""
Build script to create Windows .exe installer
Requires: pip install pyinstaller
"""

import PyInstaller.__main__
import shutil
from pathlib import Path

def build_exe():
    """Build the installer as a standalone .exe"""
    
    print("Building CampusCompute Agent Installer...")
    
    # PyInstaller arguments
    PyInstaller.__main__.run([
        'installer.py',
        '--name=CampusCompute-Agent-Installer',
        '--onefile',
        '--windowed',
        '--icon=icon.ico',  # Add an icon file
        '--add-data=../agent;agent',  # Include agent files
        '--hidden-import=win32timezone',
        '--hidden-import=pywintypes',
        '--hidden-import=win32api',
        '--hidden-import=win32service',
        '--hidden-import=win32serviceutil',
        '--hidden-import=win32event',
        '--clean',
    ])
    
    print("\n✅ Build complete!")
    print("Installer created: dist/CampusCompute-Agent-Installer.exe")
    print("\nYou can now distribute this .exe file to administrators.")

if __name__ == "__main__":
    build_exe()
