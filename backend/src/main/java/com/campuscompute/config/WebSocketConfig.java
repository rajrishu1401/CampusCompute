package com.campuscompute.config;

import com.campuscompute.websocket.AgentWebSocketHandler;
import com.campuscompute.websocket.TerminalWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket configuration for agent communication and terminal access
 * Endpoints:
 * - /ws/agent - For agent connections (heartbeat, container events)
 * - /ws/terminal/{containerId} - For terminal sessions
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    
    private final AgentWebSocketHandler agentWebSocketHandler;
    private final TerminalWebSocketHandler terminalWebSocketHandler;
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Agent WebSocket endpoint (plain WebSocket for Python agents)
        registry.addHandler(agentWebSocketHandler, "/ws/agent")
                .setAllowedOrigins("*");  // In production, specify allowed origins
        
        // Terminal WebSocket endpoint (for user terminal access)
        registry.addHandler(terminalWebSocketHandler, "/ws/terminal/**")
                .setAllowedOrigins("*");  // In production, specify allowed origins
    }
}
