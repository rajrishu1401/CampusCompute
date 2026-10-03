# Phase 2: Agent Integration with Multi-Org Support - COMPLETE

**Date**: October 3, 2026  
**Status**: ✅ Complete  
**Build Status**: ✅ 55 source files compiled successfully  

---

## 🎯 Objective

Integrate the Python agent with the multi-organization backend by adding:
1. Enrollment token support for device registration
2. Organization context in WebSocket communications
3. Automatic device registration on first connection

---

## 📦 What Was Implemented

### 1. Agent Updates (Python)

#### config.py - Configuration Enhancement
**File**: `agent/src/config.py`

**New Properties**:
- `enrollment_token`: Optional token for first-time device registration
- `organization_id`: Organization ID for multi-tenant context

**Changes**:
```python
@property
def enrollment_token(self) -> Optional[str]:
    return self.get('broker.enrollment_token')

@property
def organization_id(self) -> Optional[int]:
    return self.get('device.organization_id')
```

#### config.yaml - Configuration File
**File**: `agent/config.yaml`

**New Fields**:
```yaml
broker:
  url: "ws://localhost:8081/ws/agent"
  enrollment_token: ""  # Get from org admin dashboard
  
device:
  id: 1
  device_id: "LAB-PC-001"
  lab_name: "Computer Science Lab"
  hostname: ""
  organization_id: null  # Set during enrollment
```

#### websocket_client.py - WebSocket Client
**File**: `agent/src/websocket_client.py`

**New Constructor Parameters**:
- `enrollment_token`: Optional enrollment token
- `organization_id`: Optional organization ID

**Changes**:
1. **Connection URL**: Now includes enrollment token and org ID in query params
   ```
   ws://broker/ws/agent?deviceId=1&enrollmentToken=xxx&organizationId=1
   ```

2. **Enrollment Tracking**: 
   ```python
   self.enrolled = False  # Track enrollment status
   ```

3. **Message Headers**: Organization ID included in all messages
   ```python
   if self.organization_id:
       message['organizationId'] = self.organization_id
   ```

4. **Auto-Enrollment**: Marks device as enrolled after successful connection

**Key Features**:
- Sends enrollment token on first connection
- Auto-marks as enrolled after successful registration
- Includes org context in all messages
- Reconnects without enrollment token after first success

#### agent.py - Main Agent Logic
**File**: `agent/src/agent.py`

**Changes**:
1. **Initialization**: Passes enrollment token and org ID to WebSocket client
   ```python
   enrollment_token = self.config.enrollment_token
   organization_id = self.config.organization_id
   
   self.ws_client = BrokerWebSocketClient(
       broker_url=broker_url,
       device_id=device_id,
       message_handler=self._handle_broker_message,
       enrollment_token=enrollment_token,
       organization_id=organization_id
   )
   ```

2. **Logging**: Shows enrollment and org status on startup
   ```
   🔑 Enrollment token found - Will register device on first connection
   🏢 Organization ID: 1
   ```

3. **Heartbeat**: Includes organization ID in heartbeat payload
   ```python
   if self.config.organization_id:
       payload['organization_id'] = self.config.organization_id
   ```

---

### 2. Backend Updates (Spring Boot)

#### AgentWebSocketHandler - WebSocket Handler
**File**: `backend/src/main/java/com/campuscompute/websocket/AgentWebSocketHandler.java`

**New Methods**:

1. **extractEnrollmentToken(String query)**
   - Extracts enrollment token from WebSocket query string
   - Returns null if not present

2. **extractOrganizationId(String query)**
   - Extracts organization ID from WebSocket query string
   - Returns null if not present

**Updated Method**: `afterConnectionEstablished()`

**Flow**:
```
1. Extract deviceId, enrollmentToken, organizationId from query string
2. If enrollmentToken present:
   a. Call deviceService.registerDeviceWithToken()
   b. Device registered and assigned ID
   c. Log success with org info
3. Verify device exists in database
4. Verify organization matches (if provided)
5. Register WebSocket session
6. Update device status to ONLINE
7. Send ACK to agent
```

**Security Checks**:
- ✅ Validates enrollment token
- ✅ Verifies organization matches device's org
- ✅ Closes connection if validation fails
- ✅ Logs all enrollment attempts

**Error Handling**:
- Invalid token → Close with "Invalid enrollment token"
- Device not found → Close with "Device not found"
- Org mismatch → Close with "Organization mismatch"

#### DeviceService - Device Management
**File**: `backend/src/main/java/com/campuscompute/service/DeviceService.java`

**New Dependencies**:
```java
private final OrganizationService organizationService;
private final OrganizationRepository organizationRepository;
```

**New Method**: `registerDeviceWithToken(String enrollmentToken, String queryString)`

