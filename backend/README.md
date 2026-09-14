# CampusCompute Backend (Spring Boot)

Central broker service for the CampusCompute system.

## Technology Stack
- Java 17
- Spring Boot 3.2
- PostgreSQL 15
- Redis 7
- Spring Security + JWT
- Spring WebSocket

## Project Structure
```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/campuscompute/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── entity/          # JPA entities
│   │   │   ├── exception/       # Custom exceptions
│   │   │   ├── repository/      # JPA repositories
│   │   │   ├── security/        # Security configuration
│   │   │   ├── service/         # Business logic
│   │   │   ├── websocket/       # WebSocket handlers
│   │   │   └── CampusComputeApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-dev.yml
│   └── test/
├── pom.xml
└── README.md
```

## Features
- Authentication & Authorization (JWT)
- Device Management
- Container Lifecycle Management
- Adaptive Scheduling Algorithm
- WebSocket Communication (Agents & Clients)
- Lab Reservation System
- User & Quota Management

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 15+
- Redis 7+

### Run Locally
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### API Endpoints
- `POST /api/auth/login` - User authentication
- `GET /api/containers` - List containers
- `POST /api/containers` - Create container
- `DELETE /api/containers/{id}` - Delete container
- `WS /ws/agent` - Agent WebSocket
- `WS /ws/terminal/{containerId}` - Terminal WebSocket
