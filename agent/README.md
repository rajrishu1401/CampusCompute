# CampusCompute Agent (Python)

Agent software that runs on lab computers to manage Docker containers.

## Technology Stack
- Python 3.11+
- Docker SDK for Python
- WebSocket client
- psutil (system monitoring)
- mTLS authentication

## Project Structure
```
agent/
├── src/
│   ├── __init__.py
│   ├── main.py              # Entry point
│   ├── config.py            # Configuration
│   ├── agent.py             # Main agent logic
│   ├── docker_manager.py    # Docker operations
│   ├── system_monitor.py    # System metrics
│   ├── websocket_client.py  # WebSocket communication
│   └── auth/
│       ├── __init__.py
│       └── mtls.py          # mTLS certificate handling
├── requirements.txt
├── setup.py
├── config.yaml              # Agent configuration
└── README.md
```

## Features
- Automatic broker connection via WebSocket
- Container lifecycle management (create, stop, delete)
- System resource monitoring (CPU, RAM, disk)
- Heartbeat mechanism
- mTLS authentication
- Docker API integration
- Systemd service integration

## Installation

### Prerequisites
- Python 3.11+
- Docker Engine 24+
- pip

### Install Dependencies
```bash
cd agent
pip install -r requirements.txt
```

### Configure
Edit `config.yaml`:
```yaml
broker:
  url: wss://campuscompute.upes.ac.in/ws/agent
  
device:
  device_id: "CS-LAB-PC-001"
  lab_name: "Computer Science Lab"
```

### Run Agent
```bash
python src/main.py
```

### Install as Service (Linux)
```bash
sudo python setup.py install-service
sudo systemctl enable campuscompute-agent
sudo systemctl start campuscompute-agent
```

## Message Types

### Agent → Broker
- `HEARTBEAT` - Regular health check
- `STATUS_UPDATE` - Container status changes
- `METRICS` - System resource metrics

### Broker → Agent
- `CREATE_CONTAINER` - Create new container
- `STOP_CONTAINER` - Stop running container
- `DELETE_CONTAINER` - Remove container
- `GET_METRICS` - Request system metrics
