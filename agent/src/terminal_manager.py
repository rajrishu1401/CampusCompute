"""
Terminal Manager for Docker container terminal access
Manages PTY sessions for interactive shell access to containers
"""

import asyncio
import logging
import docker
from typing import Dict, Optional, Any

logger = logging.getLogger(__name__)


class TerminalSession:
    """Represents a single terminal session to a container"""
    
    def __init__(self, container_id: str, terminal_session_id: str, 
                 cols: int = 80, rows: int = 24):
        self.container_id = container_id
        self.terminal_session_id = terminal_session_id
        self.cols = cols
        self.rows = rows
        self.exec_instance = None
        self.socket = None
        self.running = False
        self.output_task = None
    
    async def start(self, docker_client, on_output_callback):
        """Start the terminal session"""
        try:
            container = docker_client.containers.get(self.container_id)
            
            # Create exec instance for interactive shell
            self.exec_instance = docker_client.api.exec_create(
                container.id,
                ['/bin/sh'],  # Try sh first, fallback to bash
                stdin=True,
                tty=True,
                environment={'TERM': 'xterm-256color'},
                workdir=None
            )
            
            # Start exec and get socket
            self.socket = docker_client.api.exec_start(
                self.exec_instance['Id'],
                detach=False,
                tty=True,
                stream=True,
                socket=True,
                demux=False
            )
            
            self.running = True
            
            # Start output reading task
            self.output_task = asyncio.create_task(
                self._read_output_loop(on_output_callback)
            )
            
            logger.info(f"Terminal session {self.terminal_session_id} started for container {self.container_id[:12]}")
            
        except docker.errors.NotFound:
            logger.error(f"Container not found: {self.container_id[:12]}")
            raise
        except Exception as e:
            logger.error(f"Failed to start terminal session: {e}")
            raise
    
    async def _read_output_loop(self, on_output_callback):
        """Read output from terminal and send to callback"""
        try:
            while self.running:
                # Read in chunks to avoid blocking
                try:
                    # socket._sock is the underlying socket
                    output = await asyncio.get_event_loop().run_in_executor(
                        None,
                        lambda: self.socket._sock.recv(4096)
                    )
                    
                    if not output:
                        logger.info(f"Terminal session {self.terminal_session_id} ended (no output)")
                        break
                    
                    # Decode and send output
                    text = output.decode('utf-8', errors='replace')
                    await on_output_callback(self.terminal_session_id, text)
                    
                except Exception as e:
                    logger.error(f"Error reading terminal output: {e}")
                    break
                    
        except asyncio.CancelledError:
            logger.info(f"Terminal session {self.terminal_session_id} read loop cancelled")
        finally:
            self.running = False
    
    async def send_input(self, data: str):
        """Send input to terminal"""
        try:
            if self.socket and self.running:
                # Encode and send input
                await asyncio.get_event_loop().run_in_executor(
                    None,
                    lambda: self.socket._sock.sendall(data.encode('utf-8'))
                )
        except Exception as e:
            logger.error(f"Error sending terminal input: {e}")
    
    async def resize(self, cols: int, rows: int):
        """Resize terminal"""
        try:
            self.cols = cols
            self.rows = rows
            # Docker doesn't have direct resize API for exec sessions
            # This would require using docker.types.Host Config
            logger.debug(f"Terminal resize requested: {cols}x{rows} (not implemented)")
        except Exception as e:
            logger.error(f"Error resizing terminal: {e}")
    
    async def stop(self):
        """Stop the terminal session"""
        self.running = False
        
        if self.output_task:
            self.output_task.cancel()
            try:
                await self.output_task
            except asyncio.CancelledError:
                pass
        
        if self.socket:
            try:
                self.socket.close()
            except:
                pass
        
        logger.info(f"Terminal session {self.terminal_session_id} stopped")


class TerminalManager:
    """Manages multiple terminal sessions"""
    
    def __init__(self, docker_client):
        self.docker_client = docker_client
        self.sessions: Dict[str, TerminalSession] = {}
    
    async def attach_terminal(self, container_id: str, terminal_session_id: str,
                             cols: int, rows: int, on_output_callback) -> bool:
        """
        Attach a terminal session to a container
        
        Args:
            container_id: Docker container ID
            terminal_session_id: Unique session identifier
            cols: Terminal columns
            rows: Terminal rows
            on_output_callback: Async callback(session_id, output) for terminal output
            
        Returns:
            bool: True if successful, False otherwise
        """
        try:
            if terminal_session_id in self.sessions:
                logger.warning(f"Terminal session {terminal_session_id} already exists")
                return False
            
            session = TerminalSession(container_id, terminal_session_id, cols, rows)
            await session.start(self.docker_client, on_output_callback)
            
            self.sessions[terminal_session_id] = session
            return True
            
        except Exception as e:
            logger.error(f"Failed to attach terminal: {e}")
            return False
    
    async def send_input(self, terminal_session_id: str, data: str) -> bool:
        """Send input to a terminal session"""
        session = self.sessions.get(terminal_session_id)
        if not session:
            logger.warning(f"Terminal session not found: {terminal_session_id}")
            return False
        
        await session.send_input(data)
        return True
    
    async def resize_terminal(self, terminal_session_id: str, cols: int, rows: int) -> bool:
        """Resize a terminal session"""
        session = self.sessions.get(terminal_session_id)
        if not session:
            logger.warning(f"Terminal session not found: {terminal_session_id}")
            return False
        
        await session.resize(cols, rows)
        return True
    
    async def detach_terminal(self, terminal_session_id: str) -> bool:
        """Detach a terminal session"""
        session = self.sessions.pop(terminal_session_id, None)
        if not session:
            logger.warning(f"Terminal session not found: {terminal_session_id}")
            return False
        
        await session.stop()
        return True
    
    async def detach_all(self):
        """Detach all terminal sessions"""
        for terminal_session_id in list(self.sessions.keys()):
            await self.detach_terminal(terminal_session_id)
    
    def get_session_count(self) -> int:
        """Get number of active terminal sessions"""
        return len(self.sessions)
