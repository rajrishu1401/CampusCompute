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
from websocket_client import BrokerWebSocketClient

logger = logging.getLogger(__name__)


class CampusComputeAgent:
    """Main agent class"""
    
    def __init__(self, config: Config):
        self.config = config
        self.running = False
        
        # Initialize managers
        self.docker_manager = DockerManager(config.docker_socket)
        self.system_monitor = SystemMonitor()
        
        # WebSocket client
        self.ws_client = None
        
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
        
        # Initialize WebSocket client
        broker_url = self.config.get('broker.url', 'ws://localhost:8081/ws/agent')
        device_id = self.config.get('device.id')
        
        if not device_id:
            logger.error("Device ID not configured! Please set device.id in config.yaml")
            return
        
        logger.info(f"Connecting to broker: {broker_url} (Device ID: {device_id})")
        
        self.ws_client = BrokerWebSocketClient(
            broker_url=broker_url,
            device_id=device_id,
            message_handler=self._handle_broker_message
        )
        
        # Start background tasks
        self.heartbeat_task = asyncio.create_task(self._heartbeat_loop())
        self.metrics_task = asyncio.create_task(self._metrics_loop())
        self.container_check_task = asyncio.create_task(self._container_check_loop())
        
        # Connect to broker (this will run in background and auto-reconnect)
        asyncio.create_task(self.ws_client.connect())
        
        logger.info("✅ Agent started successfully")
    
    async def _handle_broker_message(self, message_type: str, payload: Dict[str, Any], 
                                     request_id: Optional[str]) -> Optional[Dict[str, Any]]:
        """
        Handle messages from broker
        
        Args:
            message_type: Type of message
            payload: Message payload
            request_id: Request ID for tracking
            
        Returns:
            Response dict with type, payload, and optional error
        """
        logger.info(f"Handling broker message: {message_type}")
        
        try:
            if message_type == 'CREATE_CONTAINER':
                result = await self.handle_create_container(payload)
                
                if result['success']:
                    return {
                        'type': 'CONTAINER_CREATED',
                        'payload': {
                            'container_id': result['container_id'],
                            'status': 'running'
                        }
                    }
                else:
                    return {
                        'type': 'CONTAINER_FAILED',
                        'payload': {},
                        'error': result.get('error', 'Unknown error')
                    }
            
            elif message_type == 'STOP_CONTAINER':
                result = await self.handle_stop_container(payload)
                return {
                    'type': 'CONTAINER_STOPPED',
                    'payload': result
                }
            
            elif message_type == 'DELETE_CONTAINER':
                result = await self.handle_delete_container(payload)
                return {
                    'type': 'CONTAINER_DELETED',
                    'payload': result
                }
            
            elif message_type == 'PING':
                return {
                    'type': 'PONG',
                    'payload': {'timestamp': datetime.utcnow().isoformat()}
                }
            
            elif message_type == 'ACK':
                logger.debug("Received ACK from broker")
                return None
            
            else:
                logger.warning(f"Unknown message type: {message_type}")
                return {
                    'type': 'ERROR',
                    'payload': {},
                    'error': f'Unknown message type: {message_type}'
                }
                
        except Exception as e:
            logger.error(f"Error handling message: {e}", exc_info=True)
            return {
                'type': 'ERROR',
                'payload': {},
                'error': str(e)
            }
    
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
        if self.ws_client:
            await self.ws_client.disconnect()
        
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
        if not self.ws_client or not self.ws_client.is_connected():
            logger.debug("Skipping heartbeat - not connected")
            return
        
        metrics = self.system_monitor.get_all_metrics()
        
        payload = {
            'hostname': self.config.hostname,
            'lab_name': self.config.lab_name,
            'status': 'ONLINE',
            'cpu_percent': metrics['cpu']['current_load_percent'],
            'ram_used_bytes': metrics['memory']['used_bytes'],
            'disk_used_bytes': metrics['disk']['used_bytes'],
            'container_count': len(self.containers),
            'docker_version': self.docker_manager.get_docker_version(),
            'agent_version': '1.0.0',
        }
        
        await self.ws_client.send_message('HEARTBEAT', payload)
        
        logger.debug(f"Heartbeat sent: CPU={metrics['cpu']['current_load_percent']:.1f}% "
                    f"RAM={metrics['memory']['used_percent']:.1f}%")
    
    async def _send_metrics(self):
        """Send detailed metrics to broker"""
        if not self.ws_client or not self.ws_client.is_connected():
            return
        
        metrics = self.system_monitor.get_all_metrics()
        await self.ws_client.send_message('METRICS_UPDATE', metrics)
        
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
        if not self.ws_client or not self.ws_client.is_connected():
            return
        
        payload = {
            'container_id': container_id,
            'status': status,
        }
        
        await self.ws_client.send_message('CONTAINER_STATUS', payload)
        logger.debug(f"Container status update: {container_id[:12]} = {status}")
    
    async def handle_create_container(self, data: Dict[str, Any]) -> Dict[str, Any]:
        """Handle CREATE_CONTAINER command from broker"""
        try:
            # Extract container spec from payload
            container_name = data.get('containerName')
            image = data.get('image')
            cpu_cores = data.get('cpuCores')
            ram_bytes = data.get('ramBytes')
            disk_bytes = data.get('diskBytes')
            
            logger.info(f"Creating container: {container_name} (image: {image})")
            logger.info(f"Resources: {cpu_cores} cores, {ram_bytes / (1024**3):.2f} GB RAM")
            
            # Check resource availability
            if not self.system_monitor.is_resource_available(cpu_cores, ram_bytes):
                logger.warning("Insufficient resources available")
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
            
            logger.info(f"✅ Container created successfully: {result['container_id'][:12]}")
            
            return {
                'success': True,
                'container_id': result['container_id'],
            }
            
        except Exception as e:
            logger.error(f"❌ Failed to create container: {e}", exc_info=True)
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
