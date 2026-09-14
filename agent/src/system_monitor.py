"""
System resource monitoring for CampusCompute Agent
"""

import psutil
import platform
from typing import Dict, Any
from datetime import datetime


class SystemMonitor:
    """Monitor system resources (CPU, RAM, disk)"""
    
    def __init__(self):
        self.boot_time = datetime.fromtimestamp(psutil.boot_time())
    
    def get_cpu_info(self) -> Dict[str, Any]:
        """Get CPU information and current load"""
        cpu_percent = psutil.cpu_percent(interval=1, percpu=False)
        cpu_count = psutil.cpu_count(logical=True)
        cpu_freq = psutil.cpu_freq()
        
        return {
            'total_cores': cpu_count,
            'current_load_percent': cpu_percent,
            'frequency_mhz': cpu_freq.current if cpu_freq else 0,
        }
    
    def get_memory_info(self) -> Dict[str, Any]:
        """Get memory (RAM) information"""
        mem = psutil.virtual_memory()
        
        return {
            'total_bytes': mem.total,
            'available_bytes': mem.available,
            'used_bytes': mem.used,
            'used_percent': mem.percent,
        }
    
    def get_disk_info(self) -> Dict[str, Any]:
        """Get disk information"""
        disk = psutil.disk_usage('/')
        
        return {
            'total_bytes': disk.total,
            'available_bytes': disk.free,
            'used_bytes': disk.used,
            'used_percent': disk.percent,
        }
    
    def get_network_info(self) -> Dict[str, Any]:
        """Get network information"""
        net_io = psutil.net_io_counters()
        
        return {
            'bytes_sent': net_io.bytes_sent,
            'bytes_recv': net_io.bytes_recv,
            'packets_sent': net_io.packets_sent,
            'packets_recv': net_io.packets_recv,
        }
    
    def get_system_info(self) -> Dict[str, Any]:
        """Get general system information"""
        return {
            'platform': platform.system(),
            'platform_release': platform.release(),
            'platform_version': platform.version(),
            'architecture': platform.machine(),
            'processor': platform.processor(),
            'boot_time': self.boot_time.isoformat(),
        }
    
    def get_all_metrics(self) -> Dict[str, Any]:
        """Get all system metrics in one call"""
        return {
            'timestamp': datetime.utcnow().isoformat(),
            'cpu': self.get_cpu_info(),
            'memory': self.get_memory_info(),
            'disk': self.get_disk_info(),
            'network': self.get_network_info(),
            'system': self.get_system_info(),
        }
    
    def is_resource_available(self, cpu_cores: int, ram_bytes: int) -> bool:
        """Check if requested resources are available"""
        cpu_info = self.get_cpu_info()
        mem_info = self.get_memory_info()
        
        # Check if CPU is not overloaded
        if cpu_info['current_load_percent'] > 80:
            return False
        
        # Check if enough CPU cores
        if cpu_cores > cpu_info['total_cores']:
            return False
        
        # Check if enough RAM available
        if ram_bytes > mem_info['available_bytes']:
            return False
        
        return True


if __name__ == "__main__":
    # Test system monitor
    monitor = SystemMonitor()
    metrics = monitor.get_all_metrics()
    
    print("=== System Metrics ===")
    print(f"CPU Cores: {metrics['cpu']['total_cores']}")
    print(f"CPU Load: {metrics['cpu']['current_load_percent']}%")
    print(f"RAM Total: {metrics['memory']['total_bytes'] / (1024**3):.2f} GB")
    print(f"RAM Used: {metrics['memory']['used_percent']}%")
    print(f"Disk Total: {metrics['disk']['total_bytes'] / (1024**3):.2f} GB")
    print(f"Disk Used: {metrics['disk']['used_percent']}%")
    print(f"Platform: {metrics['system']['platform']} {metrics['system']['platform_release']}")
