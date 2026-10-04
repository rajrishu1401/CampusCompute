"""
CampusCompute Agent - Main Entry Point

Runs on lab computers to manage Docker containers
"""

import asyncio
import logging
import signal
import sys
from pathlib import Path
import os

from config import load_config
from agent import CampusComputeAgent


# Configure logging (basic console logging first, will be updated after config load)
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.StreamHandler(sys.stdout),
    ]
)

logger = logging.getLogger(__name__)


def print_banner():
    """Print startup banner"""
    banner = """
====================================================
          CampusCompute Agent Started                 
     Campus-Aware Cloud Resource Pooling System       
                                                       
     Agent v1.0.0                                     
====================================================
"""
    print(banner)


async def main():
    """Main entry point"""
    print_banner()
    
    # Load configuration
    try:
        config = load_config()
        
        # Setup file logging now that we have config
        log_file = config.get('logging', {}).get('file', 'logs/agent.log')
        log_file_path = Path(log_file)
        log_file_path.parent.mkdir(parents=True, exist_ok=True)
        
        # Add file handler
        file_handler = logging.FileHandler(log_file_path, mode='a')
        file_handler.setLevel(logging.INFO)
        file_handler.setFormatter(logging.Formatter(
            '%(asctime)s - %(name)s - %(levelname)s - %(message)s'
        ))
        logging.getLogger().addHandler(file_handler)
        
        logger.info(f"Configuration loaded from config.yaml")
        logger.info(f"Device ID: {config.device_id}")
        logger.info(f"Lab Name: {config.lab_name}")
        logger.info(f"Broker URL: {config.broker_url}")
    except Exception as e:
        logger.error(f"Failed to load configuration: {e}")
        return 1
    
    # Create agent
    agent = CampusComputeAgent(config)
    
    # Setup signal handlers for graceful shutdown
    def signal_handler(sig, frame):
        logger.info(f"Received signal {sig}, shutting down...")
        asyncio.create_task(agent.stop())
    
    signal.signal(signal.SIGINT, signal_handler)
    signal.signal(signal.SIGTERM, signal_handler)
    
    # Start agent
    try:
        await agent.start()
        logger.info("Agent started successfully")
        
        # Keep running
        while agent.running:
            await asyncio.sleep(1)
            
    except KeyboardInterrupt:
        logger.info("Keyboard interrupt received")
    except Exception as e:
        logger.error(f"Agent error: {e}", exc_info=True)
        return 1
    finally:
        await agent.stop()
        logger.info("Agent stopped")
    
    return 0


if __name__ == "__main__":
    sys.exit(asyncio.run(main()))
