package com.campuscompute.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Message format for WebSocket communication between broker and agents
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentMessage {
    
    private MessageType type;
    private String requestId;  // Unique ID to track request-response pairs
    private Long deviceId;     // Device that sent/receives the message
    private LocalDateTime timestamp;
    private Map<String, Object> payload;
    private String error;      // Error message if any
    
    public enum MessageType {
        // Agent → Broker
        HEARTBEAT,              // Periodic health check with metrics
        DEVICE_REGISTERED,      // Device successfully enrolled
        CONTAINER_CREATED,      // Container successfully created
        CONTAINER_FAILED,       // Container creation failed
        CONTAINER_STOPPED,      // Container stopped
        CONTAINER_DELETED,      // Container deleted
        CONTAINER_RESTARTED,    // Container successfully restarted
        METRICS_UPDATE,         // Resource usage metrics
        TERMINAL_OUTPUT,        // Terminal output from container
        
        // Broker → Agent
        CREATE_CONTAINER,       // Request to create container
        STOP_CONTAINER,         // Request to stop container
        DELETE_CONTAINER,       // Request to delete container
        RESTART_CONTAINER,      // Request to restart container
        GET_CONTAINER_STATS,    // Request container statistics
        GET_CONTAINER_LOGS,     // Request container logs
        TERMINAL_ATTACH,        // Attach to container terminal
        TERMINAL_DETACH,        // Detach from container terminal
        TERMINAL_INPUT,         // Input to container terminal
        TERMINAL_RESIZE,        // Resize terminal dimensions
        
        // Bidirectional
        PING,                   // Keepalive ping
        PONG,                   // Keepalive pong response
        ERROR,                  // Error occurred
        ACK                     // Acknowledgment
    }
    
    /**
     * Create heartbeat message
     */
    public static AgentMessage heartbeat(Long deviceId, Map<String, Object> metrics) {
        return new AgentMessage(
            MessageType.HEARTBEAT,
            null,
            deviceId,
            LocalDateTime.now(),
            metrics,
            null
        );
    }
    
    /**
     * Create container creation request
     */
    public static AgentMessage createContainer(Long deviceId, String requestId, Map<String, Object> containerSpec) {
        return new AgentMessage(
            MessageType.CREATE_CONTAINER,
            requestId,
            deviceId,
            LocalDateTime.now(),
            containerSpec,
            null
        );
    }
    
    /**
     * Create container stop request
     */
    public static AgentMessage stopContainer(Long deviceId, String requestId, String containerId) {
        return new AgentMessage(
            MessageType.STOP_CONTAINER,
            requestId,
            deviceId,
            LocalDateTime.now(),
            Map.of("container_id", containerId),  // Use snake_case for Python agent
            null
        );
    }
    
    /**
     * Create container delete request
     */
    public static AgentMessage deleteContainer(Long deviceId, String requestId, String containerId) {
        return new AgentMessage(
            MessageType.DELETE_CONTAINER,
            requestId,
            deviceId,
            LocalDateTime.now(),
            Map.of("container_id", containerId),  // Use snake_case for Python agent
            null
        );
    }

    /**
     * Create container restart request
     */
    public static AgentMessage restartContainer(Long deviceId, String requestId, String containerId) {
        return new AgentMessage(
            MessageType.RESTART_CONTAINER,
            requestId,
            deviceId,
            LocalDateTime.now(),
            Map.of("container_id", containerId),
            null
        );
    }
    
    /**
     * Create error message
     */
    public static AgentMessage error(String requestId, String errorMessage) {
        return new AgentMessage(
            MessageType.ERROR,
            requestId,
            null,
            LocalDateTime.now(),
            null,
            errorMessage
        );
    }
    
    /**
     * Create ACK message
     */
    public static AgentMessage ack(String requestId) {
        return new AgentMessage(
            MessageType.ACK,
            requestId,
            null,
            LocalDateTime.now(),
            null,
            null
        );
    }
}
