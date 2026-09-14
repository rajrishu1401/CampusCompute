"""
CampusCompute Agent - Core Logic

Manages containers and communicates with broker
"""

import asyncio
import json
import logging
from datetime import datetime
from typing import Dict, Any, Optional

from config import Config
from docker_manager import DockerManager
from system_monitor import SystemMonitor

logger = logging.getLogger(__name__)


class CampusComputeAgent:
    """Main agent class"""
    
    def __init__(self, config: Config):
        self.config = config
        self.running = False
        
        # Initialize managers
        self.docker_manager = DockerManager(config.docker_socket)
        self.system_monitor = SystemMonitor()
        
        # Connection state
        self.connected = False
        self.websocket = None
        
        # Containers managed by this agent
        self.containers: Dict[str, Dict[str, Any]] = {}
        
        # Tasks
        self.heartbeat_task = None
        self.metrics_task = None
        self.container_check_task = None
    
    async def start(self):
        """Start the agent"""
        self.running = True
        logger.info("Starting CampusCompute Agent...")
        
        # Get initial system info
        metrics = self.system_monitor.get_all_metrics()
        logger.info(f"System: {metrics['system']['platform']} {metrics['system']['platform_release']}")
        logger.info(f"CPU: {metrics['cpu']['total_cores']} cores")
        logger.info(f"RAM: {metrics['memory']['total_bytes'] / (1024**3):.2f} GB")
        logger.info(f"Docker: {self.docker_manager.get_docker_version()}")
        
        # Start background tasks
        self.heartbeat_task = asyncio.create_task(self._heartbeat_loop())
        self.metrics_task = asyncio.create_task(self._metrics_loop())
        self.container_check_task = asyncio.create_task(self._container_check_loop())
        
        # TODO: Connect to broker via WebSocket
        logger.info("Agent started (broker connection not implemented yet)")
    
    async def stop(self):
        """Stop the agent"""
        logger.info("Stopping agent...")
        self.running = False
        
        # Cancel tasks
        if self.heartbeat_task:
            self.heartbeat_task.cancel()
        if self.metrics_task:
            self.metrics_task.cancel()
        if self.container_check_task:
            self.container_check_task.cancel()
        
        # Close WebSocket
        if self.websocket:
            await self.websocket.close()
        
        logger.info("Agent stopped")
    
    async def _heartbeat_loop(self):
        """Send heartbeat to broker periodically"""
        while self.running:
            try:
                await self._send_heartbeat()
                await asyncio.sleep(self.config.heartbeat_interval)
            except asyncio.CancelledError:
                break
            except Exception as e:
                logger.error(f"Heartbeat error: {e}")
    
    async def _metrics_loop(self):
        """Send system metrics to broker periodically"""
        interval = self.config.get('monitoring.metrics_interval', 60)
        
        while self.running:
            try:
                await self._send_metrics()
                await asyncio.sleep(interval)
            except asyncio.CancelledError:
                break
            except Exception as e:
                logger.error(f"Metrics error: {e}")
    
    async def _container_check_loop(self):
        """Check container status periodically"""
        interval = self.config.get('monitoring.container_check_interval', 10)
        
        while self.running:
            try:
                await self._check_containers()
                await asyncio.sleep(interval)
            except asyncio.CancelledError:
                break
            except Exception as e:
                logger.error(f"Container check error: {e}")
    
    async def _send_heartbeat(self):
        """Send heartbeat message to broker"""
        metrics = self.system_monitor.get_all_metrics()
        
        message = {
            'type': 'HEARTBEAT',
            'device_id': self.config.device_id,
            'timestamp': datetime.utcnow().isoformat(),
            'data': {
                'hostname': self.config.hostname,
                'lab_name': self.config.lab_name,
                'status': 'ONLINE',
                'cpu': metrics['cpu'],
                'memory': metrics['memory'],
                'disk': metrics['disk'],
                'docker_version': self.docker_manager.get_docker_version(),
                'agent_version': '1.0.0',
                'container_count': len(self.containers),
            }
        }
        
        # TODO: Send via WebSocket
        logger.debug(f"Heartbeat: CPU={metrics['cpu']['current_load_percent']:.1f}% "
                    f"RAM={metrics['memory']['used_percent']:.1f}%")
    
    async def _send_metrics(self):
        """Send detailed metrics to broker"""
        metrics = self.system_monitor.get_all_metrics()
        
        message = {
            'type': 'METRICS',
            'device_id': self.config.device_id,
            'timestamp': datetime.utcnow().isoformat(),
            'data': metrics,
        }
        
        # TODO: Send via WebSocket
        logger.info("Metrics sent to broker")
    
    async def _check_containers(self):
        """Check status of managed containers"""
        for container_id, info in list(self.containers.items()):
            try:
                status = self.docker_manager.get_container_status(container_id)
                
                if status != info.get('status'):
                    logger.info(f"Container {container_id[:12]} status changed: "
                              f"{info.get('status')} -> {status}")
                    info['status'] = status
                    
                    # Notify broker
                    await self._send_container_status(container_id, status)
                    
            except Exception as e:
                logger.error(f"Failed to check container {container_id}: {e}")
    
    async def _send_container_status(self, container_id: str, status: str):
        """Send container status update to broker"""
        message = {
            'type': 'CONTAINER_STATUS',
            'device_id': self.config.device_id,
            'timestamp': datetime.utcnow().isoformat(),
            'data': {
                'container_id': container_id,
                'status': status,
            }
        }
        
        # TODO: Send via WebSocket
        logger.debug(f"Container status update: {container_id[:12]} = {status}")
    
    async def handle_create_container(self, data: Dict[str, Any]) -> Dict[str, Any]:
        """Handle CREATE_CONTAINER command from broker"""
        try:
            container_name = data['container_name']
            image = data['image']
            cpu_cores = data['cpu_cores']
            ram_bytes = data['ram_bytes']
            disk_bytes = data['disk_bytes']
            
            logger.info(f"Creating container: {container_name}")
            
            # Check resource availability
            if not self.system_monitor.is_resource_available(cpu_cores, ram_bytes):
                return {
                    'success': False,
                    'error': 'Insufficient resources available'
                }
            
            # Create container
            result = self.docker_manager.create_container(
                container_name=container_name,
                image=image,
                cpu_cores=cpu_cores,
                ram_bytes=ram_bytes,
                disk_bytes=disk_bytes,
            )
            
            # Track container
            self.containers[result['container_id']] = {
                'name': container_name,
                'image': image,
                'status': result['status'],
                'created_at': datetime.utcnow().isoformat(),
            }
            
            logger.info(f"Container created successfully: {result['container_id'][:12]}")
            
            return {
                'success': True,
                'container_id': result['container_id'],
            }
            
        except Exception as e:
            logger.error(f"Failed to create container: {e}")
            return {
                'success': False,
                'error': str(e)
            }
    
    async def handle_stop_container(self, data: Dict[str, Any]) -> Dict[str, Any]:
        """Handle STOP_CONTAINER command from broker"""
        try:
            container_id = data['container_id']
            
            logger.info(f"Stopping container: {container_id[:12]}")
            
            success = self.docker_manager.stop_container(container_id)
            
            if success and container_id in self.containers:
                self.containers[container_id]['status'] = 'stopped'
            
            return {'success': success}
            
        except Exception as e:
            logger.error(f"Failed to stop container: {e}")
            return {
                'success': False,
                'error': str(e)
            }
    
    async def handle_delete_container(self, data: Dict[str, Any]) -> Dict[str, Any]:
        """Handle DELETE_CONTAINER command from broker"""
        try:
            container_id = data['container_id']
            
            logger.info(f"Deleting container: {container_id[:12]}")
            
            success = self.docker_manager.delete_container(container_id, force=True)
            
            if success and container_id in self.containers:
                del self.containers[container_id]
            
            return {'success': success}
            
        except Exception as e:
            logger.error(f"Failed to delete container: {e}")
            return {
                'success': False,
                'error': str(e)
            }