**Functionality**:
1. Validates enrollment token (basic validation for Phase 2)
2. Extracts device info from query string
3. Checks if device already exists
4. If exists: Updates status to ONLINE
5. If new: Creates device with default specs
6. Returns registered device

**Current Limitations** (to be addressed in Phase 3):
- ⚠️ Token validation is basic (not using Redis)
- ⚠️ Organization assignment not automated yet
- ⚠️ Manual org assignment required in database

**Helper Methods**:
- `extractParamFromQuery()`: Extract params from query string

---

## 🔄 Enrollment Workflow

### New Device Registration

```
┌─────────┐                  ┌─────────┐                  ┌──────────┐
│  Agent  │                  │ Backend │                  │ Database │
└────┬────┘                  └────┬────┘                  └────┬─────┘
     │                            │                            │
     │ 1. Connect with token      │                            │
     │ ────────────────────────> │                            │
     │                            │                            │
     │                            │ 2. Validate token          │
     │                            │ ─────────────────────────> │
     │                            │                            │
     │                            │ 3. Create device           │
     │                            │ ─────────────────────────> │
     │                            │                            │
     │ 4. ACK + Device ID         │ 4. Device created          │
     │ <────────────────────────  │ <───────────────────────── │
     │                            │                            │
     │ 5. Store device ID         │                            │
     │ (mark enrolled)            │                            │
     │                            │                            │
     │ 6. Send heartbeat          │                            │
     │ ────────────────────────> │                            │
     │                            │ 7. Update metrics          │
     │                            │ ─────────────────────────> │
     │                            │                            │
```

### Existing Device Reconnection

```
┌─────────┐                  ┌─────────┐                  ┌──────────┐
│  Agent  │                  │ Backend │                  │ Database │
└────┬────┘                  └────┬────┘                  └────┬─────┘
     │                            │                            │
     │ 1. Connect with deviceId   │                            │
     │    (no token, enrolled)    │                            │
     │ ────────────────────────> │                            │
     │                            │                            │
     │                            │ 2. Verify device           │
     │                            │ ─────────────────────────> │
     │                            │                            │
     │ 3. ACK                     │ 3. Device found            │
     │ <────────────────────────  │ <───────────────────────── │
     │                            │                            │
     │ 4. Normal operation        │                            │
     │ ────────────────────────> │                            │
```

---

## 🧪 Testing

### Test Scenario 1: New Device Enrollment

**Setup**:
1. Generate enrollment token from backend:
   ```bash
   curl -X POST http://localhost:8081/api/organizations/devices/token \
     -H "Authorization: Bearer <org_admin_token>"
   ```

2. Update agent config.yaml:
   ```yaml
   broker:
     enrollment_token: "<token-from-step-1>"
   
   device:
     device_id: "NEW-LAB-PC-001"
     lab_name: "New Computer Lab"
   ```

3. Start agent:
   ```bash
   python agent/src/main.py
   ```

**Expected Output**:
```
Agent: 🔑 Enrollment token found - Will register device on first connection
Agent: Connecting to broker: ws://localhost:8081/ws/agent
Backend: Enrollment token provided, attempting device registration
Backend: ✅ Device registered successfully: NEW-LAB-PC-001 (ID: 5, Org: 1)
Agent: ✅ Connected to broker successfully
Agent: ✅ Device enrolled successfully
```

### Test Scenario 2: Existing Device Reconnection

**Setup**:
1. Remove enrollment_token from config.yaml
2. Ensure device_id matches database
3. Start agent

**Expected Output**:
```
Agent: Connecting to broker: ws://localhost:8081/ws/agent (Device ID: 1)
Backend: Device 1 verified
Agent: ✅ Connected to broker successfully
Agent: Heartbeat sent
```

### Test Scenario 3: Organization Context

**Setup**:
1. Set organization_id in config.yaml:
   ```yaml
   device:
     organization_id: 1
   ```

2. Start agent
3. Check logs for org context

**Expected Output**:
```
Agent: 🏢 Organization ID: 1
Backend: ✅ Agent connected for device 1: LAB-PC-001 (Org: UPES)
```

---

## 📊 Message Format Updates

### Agent → Backend Messages

**Heartbeat** (updated):
```json
{
  "type": "HEARTBEAT",
  "deviceId": 1,
  "organizationId": 1,
  "timestamp": "2026-10-03T12:00:00Z",
  "payload": {
    "hostname": "LAB-PC-001",
    "lab_name": "CS Lab",
    "status": "ONLINE",
    "cpu_percent": 25.5,
    "ram_used_bytes": 4294967296,
    "organization_id": 1
  }
}
```

**Container Created** (updated):
```json
{
  "type": "CONTAINER_CREATED",
  "deviceId": 1,
  "organizationId": 1,
  "requestId": "123",
  "timestamp": "2026-10-03T12:00:00Z",
  "payload": {
    "container_id": "abc123...",
    "status": "running"
  }
}
```

