"""
Docker container management for CampusCompute Agent
"""

import docker
from docker.errors import DockerException, NotFound, APIError
from typing import Dict, List, Optional, Any
import logging

logger = logging.getLogger(__name__)


class DockerManager:
    """Manage Docker containers on the agent"""
    
    def __init__(self, socket_url: str = "unix:///var/run/docker.sock"):
        """Initialize Docker client"""
        try:
            self.client = docker.DockerClient(base_url=socket_url)
            self.api_client = docker.APIClient(base_url=socket_url)
            
            # Test connection
            self.client.ping()
            logger.info("Docker client connected successfully")
            
        except DockerException as e:
            logger.error(f"Failed to connect to Docker: {e}")
            raise
    
    def get_docker_version(self) -> str:
        """Get Docker engine version"""
        try:
            version_info = self.client.version()
            return version_info.get('Version', 'unknown')
        except Exception as e:
            logger.error(f"Failed to get Docker version: {e}")
            return "unknown"
    
    def create_container(
        self,
        container_name: str,
        image: str,
        cpu_cores: int,
        ram_bytes: int,
        disk_bytes: int,
        environment: Optional[Dict[str, str]] = None,
    ) -> Dict[str, Any]:
        """
        Create a new Docker container with resource limits
        
        Args:
            container_name: Name for the container
            image: Docker image (e.g., 'ubuntu:24.04')
            cpu_cores: Number of CPU cores to allocate
            ram_bytes: RAM in bytes
            disk_bytes: Disk space in bytes
            environment: Environment variables
            
        Returns:
            Dict with container info (id, name, status)
        """
        try:
            # Pull image if not exists
            logger.info(f"Pulling image: {image}")
            self.client.images.pull(image)
            
            # Create container with resource limits
            logger.info(f"Creating container: {container_name}")
            container = self.client.containers.create(
                image=image,
                name=container_name,
                detach=True,
                tty=True,
                stdin_open=True,
                environment=environment or {},
                
                # Resource limits
                cpu_count=cpu_cores,
                mem_limit=ram_bytes,
                
                # Storage limit (using tmpfs for /tmp)
                tmpfs={'/tmp': f'size={disk_bytes}'},
                
                # Security: drop all capabilities
                cap_drop=['ALL'],
                
                # Network
                network_mode='bridge',
                
                # Read-only root filesystem (except volumes)
                # read_only=True,  # Uncomment for stricter security
                
                # Auto-remove on stop
                auto_remove=False,
            )
            
            # Start container
            container.start()
            logger.info(f"Container started: {container.id[:12]}")
            
            return {
                'container_id': container.id,
                'container_name': container_name,
                'status': 'running',
                'image': image,
            }
            
        except Exception as e:
            logger.error(f"Failed to create container: {e}")
            raise
    
    def stop_container(self, container_id: str, timeout: int = 10) -> bool:
        """Stop a running container"""
        try:
            container = self.client.containers.get(container_id)
            container.stop(timeout=timeout)
            logger.info(f"Container stopped: {container_id[:12]}")
            return True
            
        except NotFound:
            logger.warning(f"Container not found: {container_id}")
            return False
            
        except Exception as e:
            logger.error(f"Failed to stop container: {e}")
            return False
    
    def delete_container(self, container_id: str, force: bool = False) -> bool:
        """Delete a container"""
        try:
            container = self.client.containers.get(container_id)
            container.remove(force=force)
            logger.info(f"Container deleted: {container_id[:12]}")
            return True
            
        except NotFound:
            logger.warning(f"Container not found: {container_id}")
            return False
            
        except Exception as e:
            logger.error(f"Failed to delete container: {e}")
            return False
    
    def get_container_status(self, container_id: str) -> Optional[str]:
        """Get container status"""
        try:
            container = self.client.containers.get(container_id)
            return container.status
            
        except NotFound:
            return None
            
        except Exception as e:
            logger.error(f"Failed to get container status: {e}")
            return None
    
    def list_containers(self, all: bool = False) -> List[Dict[str, Any]]:
        """List all containers on this agent"""
        try:
            containers = self.client.containers.list(all=all)
            
            return [
                {
                    'container_id': c.id,
                    'name': c.name,
                    'status': c.status,
                    'image': c.image.tags[0] if c.image.tags else 'unknown',
                }
                for c in containers
            ]
            
        except Exception as e:
            logger.error(f"Failed to list containers: {e}")
            return []
    
    def get_container_stats(self, container_id: str) -> Optional[Dict[str, Any]]:
        """Get real-time container resource usage"""
        try:
            container = self.client.containers.get(container_id)
            stats = container.stats(stream=False)
            
            # Calculate CPU percentage
            cpu_delta = stats['cpu_stats']['cpu_usage']['total_usage'] - \
                       stats['precpu_stats']['cpu_usage']['total_usage']
            system_delta = stats['cpu_stats']['system_cpu_usage'] - \
                          stats['precpu_stats']['system_cpu_usage']
            cpu_percent = (cpu_delta / system_delta) * 100.0 if system_delta > 0 else 0.0
            
            # Memory usage
            mem_usage = stats['memory_stats'].get('usage', 0)
            mem_limit = stats['memory_stats'].get('limit', 0)
            mem_percent = (mem_usage / mem_limit) * 100.0 if mem_limit > 0 else 0.0
            
            return {
                'cpu_percent': cpu_percent,
                'memory_bytes': mem_usage,
                'memory_percent': mem_percent,
                'network_rx_bytes': stats['networks']['eth0']['rx_bytes'],
                'network_tx_bytes': stats['networks']['eth0']['tx_bytes'],
            }
            
        except Exception as e:
            logger.error(f"Failed to get container stats: {e}")
            return None
    
    def cleanup_stopped_containers(self) -> int:
        """Remove all stopped containers"""
        try:
            containers = self.client.containers.list(
                filters={'status': 'exited'}
            )
            
            count = 0
            for container in containers:
                container.remove()
                count += 1
            
            logger.info(f"Cleaned up {count} stopped containers")
            return count
            
        except Exception as e:
            logger.error(f"Failed to cleanup containers: {e}")
            return 0


if __name__ == "__main__":
    # Test Docker manager
    logging.basicConfig(level=logging.INFO)
    
    dm = DockerManager()
    print(f"Docker version: {dm.get_docker_version()}")
    
    # List containers
    containers = dm.list_containers(all=True)
    print(f"\nContainers: {len(containers)}")
    for c in containers:
        print(f"  - {c['name']}: {c['status']}")
