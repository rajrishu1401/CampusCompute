"""
WebSocket client for connecting agent to broker
"""

import asyncio
import json
import logging
from datetime import datetime
from typing import Optional, Callable, Dict, Any
import websockets
from websockets.exceptions import ConnectionClosed, WebSocketException

logger = logging.getLogger(__name__)


class BrokerWebSocketClient:
    """WebSocket client for broker communication"""
    
    def __init__(self, broker_url: str, device_id: int, message_handler: Callable):
        """
        Initialize WebSocket client
        
        Args:
            broker_url: WebSocket URL (e.g., ws://localhost:8081/ws/agent)
            device_id: Device ID for this agent
            message_handler: Async function to handle incoming messages
        """
        self.broker_url = broker_url
        self.device_id = device_id
        self.message_handler = message_handler
        self.websocket: Optional[websockets.WebSocketClientProtocol] = None
        self.connected = False
        self.reconnect_interval = 5  # seconds
        self.running = False
    
    async def connect(self):
        """Connect to broker WebSocket"""
        self.running = True
        
        while self.running:
            try:
                # Build URL with deviceId query param
                url = f"{self.broker_url}?deviceId={self.device_id}"
                
                logger.info(f"Connecting to broker: {url}")
                
                async with websockets.connect(
                    url,
                    ping_interval=30,
                    ping_timeout=10,
                    close_timeout=10,
                ) as websocket:
                    self.websocket = websocket
                    self.connected = True
                    
                    logger.info("✅ Connected to broker successfully")
                    
                    # Start receiving messages
                    await self._receive_loop()
                    
            except ConnectionClosed as e:
                logger.warning(f"Connection closed: {e}")
                self.connected = False
                self.websocket = None
                
            except WebSocketException as e:
                logger.error(f"WebSocket error: {e}")
                self.connected = False
                self.websocket = None
                
            except Exception as e:
                logger.error(f"Connection error: {e}")
                self.connected = False
                self.websocket = None
            
            # Reconnect after delay
            if self.running:
                logger.info(f"Reconnecting in {self.reconnect_interval} seconds...")
                await asyncio.sleep(self.reconnect_interval)
    
    async def disconnect(self):
        """Disconnect from broker"""
        logger.info("Disconnecting from broker...")
        self.running = False
        self.connected = False
        
        if self.websocket:
            await self.websocket.close()
            self.websocket = None
    
    async def send_message(self, message_type: str, payload: Optional[Dict[str, Any]] = None, 
                          request_id: Optional[str] = None, error: Optional[str] = None):
        """
        Send message to broker
        
        Args:
            message_type: Type of message (HEARTBEAT, CONTAINER_CREATED, etc.)
            payload: Message payload (optional)
            request_id: Request ID for request-response tracking (optional)
            error: Error message (optional)
        """
        if not self.connected or not self.websocket:
            logger.warning(f"Cannot send {message_type}: not connected")
            return False
        
        try:
            message = {
                'type': message_type,
                'deviceId': self.device_id,
                'timestamp': datetime.utcnow().isoformat(),
                'payload': payload or {},
            }
            
            if request_id:
                message['requestId'] = request_id
            
            if error:
                message['error'] = error
            
            # Send JSON message
            await self.websocket.send(json.dumps(message))
            logger.debug(f"Sent {message_type} message to broker")
            return True
            
        except Exception as e:
            logger.error(f"Failed to send message: {e}")
            return False
    
    async def _receive_loop(self):
        """Receive messages from broker"""
        try:
            async for message in self.websocket:
                try:
                    # Parse JSON message
                    data = json.loads(message)
                    
                    # Handle message
                    await self._handle_message(data)
                    
                except json.JSONDecodeError as e:
                    logger.error(f"Invalid JSON message: {e}")
                    
                except Exception as e:
                    logger.error(f"Error handling message: {e}")
                    
        except ConnectionClosed:
            logger.info("Connection closed by broker")
            self.connected = False
            
        except Exception as e:
            logger.error(f"Receive loop error: {e}")
            self.connected = False
    
    async def _handle_message(self, data: Dict[str, Any]):
        """
        Handle incoming message from broker
        
        Args:
            data: Parsed JSON message
        """
        message_type = data.get('type')
        request_id = data.get('requestId')
        payload = data.get('payload', {})
        
        logger.info(f"Received {message_type} message from broker")
        logger.debug(f"Message data: {data}")
        
        # Route to message handler
        try:
            result = await self.message_handler(message_type, payload, request_id)
            
            # Send response if handler returns result
            if result and isinstance(result, dict):
                response_type = result.get('type')
                response_payload = result.get('payload')
                response_error = result.get('error')
                
                if response_type:
                    await self.send_message(
                        response_type,
                        response_payload,
                        request_id,
                        response_error
                    )
                    
        except Exception as e:
            logger.error(f"Message handler error: {e}")
            
            # Send error response
            await self.send_message(
                'ERROR',
                None,
                request_id,
                str(e)
            )
    
    def is_connected(self) -> bool:
        """Check if connected to broker"""
        return self.connected and self.websocket is not None


if __name__ == "__main__":
    # Test WebSocket client
    logging.basicConfig(
        level=logging.DEBUG,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
    )
    
    async def test_handler(msg_type, payload, request_id):
        logger.info(f"Test handler: {msg_type} - {payload}")
        return None
    
    async def main():
        client = BrokerWebSocketClient(
            "ws://localhost:8081/ws/agent",
            1,
            test_handler
        )
        
        try:
            await client.connect()
        except KeyboardInterrupt:
            await client.disconnect()
    
    asyncio.run(main())