---

## 🔒 Security Considerations

### Phase 2 (Current)
- ✅ Enrollment token passed via WebSocket query params
- ✅ Organization context tracked
- ⚠️ Token validation is basic
- ⚠️ No token expiration checking
- ⚠️ No Redis storage

### Phase 3 (Future Enhancements)
- [ ] Store tokens in Redis with TTL
- [ ] Validate token expiration
- [ ] Link token to organization
- [ ] Revoke used tokens
- [ ] Add mTLS authentication
- [ ] Encrypt WebSocket connection (WSS)

---

## 📝 Configuration Guide

### For Organization Admins

**Step 1**: Get Enrollment Token
```bash
curl -X POST http://localhost:8081/api/organizations/devices/token \
  -H "Authorization: Bearer <your_admin_token>" \
  -H "Content-Type: application/json"
```

**Response**:
```json
{
  "success": true,
  "data": {
    "token": "a1b2c3d4-e5f6-7g8h-9i0j-k1l2m3n4o5p6",
    "organizationId": 1,
    "organizationCode": "UPES",
    "expiresAt": "2026-10-04T12:00:00Z",
    "installScript": "#!/bin/bash\n...",
    "backendUrl": "http://localhost:8081"
  }
}
```

**Step 2**: Configure Agent

Create/update `config.yaml`:
```yaml
broker:
  url: "ws://localhost:8081/ws/agent"
  enrollment_token: "a1b2c3d4-e5f6-7g8h-9i0j-k1l2m3n4o5p6"

device:
  device_id: "LAB-PC-001"  # Unique per machine
  lab_name: "Computer Science Lab"
  hostname: ""  # Auto-detected
  organization_id: 1
```

**Step 3**: Install and Start Agent
```bash
# Install dependencies
pip install -r agent/requirements.txt

# Start agent
python agent/src/main.py
```

**Step 4**: Verify in Dashboard
- Check backend logs for "Device registered successfully"
- Verify device appears in organization's device list
- Check device status is ONLINE

---

## 🐛 Troubleshooting

### Issue: "Invalid enrollment token"

**Cause**: Token expired or doesn't exist

**Solution**:
1. Generate new token from admin dashboard
2. Update agent config.yaml
3. Restart agent

### Issue: "Organization mismatch"

**Cause**: Agent's organizationId doesn't match device's org

**Solution**:
1. Check device's organization in database
2. Update agent config.yaml with correct org ID
3. Restart agent

### Issue: "Device not found"

**Cause**: Device ID doesn't exist in database

**Solution**:
1. Use enrollment token for first-time registration
2. OR manually register device in database first

### Issue: Agent shows enrolled but backend rejects

**Cause**: Agent config has device ID but device was deleted

**Solution**:
1. Remove device_id from config (will use enrollment token)
2. Add enrollment_token to config
3. Restart agent

---

## 📈 Metrics

### Implementation Stats
- **Files Modified**: 7 (4 agent + 3 backend)
- **Lines Added**: ~250 lines
- **New Methods**: 5
- **Compile Status**: ✅ Success (55 files)
- **Backward Compatible**: ✅ Yes

### Features Added
- ✅ Enrollment token support
- ✅ Organization context in messages
- ✅ Auto-device registration
- ✅ Query param extraction
- ✅ Enhanced logging

---

## 🚀 Next Steps (Phase 3)

### Token Management
- [ ] Store tokens in Redis
- [ ] Implement token expiration
- [ ] Add token revocation
- [ ] Link tokens to organizations

### Organization Statistics
- [ ] Device count per organization
- [ ] Resource usage per organization
- [ ] Container count per organization
- [ ] Student activity tracking

### Admin Dashboard Endpoints
- [ ] GET /api/organizations/stats - Org statistics
- [ ] GET /api/organizations/devices - List devices
- [ ] GET /api/organizations/students - List students
- [ ] GET /api/organizations/containers - List containers

---

## ✅ Phase 2 Complete Checklist

- [x] Agent config updated with enrollment_token field
- [x] Agent config updated with organization_id field
- [x] WebSocket client sends enrollment token
- [x] WebSocket client includes org ID in messages
- [x] Backend extracts enrollment token from query
- [x] Backend registers device with token
- [x] Backend validates organization context
- [x] DeviceService has registerDeviceWithToken()
- [x] All code compiles successfully
- [x] Documentation complete
- [x] Testing scenarios documented

---

**Status**: ✅ Phase 2 Complete  
**Next Phase**: Phase 3 - Statistics & Admin Analytics  
**Estimated Time**: 3-5 days  

**Date**: October 3, 2026  
**Author**: Rishabh Raj  
**Institution**: UPES Dehradun
