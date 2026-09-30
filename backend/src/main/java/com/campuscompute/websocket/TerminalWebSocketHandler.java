package com.campuscompute.websocket;

import com.campuscompute.entity.Container;
import com.campuscompute.service.ContainerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles WebSocket connections for container terminal access
 * Provides interactive shell access to running containers
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TerminalWebSocketHandler extends TextWebSocketHandler {
    
    private final ContainerService containerService;
    private final WebSocketSessionManager sessionManager;
    private final AgentWebSocketHandler agentWebSocketHandler;
    private final ObjectMapper objectMapper;
    
    // Map: terminal session ID -> WebSocketSession
    private final Map<String, WebSocketSession> terminalSessions = new ConcurrentHashMap<>();
    
    // Map: terminal session ID -> container ID
    private final Map<String, Long> terminalToContainerMap = new ConcurrentHashMap<>();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("New terminal WebSocket connection: {}", session.getId());
        
        // Extract container ID from path: /ws/terminal/{containerId}
        String path = session.getUri().getPath();
        String[] parts = path.split("/");
        
        if (parts.length < 4) {
            log.error("Invalid path format: {}", path);
            session.close(CloseStatus.BAD_DATA.withReason("Invalid path"));
            return;
        }
        
        Long containerId;
        try {
            containerId = Long.parseLong(parts[3]);
        } catch (NumberFormatException e) {
            log.error("Invalid container ID: {}", parts[3]);
            session.close(CloseStatus.BAD_DATA.withReason("Invalid container ID"));
            return;
        }
        
        // Verify container exists and is running
        Container container = containerService.getContainerById(containerId)
            .orElse(null);
        
        if (container == null) {
            log.error("Container not found: {}", containerId);
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Container not found"));
            return;
        }
        
        if (container.getStatus() != Container.ContainerStatus.RUNNING) {
            log.error("Container {} is not running: {}", containerId, container.getStatus());
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Container not running"));
            return;
        }
        
        // TODO: Verify user owns container (get userId from JWT in query params or headers)
        
        // Store mapping
        terminalToContainerMap.put(session.getId(), containerId);
        
        // Send TERMINAL_ATTACH message to agent
        Long deviceId = container.getDevice().getId();
        
        Map<String, Object> payload = Map.of(
            "container_id", container.getContainerId(),
            "terminal_session_id", session.getId(),
            "cols", 80,  // Default terminal size
            "rows", 24
        );
        
        com.campuscompute.dto.AgentMessage message = new com.campuscompute.dto.AgentMessage(
            com.campuscompute.dto.AgentMessage.MessageType.TERMINAL_ATTACH,
            containerId.toString(),
            deviceId,
            java.time.LocalDateTime.now(),
            payload,
            null
        );
        
        // Get agent session and send attach message
        sessionManager.getSession(deviceId).ifPresentOrElse(
            agentSession -> {
                try {
                    agentWebSocketHandler.sendMessage(agentSession, message);
                    terminalSessions.put(session.getId(), session);
                    log.info("Terminal session {} attached to container {} on device {}", 
                        session.getId(), containerId, deviceId);
                } catch (Exception e) {
                    log.error("Failed to attach terminal: {}", e.getMessage());
                    try {
                        session.close(CloseStatus.SERVER_ERROR.withReason("Failed to attach"));
                    } catch (IOException ex) {
                        log.error("Error closing session: {}", ex.getMessage());
                    }
                }
            },
            () -> {
                log.error("Agent not connected for device {}", deviceId);
                try {
                    session.close(CloseStatus.SERVICE_RESTARTED.withReason("Agent offline"));
                } catch (IOException e) {
                    log.error("Error closing session: {}", e.getMessage());
                }
            }
        );
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String terminalSessionId = session.getId();
        Long containerId = terminalToContainerMap.get(terminalSessionId);
        
        if (containerId == null) {
            log.error("No container mapping for terminal session {}", terminalSessionId);
            return;
        }
        
        // Get container to find device
        Container container = containerService.getContainerById(containerId).orElse(null);
        if (container == null || container.getDevice() == null) {
            log.error("Container or device not found for terminal session {}", terminalSessionId);
            return;
        }
        
        // Forward input to agent
        Long deviceId = container.getDevice().getId();
        
        Map<String, Object> payload = Map.of(
            "container_id", container.getContainerId(),
            "terminal_session_id", terminalSessionId,
            "input", message.getPayload()
        );
        
        com.campuscompute.dto.AgentMessage agentMessage = new com.campuscompute.dto.AgentMessage(
            com.campuscompute.dto.AgentMessage.MessageType.TERMINAL_INPUT,
            containerId.toString(),
            deviceId,
            java.time.LocalDateTime.now(),
            payload,
            null
        );
        
        sessionManager.getSession(deviceId).ifPresent(agentSession -> {
            try {
                agentWebSocketHandler.sendMessage(agentSession, agentMessage);
            } catch (Exception e) {
                log.error("Failed to send terminal input: {}", e.getMessage());
            }
        });
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String terminalSessionId = session.getId();
        Long containerId = terminalToContainerMap.remove(terminalSessionId);
        terminalSessions.remove(terminalSessionId);
        
        log.info("Terminal session {} closed: {}", terminalSessionId, status);
        
        if (containerId != null) {
            // Send TERMINAL_DETACH to agent
            Container container = containerService.getContainerById(containerId).orElse(null);
            if (container != null && container.getDevice() != null) {
                Long deviceId = container.getDevice().getId();
                
                Map<String, Object> payload = Map.of(
                    "container_id", container.getContainerId(),
                    "terminal_session_id", terminalSessionId
                );
                
                com.campuscompute.dto.AgentMessage message = new com.campuscompute.dto.AgentMessage(
                    com.campuscompute.dto.AgentMessage.MessageType.TERMINAL_DETACH,
                    containerId.toString(),
                    deviceId,
                    java.time.LocalDateTime.now(),
                    payload,
                    null
                );
                
                sessionManager.getSession(deviceId).ifPresent(agentSession -> {
                    try {
                        agentWebSocketHandler.sendMessage(agentSession, message);
                    } catch (Exception e) {
                        log.error("Failed to send terminal detach: {}", e.getMessage());
                    }
                });
            }
        }
    }
    
    /**
     * Send terminal output to user
     * Called by AgentWebSocketHandler when receiving TERMINAL_OUTPUT
     */
    public void sendTerminalOutput(String terminalSessionId, String output) {
        WebSocketSession session = terminalSessions.get(terminalSessionId);
        
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(output));
                log.trace("Sent {} bytes to terminal session {}", output.length(), terminalSessionId);
            } catch (IOException e) {
                log.error("Failed to send terminal output to session {}: {}", terminalSessionId, e.getMessage());
                // Remove dead session
                terminalSessions.remove(terminalSessionId);
                terminalToContainerMap.remove(terminalSessionId);
            }
        } else {
            log.debug("Terminal session {} not found or closed", terminalSessionId);
        }
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("Terminal WebSocket error for session {}: {}", session.getId(), exception.getMessage());
        session.close(CloseStatus.SERVER_ERROR);
    }
}
