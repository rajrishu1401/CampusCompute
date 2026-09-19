package com.campuscompute.websocket;

import com.campuscompute.dto.AgentMessage;
import com.campuscompute.entity.Device;
import com.campuscompute.service.DeviceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Handles WebSocket connections from agents
 * Processes heartbeats, container lifecycle events, and metrics
 */
@Component
@Slf4j
public class AgentWebSocketHandler extends TextWebSocketHandler {
    
    private final WebSocketSessionManager sessionManager;
    private final DeviceService deviceService;
    private final ObjectMapper objectMapper;
    
    // Lazy injection to avoid circular dependency
    @Autowired
    @Lazy
    private com.campuscompute.service.ContainerService containerService;
    
    public AgentWebSocketHandler(WebSocketSessionManager sessionManager,
                                 DeviceService deviceService,
                                 ObjectMapper objectMapper) {
        this.sessionManager = sessionManager;
        this.deviceService = deviceService;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("New WebSocket connection established: {}", session.getId());
        
        // Extract device ID from query params or headers
        // Format: ws://broker:8081/ws/agent?deviceId=123
        String query = session.getUri().getQuery();
        Long deviceId = extractDeviceId(query);
        
        if (deviceId == null) {
            log.error("No deviceId provided in connection, closing");
            session.close(CloseStatus.BAD_DATA.withReason("Missing deviceId"));
            return;
        }
        
        // Verify device exists
        Device device = deviceService.getDeviceById(deviceId).orElse(null);
        if (device == null) {
            log.error("Device {} not found, closing connection", deviceId);
            session.close(CloseStatus.BAD_DATA.withReason("Device not found"));
            return;
        }
        
        // Register session
        sessionManager.registerSession(deviceId, session);
        
        // Update device status to ONLINE
        deviceService.updateDeviceStatus(deviceId, Device.DeviceStatus.ONLINE);
        deviceService.updateLastHeartbeat(deviceId);
        
        log.info("Agent connected for device {}: {}", deviceId, device.getHostname());
        
        // Send ACK
        AgentMessage ack = AgentMessage.ack(null);
        sendMessage(session, ack);
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("Received message: {}", payload);
        
        try {
            AgentMessage agentMessage = objectMapper.readValue(payload, AgentMessage.class);
            handleAgentMessage(session, agentMessage);
        } catch (Exception e) {
            log.error("Error parsing message: {}", e.getMessage(), e);
            AgentMessage error = AgentMessage.error(null, "Invalid message format: " + e.getMessage());
            sendMessage(session, error);
        }
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket connection closed: {} ({})", session.getId(), status);
        
        Long deviceId = sessionManager.getDeviceId(session).orElse(null);
        
        if (deviceId != null) {
            // Update device status to OFFLINE
            deviceService.updateDeviceStatus(deviceId, Device.DeviceStatus.OFFLINE);
            log.info("Device {} marked as OFFLINE", deviceId);
        }
        
        sessionManager.removeSession(session);
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error: {}", exception.getMessage(), exception);
        
        Long deviceId = sessionManager.getDeviceId(session).orElse(null);
        if (deviceId != null) {
            deviceService.updateDeviceStatus(deviceId, Device.DeviceStatus.OFFLINE);
        }
        
        sessionManager.removeSession(session);
    }
    
    /**
     * Handle incoming message from agent
     */
    private void handleAgentMessage(WebSocketSession session, AgentMessage message) {
        Long deviceId = message.getDeviceId();
        
        log.debug("Processing {} message from device {}", message.getType(), deviceId);
        
        switch (message.getType()) {
            case HEARTBEAT -> handleHeartbeat(deviceId, message.getPayload());
            case CONTAINER_CREATED -> handleContainerCreated(message);
            case CONTAINER_FAILED -> handleContainerFailed(message);
            case CONTAINER_STOPPED -> handleContainerStopped(message);
            case CONTAINER_DELETED -> handleContainerDeleted(message);
            case METRICS_UPDATE -> handleMetricsUpdate(deviceId, message.getPayload());
            case PONG -> log.debug("Received PONG from device {}", deviceId);
            case ERROR -> log.error("Agent error: {}", message.getError());
            default -> log.warn("Unknown message type: {}", message.getType());
        }
    }
    
    /**
     * Handle heartbeat from agent
     */
    private void handleHeartbeat(Long deviceId, Map<String, Object> metrics) {
        log.debug("Heartbeat from device {}", deviceId);
        
        // Update last heartbeat timestamp
        deviceService.updateLastHeartbeat(deviceId);
        
        // Update device metrics if provided
        if (metrics != null && !metrics.isEmpty()) {
            updateDeviceMetrics(deviceId, metrics);
        }
    }
    
