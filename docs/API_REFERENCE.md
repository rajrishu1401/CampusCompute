# CampusCompute - API Reference

**Version**: 1.0  
**Base URL**: `http://your-domain.com:8081/api`  
**Last Updated**: October 3, 2026  

---

## Table of Contents

1. [Overview](#overview)
2. [Authentication](#authentication)
3. [Organization Management](#organization-management)
4. [Device Management](#device-management)
5. [Student Management](#student-management)
6. [Container Management](#container-management)
7. [Lab Management](#lab-management)
8. [Health Check](#health-check)
9. [WebSocket API](#websocket-api)
10. [Error Codes](#error-codes)
11. [Rate Limiting](#rate-limiting)

---

## Overview

### Base URL

All API endpoints are relative to the base URL:
```
http://your-domain.com:8081/api
```

For local development:
```
http://localhost:8081/api
```

### Request Format

- **Content-Type**: `application/json`
- **Accept**: `application/json`
- **Authorization**: `Bearer <jwt-token>` (for authenticated endpoints)

### Response Format

All responses follow a consistent structure:

**Success Response:**
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... }
}
```

**Error Response:**
```json
{
  "success": false,
  "message": "Error description",
  "timestamp": "2026-10-03T20:30:45.123Z",
  "path": "/api/endpoint"
}
```

### HTTP Status Codes

| Code | Meaning | Usage |
|------|---------|-------|
| 200 | OK | Request succeeded |
| 201 | Created | Resource created successfully |
| 400 | Bad Request | Invalid request parameters |
| 401 | Unauthorized | Authentication required or failed |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Resource already exists |
| 500 | Internal Server Error | Server error |

---

## Authentication

### Register Organization

Create a new organization with admin account.

**Endpoint**: `POST /organizations/register`  
**Auth Required**: No  
**Role Required**: None (public endpoint)

**Request Body:**
```json
{
  "name": "University of Petroleum and Energy Studies",
  "code": "UPES2026",
  "adminFullName": "Dr. John Smith",
  "email": "admin@upes.ac.in",
  "password": "SecurePass@123"
}
```

**Field Validation:**
- `name`: Required, 3-255 characters
- `code`: Required, 3-50 characters, alphanumeric, unique
- `adminFullName`: Required, 3-255 characters
- `email`: Required, valid email format, unique
- `password`: Required, min 8 characters

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Organization registered successfully",
  "data": {
    "organizationId": 1,
    "organizationName": "University of Petroleum and Energy Studies",
    "organizationCode": "UPES2026",
    "adminUserId": 1,
    "adminEmail": "admin@upes.ac.in"
  }
}
```

**Error Responses:**
- `400`: Validation errors (missing/invalid fields)
- `409`: Organization code or email already exists

---

### Login

Authenticate and receive JWT token.

**Endpoint**: `POST /auth/login`  
**Auth Required**: No  
**Role Required**: None

**Request Body:**
```json
{
  "email": "admin@upes.ac.in",
  "password": "SecurePass@123"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "username": "admin@upes.ac.in",
      "email": "admin@upes.ac.in",
      "role": "ORG_ADMIN",
      "organizationId": 1
    }
  }
}
```

**Token Details:**
- **Type**: JWT (JSON Web Token)
- **Expiration**: 24 hours (86400000 ms)
- **Usage**: Include in `Authorization` header as `Bearer <token>`

**Error Responses:**
- `401`: Invalid credentials
- `400`: Missing email or password

---

### First-Time Student Login

Activate student account and set password.

**Endpoint**: `POST /auth/first-login`  
**Auth Required**: No  
**Role Required**: None

**Request Body:**
```json
{
  "studentId": "500101234",
  "email": "john.doe@upes.ac.in",
  "password": "MyNewPass@123"
}
```

**Validation:**
- Student must exist in database (uploaded by admin)
- Email must match uploaded email
- Student must not already be approved

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Account activated successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 5,
      "username": "500101234",
      "email": "john.doe@upes.ac.in",
      "role": "STUDENT",
      "organizationId": 1
    }
  }
}
```

**Error Responses:**
- `401`: Invalid student ID or email
- `400`: Student already activated
- `400`: Validation errors

---

## Organization Management

All organization endpoints require `ORG_ADMIN` role.

### Get Organization Statistics

Get comprehensive statistics for your organization.

**Endpoint**: `GET /organizations/stats`  
**Auth Required**: Yes  
**Role Required**: `ORG_ADMIN`

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "organizationName": "University of Petroleum and Energy Studies",
    "organizationCode": "UPES2026",
    "subscriptionTier": "FREE",
    "devices": {
      "total": 10,
      "online": 8,
      "offline": 2,
      "busy": 3,
      "maintenance": 0
    },
    "students": {
      "total": 150,
      "approved": 145,
      "pending": 5,
      "active": 120
    },
    "containers": {
      "total": 65,
      "running": 45,
      "stopped": 15,
      "pending": 5
    },
    "resources": {
      "cpuTotal": 40,
      "cpuUsed": 28,
      "cpuAvailable": 12,
      "cpuUtilizationPercent": 70.0,
      "ramTotalGb": 160,
      "ramUsedGb": 112,
      "ramAvailableGb": 48,
      "ramUtilizationPercent": 70.0,
      "diskTotalGb": 2048,
      "diskUsedGb": 856,
      "diskAvailableGb": 1192,
      "diskUtilizationPercent": 41.8
    }
  }
}
```

**Error Responses:**
- `401`: Not authenticated
- `403`: Not an admin

---

### List Devices

Get all devices with detailed information.

**Endpoint**: `GET /organizations/devices`  
**Auth Required**: Yes  
**Role Required**: `ORG_ADMIN`

**Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "deviceId": "LAB-01-PC-15",
      "hostname": "Lab 1 - Computer 15",
      "status": "ONLINE",
      "ipAddress": "192.168.1.101",
      "cpuCores": 4,
      "cpuCoresAvailable": 2,
      "cpuUtilizationPercent": 50.0,
      "ramGb": 16,
      "ramAvailableGb": 8,
      "ramUtilizationPercent": 50.0,
      "diskGb": 256,
      "diskAvailableGb": 128,
      "diskUtilizationPercent": 50.0,
      "activeContainers": 2,
      "dockerVersion": "24.0.7",
      "agentVersion": "1.0.0",
      "osInfo": "Windows 11 Pro",
      "lastHeartbeat": "2026-10-03T20:30:45.123Z",
      "createdAt": "2026-10-01T10:00:00.000Z"
    },
    {
      "id": 2,
      "deviceId": "LAB-02-PC-08",
      "hostname": "Lab 2 - Computer 8",
      "status": "OFFLINE",
      "ipAddress": "192.168.1.102",
      "cpuCores": 4,
      "ramGb": 8,
      "diskGb": 128,
      "activeContainers": 0,
      "lastHeartbeat": "2026-10-03T18:15:30.456Z",
      "createdAt": "2026-10-01T11:30:00.000Z"
    }
  ]
}
```

**Status Values:**
- `ONLINE`: Device is connected and available
- `OFFLINE`: Device hasn't sent heartbeat in > 90 seconds
- `BUSY`: Device is at full capacity
- `MAINTENANCE`: Manually marked for maintenance

---

### Generate Enrollment Token

Generate a new token for device enrollment.

**Endpoint**: `POST /organizations/devices/token`  
**Auth Required**: Yes  
**Role Required**: `ORG_ADMIN`

**Request Body:** None

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Enrollment token generated successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJvcmdJZCI6MSwiZXhwIjoxNzI4MDc0NDAwfQ.xyz123",
    "organizationId": 1,
    "organizationCode": "UPES2026",
    "expiresAt": "2026-10-04T20:30:00.000Z",
    "backendUrl": "ws://your-domain.com:8081/ws/agent",
    "installScript": "curl -sSL https://install.campuscompute.edu/agent.sh | bash -s eyJhbG..."
  }
}
```

**Token Details:**
- **Expiration**: 24 hours
- **Single-use**: Token is consumed on first device registration
- **Security**: Tokens are organization-specific

**Error Responses:**
- `401`: Not authenticated
- `403`: Not an admin

---

### List Students

Get all students with usage statistics.

**Endpoint**: `GET /organizations/students`  
**Auth Required**: Yes  
**Role Required**: `ORG_ADMIN`

**Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 5,
      "studentId": "500101234",
      "email": "john.doe@upes.ac.in",
      "fullName": "John Doe",
      "status": "APPROVED",
      "totalContainers": 8,
      "activeContainers": 2,
      "stoppedContainers": 4,
      "deletedContainers": 2,
      "cpuHoursUsed": 45.5,
      "ramGbHoursUsed": 128.3,
      "storageGbUsed": 35.2,
      "lastLogin": "2026-10-03T14:30:00.000Z",
      "createdAt": "2026-10-01T12:00:00.000Z"
    },
    {
      "id": 6,
      "studentId": "500101235",
      "email": "jane.smith@upes.ac.in",
      "fullName": "Jane Smith",
      "status": "PENDING",
      "totalContainers": 0,
      "activeContainers": 0,
      "createdAt": "2026-10-02T09:15:00.000Z"
    }
  ]
}
```

**Status Values:**
- `PENDING`: Account created, password not set
- `APPROVED`: Account activated, can use system
- `SUSPENDED`: Account temporarily disabled
- `INACTIVE`: No login for 90+ days

---

### Upload Students (Bulk)

Upload multiple students via CSV format.

**Endpoint**: `POST /organizations/students/upload`  
**Auth Required**: Yes  
**Role Required**: `ORG_ADMIN`

**Request Body:**
```json
{
  "csvData": "500101234,john.doe@upes.ac.in,John Doe\n500101235,jane.smith@upes.ac.in,Jane Smith\n500101236,bob.wilson@upes.ac.in,Bob Wilson"
}
```

**CSV Format:**
- No header row
- Three columns: `student_id,email,full_name`
- One student per line
- No quotes around values
- Comma-separated

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Students uploaded successfully",
  "data": {
    "totalProcessed": 3,
    "successCount": 3,
    "failureCount": 0,
    "students": [
      {
        "id": 5,
        "studentId": "500101234",
        "email": "john.doe@upes.ac.in",
        "fullName": "John Doe",
        "status": "PENDING"
      },
      {
        "id": 6,
        "studentId": "500101235",
        "email": "jane.smith@upes.ac.in",
        "fullName": "Jane Smith",
        "status": "PENDING"
      },
      {
        "id": 7,
        "studentId": "500101236",
        "email": "bob.wilson@upes.ac.in",
        "fullName": "Bob Wilson",
        "status": "PENDING"
      }
    ]
  }
}
```

