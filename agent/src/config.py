"""
Configuration loader for CampusCompute Agent
"""

import os
import yaml
import socket
from pathlib import Path
from typing import Dict, Any


class Config:
    """Agent configuration manager"""
    
    def __init__(self, config_path: str = "config.yaml"):
        self.config_path = config_path
        self.data: Dict[str, Any] = {}
        self.load()
        
    def load(self):
        """Load configuration from YAML file"""
        config_file = Path(self.config_path)
        
        if not config_file.exists():
            raise FileNotFoundError(f"Configuration file not found: {self.config_path}")
        
        with open(config_file, 'r') as f:
            self.data = yaml.safe_load(f)
        
        # Auto-detect hostname if not set
        if not self.data['device'].get('hostname'):
            self.data['device']['hostname'] = socket.gethostname()
        
        # Auto-detect device_id if not set
        if not self.data['device'].get('device_id'):
            self.data['device']['device_id'] = socket.gethostname()
    
    def get(self, key: str, default: Any = None) -> Any:
        """Get configuration value by dot-notation key"""
        keys = key.split('.')
        value = self.data
        
        for k in keys:
            if isinstance(value, dict):
                value = value.get(k)
            else:
                return default
        
        return value if value is not None else default
    
    @property
    def broker_url(self) -> str:
        return self.get('broker.url')
    
    @property
    def device_id(self) -> str:
        return self.get('device.device_id')
    
    @property
    def lab_name(self) -> str:
        return self.get('device.lab_name')
    
    @property
    def hostname(self) -> str:
        return self.get('device.hostname')
    
    @property
    def heartbeat_interval(self) -> int:
        return self.get('monitoring.heartbeat_interval', 30)
    
    @property
    def mtls_enabled(self) -> bool:
        return self.get('mtls.enabled', False)
    
    @property
    def docker_socket(self) -> str:
        return self.get('docker.socket', 'unix:///var/run/docker.sock')


# Global config instance
config: Config = None


def load_config(config_path: str = "config.yaml") -> Config:
    """Load configuration and return global instance"""
    global config
    config = Config(config_path)
    return config


def get_config() -> Config:
    """Get global configuration instance"""
    global config
    if config is None:
        config = load_config()
    return config