    /**
     * Handle container created notification
     */
    private void handleContainerCreated(AgentMessage message) {
        String requestId = message.getRequestId();
        Map<String, Object> payload = message.getPayload();
        
        log.info("Container created - requestId: {}, payload: {}", requestId, payload);
        
        try {
            // Extract container info
            Long containerId = Long.parseLong(requestId);
            String dockerContainerId = (String) payload.get("container_id");
            
            // Update container status in database
            if (containerService != null) {
                containerService.markContainerRunning(containerId, dockerContainerId);
                log.info("Container {} marked as RUNNING in database", containerId);
            } else {
                log.warn("ContainerService not available, cannot update container status");
            }
        } catch (Exception e) {
            log.error("Error handling container created: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Handle container creation failure
     */
    private void handleContainerFailed(AgentMessage message) {
        String requestId = message.getRequestId();
        String error = message.getError();
        
        log.error("Container creation failed - requestId: {}, error: {}", requestId, error);
        
        try {
            // Update container status to FAILED
            Long containerId = Long.parseLong(requestId);
            
            if (containerService != null) {
                containerService.markContainerFailed(containerId);
                log.info("Container {} marked as FAILED in database", containerId);
            } else {
                log.warn("ContainerService not available, cannot update container status");
            }
        } catch (Exception e) {
            log.error("Error handling container failed: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Handle container stopped notification
     */
    private void handleContainerStopped(AgentMessage message) {
        String requestId = message.getRequestId();
        log.info("Container stopped - requestId: {}", requestId);
        
        try {
            Long containerId = Long.parseLong(requestId);
            
            if (containerService != null) {
                containerService.stopContainer(containerId);
                log.info("Container {} marked as STOPPED in database", containerId);
            }
        } catch (Exception e) {
            log.error("Error handling container stopped: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Handle container deleted notification
     */
    private void handleContainerDeleted(AgentMessage message) {
        String requestId = message.getRequestId();
        log.info("Container deleted - requestId: {}", requestId);
        
        try {
            Long containerId = Long.parseLong(requestId);
            
            if (containerService != null) {
                containerService.deleteContainer(containerId);
                log.info("Container {} marked as DELETED in database", containerId);
            }
        } catch (Exception e) {
            log.error("Error handling container deleted: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Handle metrics update
     */
    private void handleMetricsUpdate(Long deviceId, Map<String, Object> metrics) {
        log.debug("Metrics update from device {}: {}", deviceId, metrics);
        updateDeviceMetrics(deviceId, metrics);
    }
    
    /**
     * Update device metrics
     */
    private void updateDeviceMetrics(Long deviceId, Map<String, Object> metrics) {
        try {
            // Extract metrics
            Double cpuPercent = getDoubleValue(metrics, "cpu_percent");
            Long ramUsedBytes = getLongValue(metrics, "ram_used_bytes");
            Long diskUsedBytes = getLongValue(metrics, "disk_used_bytes");
            Integer containerCount = getIntegerValue(metrics, "container_count");
            
            // Update device
            if (cpuPercent != null || ramUsedBytes != null || diskUsedBytes != null || containerCount != null) {
                // Update used resources
                Device device = deviceService.getDeviceById(deviceId).orElse(null);
                if (device != null) {
                    // Note: In production, you'd want to track these in a separate metrics table
                    log.debug("Updated metrics for device {}: CPU={}%, RAM={} bytes, Disk={} bytes, Containers={}", 
                        deviceId, cpuPercent, ramUsedBytes, diskUsedBytes, containerCount);
                }
            }
        } catch (Exception e) {
            log.error("Error updating device metrics: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Send message to agent
     */
    public void sendMessage(WebSocketSession session, AgentMessage message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
            log.debug("Sent message: {}", message.getType());
        } catch (Exception e) {
            log.error("Error sending message: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Extract device ID from query string
     */
    private Long extractDeviceId(String query) {
        if (query == null || query.isEmpty()) {
            return null;
        }
        
        // Parse deviceId=123
        String[] params = query.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2 && "deviceId".equals(keyValue[0])) {
                try {
                    return Long.parseLong(keyValue[1]);
                } catch (NumberFormatException e) {
                    log.error("Invalid deviceId format: {}", keyValue[1]);
                }
            }
        }
        
        return null;
    }
    
    // Helper methods to extract values from metrics map
    
    private Double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return null;
    }
    
    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }
    
    private Integer getIntegerValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }
}