**Error Responses:**
- `400`: Invalid CSV format
- `409`: Duplicate student ID or email

---

## Container Management

Container endpoints are available to `STUDENT` role.

### Create Container

Request a new container.

**Endpoint**: `POST /containers`  
**Auth Required**: Yes  
**Role Required**: `STUDENT`

**Request Body:**
```json
{
  "imageName": "ubuntu:latest",
  "cpuCores": 2,
  "ramGb": 4,
  "diskGb": 20
}
```

**Field Validation:**
- `imageName`: Required, valid Docker image
- `cpuCores`: Required, 1-8
- `ramGb`: Required, 1-16
- `diskGb`: Optional, default 10, max 100

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Container creation initiated",
  "data": {
    "id": 123,
    "imageName": "ubuntu:latest",
    "status": "PENDING",
    "cpuCores": 2,
    "ramGb": 4,
    "diskGb": 20,
    "userId": 5,
    "deviceId": null,
    "containerId": null,
    "createdAt": "2026-10-03T20:30:45.123Z"
  }
}
```

**Container Status Flow:**
```
PENDING → RUNNING → STOPPED → DELETED
```

**Error Responses:**
- `400`: Invalid parameters
- `403`: Quota exceeded
- `503`: No devices available

---

### List Containers

Get all containers for current user.

**Endpoint**: `GET /containers`  
**Auth Required**: Yes  
**Role Required**: `STUDENT`

**Query Parameters:**
- `status` (optional): Filter by status (`RUNNING`, `STOPPED`, `PENDING`)
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "containers": [
      {
        "id": 123,
        "imageName": "ubuntu:latest",
        "status": "RUNNING",
        "cpuCores": 2,
        "ramGb": 4,
        "diskGb": 20,
        "containerId": "abc123def456",
        "deviceId": 1,
        "deviceHostname": "Lab 1 - Computer 15",
        "createdAt": "2026-10-03T20:30:45.123Z",
        "startedAt": "2026-10-03T20:31:15.456Z",
        "uptimeSeconds": 3600
      },
      {
        "id": 124,
        "imageName": "python:3.11",
        "status": "STOPPED",
        "cpuCores": 1,
        "ramGb": 2,
        "diskGb": 10,
        "containerId": "xyz789abc123",
        "deviceId": 2,
        "deviceHostname": "Lab 2 - Computer 8",
        "createdAt": "2026-10-02T14:00:00.000Z",
        "startedAt": "2026-10-02T14:00:30.000Z",
        "stoppedAt": "2026-10-03T10:00:00.000Z"
      }
    ],
    "totalCount": 2,
    "page": 0,
    "size": 20
  }
}
```

