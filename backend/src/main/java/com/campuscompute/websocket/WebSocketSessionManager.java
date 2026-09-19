package com.campuscompute.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages active WebSocket sessions for agents
 * Thread-safe session tracking and retrieval
 */
@Component
@Slf4j
public class WebSocketSessionManager {
    
    // Map: deviceId → WebSocketSession
    private final Map<Long, WebSocketSession> deviceSessions = new ConcurrentHashMap<>();
    
    // Map: sessionId → deviceId (reverse lookup)
    private final Map<String, Long> sessionToDevice = new ConcurrentHashMap<>();
    
    /**
     * Register a new agent session
     */
    public void registerSession(Long deviceId, WebSocketSession session) {
        if (deviceId == null || session == null) {
            log.warn("Attempted to register null deviceId or session");
            return;
        }
        
        // Remove old session if exists
        WebSocketSession oldSession = deviceSessions.get(deviceId);
        if (oldSession != null && oldSession.isOpen()) {
            log.info("Closing old session for device {}", deviceId);
            try {
                oldSession.close();
            } catch (Exception e) {
                log.error("Error closing old session: {}", e.getMessage());
            }
        }
        
        deviceSessions.put(deviceId, session);
        sessionToDevice.put(session.getId(), deviceId);
        
        log.info("Registered WebSocket session for device {} (session: {})", deviceId, session.getId());
    }
    
    /**
     * Remove a session
     */
    public void removeSession(WebSocketSession session) {
        if (session == null) {
            return;
        }
        
        String sessionId = session.getId();
        Long deviceId = sessionToDevice.remove(sessionId);
        
        if (deviceId != null) {
            deviceSessions.remove(deviceId);
            log.info("Removed WebSocket session for device {} (session: {})", deviceId, sessionId);
        }
    }
    
    /**
     * Remove session by device ID
     */
    public void removeSessionByDeviceId(Long deviceId) {
        WebSocketSession session = deviceSessions.remove(deviceId);
        if (session != null) {
            sessionToDevice.remove(session.getId());
            log.info("Removed session for device {}", deviceId);
        }
    }
    
    /**
     * Get session for a device
     */
    public Optional<WebSocketSession> getSession(Long deviceId) {
        WebSocketSession session = deviceSessions.get(deviceId);
        
        // Validate session is still open
        if (session != null && !session.isOpen()) {
            log.warn("Session for device {} is closed, removing", deviceId);
            removeSessionByDeviceId(deviceId);
            return Optional.empty();
        }
        
        return Optional.ofNullable(session);
    }
    
    /**
     * Get device ID for a session
     */
    public Optional<Long> getDeviceId(WebSocketSession session) {
        if (session == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(sessionToDevice.get(session.getId()));
    }
    
    /**
     * Check if device is connected
     */
    public boolean isDeviceConnected(Long deviceId) {
        return getSession(deviceId).isPresent();
    }
    
    /**
     * Get count of active sessions
     */
    public int getActiveSessionCount() {
        return deviceSessions.size();
    }
    
    /**
     * Get all connected device IDs
     */
    public java.util.Set<Long> getConnectedDeviceIds() {
        return deviceSessions.keySet();
    }
    
    /**
     * Clear all sessions (for shutdown)
     */
    public void clearAllSessions() {
        log.info("Clearing all {} WebSocket sessions", deviceSessions.size());
        
        deviceSessions.values().forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.close();
                }
            } catch (Exception e) {
                log.error("Error closing session during cleanup: {}", e.getMessage());
            }
        });
        
        deviceSessions.clear();
        sessionToDevice.clear();
    }
}