---

### Get Container Details

Get detailed information about a specific container.

**Endpoint**: `GET /containers/{id}`  
**Auth Required**: Yes  
**Role Required**: `STUDENT` (own containers only)

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 123,
    "imageName": "ubuntu:latest",
    "status": "RUNNING",
    "cpuCores": 2,
    "ramGb": 4,
    "diskGb": 20,
    "containerId": "abc123def456",
    "deviceId": 1,
    "deviceHostname": "Lab 1 - Computer 15",
    "deviceIpAddress": "192.168.1.101",
    "terminalUrl": "ws://your-domain.com:8081/ws/terminal/abc123def456",
    "cpuUsagePercent": 35.5,
    "ramUsageMb": 1024,
    "diskUsageMb": 5120,
    "networkRxBytes": 1048576,
    "networkTxBytes": 524288,
    "createdAt": "2026-10-03T20:30:45.123Z",
    "startedAt": "2026-10-03T20:31:15.456Z",
    "uptimeSeconds": 3600
  }
}
```

**Error Responses:**
- `404`: Container not found
- `403`: Not your container

---

### Stop Container

Stop a running container.

**Endpoint**: `POST /containers/{id}/stop`  
**Auth Required**: Yes  
**Role Required**: `STUDENT` (own containers only)

**Request Body:** None

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Container stop initiated",
  "data": {
    "id": 123,
    "status": "STOPPED"
  }
}
```

**Error Responses:**
- `404`: Container not found
- `400`: Container not running
- `403`: Not your container

---

### Start Container

Start a stopped container.

**Endpoint**: `POST /containers/{id}/start`  
**Auth Required**: Yes  
**Role Required**: `STUDENT` (own containers only)

**Request Body:** None

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Container start initiated",
  "data": {
    "id": 123,
    "status": "RUNNING"
  }
}
```

**Error Responses:**
- `404`: Container not found
- `400`: Container already running
- `403`: Not your container

---

### Delete Container

Permanently delete a container.

**Endpoint**: `DELETE /containers/{id}`  
**Auth Required**: Yes  
**Role Required**: `STUDENT` (own containers only)

**Request Body:** None

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Container deleted successfully"
}
```

**Warning**: This action is irreversible. All data in the container will be lost.

**Error Responses:**
- `404`: Container not found
- `403`: Not your container

---

## Lab Management

Lab management endpoints for `ORG_ADMIN` role.

### Create Lab

Create a new lab in your organization.

**Endpoint**: `POST /labs`  
**Auth Required**: Yes  
**Role Required**: `ORG_ADMIN`

**Request Body:**
```json
{
  "labName": "Computer Lab 3",
  "location": "Engineering Block, Floor 2",
  "capacity": 30
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Lab created successfully",
  "data": {
    "id": 3,
    "labName": "Computer Lab 3",
    "location": "Engineering Block, Floor 2",
    "capacity": 30,
    "organizationId": 1,
    "createdAt": "2026-10-03T20:30:45.123Z"
  }
}
```

---

### List Labs

Get all labs for your organization.

**Endpoint**: `GET /labs`  
**Auth Required**: Yes  
**Role Required**: `ORG_ADMIN`

**Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "labName": "Computer Lab 1",
      "location": "Main Building, Floor 1",
      "capacity": 40,
      "deviceCount": 38,
      "onlineDeviceCount": 35,
      "organizationId": 1
    },
    {
      "id": 2,
      "labName": "Computer Lab 2",
      "location": "Engineering Block, Floor 1",
      "capacity": 25,
      "deviceCount": 24,
      "onlineDeviceCount": 20,
      "organizationId": 1
    }
  ]
}
```

---

## Health Check

### System Health

Check system health status.

**Endpoint**: `GET /health`  
**Auth Required**: No  
**Role Required**: None

**Response (200 OK):**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "SELECT 1"
      }
    },
    "redis": {
      "status": "UP",
      "details": {
        "version": "7.0.15"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 1099511627776,
        "free": 549755813888,
        "threshold": 10485760
      }
    }
  }
}
```

---

## WebSocket API

### Agent WebSocket

Agents connect to backend via WebSocket for bidirectional communication.

**Endpoint**: `ws://your-domain.com:8081/ws/agent`  
**Protocol**: WebSocket  
**Auth**: Enrollment token (first connection) or device ID (subsequent)

**Connection Flow:**
1. Agent opens WebSocket connection
2. Agent sends enrollment/authentication message
3. Backend validates and responds
4. Bidirectional message exchange begins

**Message Format:**
```json
{
  "type": "HEARTBEAT | CONTAINER_CREATE | CONTAINER_STOP | ...",
  "deviceId": "LAB-01-PC-15",
  "organizationId": 1,
  "timestamp": "2026-10-03T20:30:45.123Z",
  "data": { ... }
}
```

**Agent → Backend Messages:**
- `ENROLLMENT`: First-time device registration
- `HEARTBEAT`: Periodic status update
- `METRICS`: System resource metrics
- `CONTAINER_STATUS`: Container status update
- `ERROR`: Error reporting

**Backend → Agent Messages:**
- `CONTAINER_CREATE`: Create new container
- `CONTAINER_STOP`: Stop container
- `CONTAINER_START`: Start container
- `CONTAINER_DELETE`: Delete container
- `CONFIG_UPDATE`: Configuration changes

---

### Terminal WebSocket

Students connect to container terminals via WebSocket.

**Endpoint**: `ws://your-domain.com:8081/ws/terminal/{containerId}`  
**Protocol**: WebSocket  
**Auth**: JWT token in query parameter or header

**Connection Flow:**
1. Frontend opens WebSocket with JWT token
2. Backend validates token and container ownership
3. Backend proxies to agent's container TTY
4. Bidirectional terminal data streaming

**Message Format:**
- **Input**: Plain text (user keystrokes)
- **Output**: Plain text (terminal output)
- **Binary**: Supported for special characters

---

## Error Codes

### Common Errors

| Code | Name | Description | Solution |
|------|------|-------------|----------|
| `AUTH_001` | Invalid Credentials | Email or password incorrect | Check credentials |
| `AUTH_002` | Token Expired | JWT token has expired | Login again |
| `AUTH_003` | Invalid Token | JWT token is malformed | Login again |
| `AUTH_004` | Insufficient Permissions | User lacks required role | Contact admin |
| `ORG_001` | Organization Not Found | Organization doesn't exist | Verify organization ID |
| `ORG_002` | Duplicate Organization Code | Code already in use | Choose different code |
| `DEV_001` | Device Not Found | Device doesn't exist | Check device ID |
| `DEV_002` | Device Offline | Device not connected | Check agent status |
| `DEV_003` | Invalid Enrollment Token | Token expired or invalid | Generate new token |
| `STU_001` | Student Not Found | Student doesn't exist | Verify student ID |
| `STU_002` | Duplicate Student | Student ID/email exists | Check existing students |
| `STU_003` | Already Approved | Student already activated | Use normal login |
| `CON_001` | Container Not Found | Container doesn't exist | Check container ID |
| `CON_002` | Quota Exceeded | User exceeded limits | Stop other containers |
| `CON_003` | No Devices Available | All devices offline/busy | Wait or add devices |
| `CON_004` | Insufficient Resources | Requested resources unavailable | Reduce request |
| `CON_005` | Container Creation Failed | Docker error | Check agent logs |

---

## Rate Limiting

**Current Status**: Not implemented (planned for v2.0)

**Planned Limits:**
- Authentication endpoints: 10 requests/minute
- Container operations: 60 requests/minute
- Statistics/list endpoints: 100 requests/minute
- WebSocket connections: 10 connections/user

**Headers (when implemented):**
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 87
X-RateLimit-Reset: 1728074400
```

---

## Examples

### Example 1: Complete Organization Setup Flow

```bash
# 1. Register organization
curl -X POST http://localhost:8081/api/organizations/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "UPES Dehradun",
    "code": "UPES2026",
    "adminFullName": "Dr. Smith",
    "email": "admin@upes.ac.in",
    "password": "Secure@123"
  }'

# 2. Login as admin
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@upes.ac.in",
    "password": "Secure@123"
  }'

# Save the token from response
TOKEN="eyJhbGci..."

# 3. Get statistics
curl -X GET http://localhost:8081/api/organizations/stats \
  -H "Authorization: Bearer $TOKEN"

# 4. Generate enrollment token
curl -X POST http://localhost:8081/api/organizations/devices/token \
  -H "Authorization: Bearer $TOKEN"

# 5. Upload students
curl -X POST http://localhost:8081/api/organizations/students/upload \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "csvData": "500101234,john@upes.ac.in,John Doe\n500101235,jane@upes.ac.in,Jane Smith"
  }'
```

### Example 2: Student Container Workflow

```bash
# 1. First-time login
curl -X POST http://localhost:8081/api/auth/first-login \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": "500101234",
    "email": "john@upes.ac.in",
    "password": "NewPass@123"
  }'

# Save the token
TOKEN="eyJhbGci..."

# 2. Create container
curl -X POST http://localhost:8081/api/containers \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "imageName": "ubuntu:latest",
    "cpuCores": 2,
    "ramGb": 4,
    "diskGb": 20
  }'

# 3. List containers
curl -X GET http://localhost:8081/api/containers \
  -H "Authorization: Bearer $TOKEN"

# 4. Stop container
curl -X POST http://localhost:8081/api/containers/123/stop \
  -H "Authorization: Bearer $TOKEN"

# 5. Delete container
curl -X DELETE http://localhost:8081/api/containers/123 \
  -H "Authorization: Bearer $TOKEN"
```

---

## SDKs & Client Libraries

**Official:**
- JavaScript/TypeScript: Available in `frontend/src/services/`
- Python: Planned for v2.0

**Community:**
- Coming soon

---

## Changelog

### v1.0.0 (October 2026)
- Initial release
- All core endpoints implemented
- JWT authentication
- WebSocket support
- Multi-organization architecture

---

## Support

**Documentation:**
- [Admin Guide](ADMIN_GUIDE.md)
- [Student Guide](STUDENT_GUIDE.md)
- [Installation Guide](INSTALLATION.md)

**Issues:**
- GitHub: https://github.com/rajrishu1401/CampusCompute/issues
- Email: support@campuscompute.edu

---

*Last Updated: October 3, 2026*  
*Version: 1.0*  
*For: CampusCompute v1.0*
