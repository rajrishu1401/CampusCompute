package com.campuscompute.config;

import com.campuscompute.websocket.AgentWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket configuration for agent communication
 * Endpoints:
 * - /ws/agent - For agent connections (heartbeat, container events)
 * - /ws/terminal/{containerId} - For terminal sessions (future)
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    
    private final AgentWebSocketHandler agentWebSocketHandler;
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Agent WebSocket endpoint
        registry.addHandler(agentWebSocketHandler, "/ws/agent")
                .setAllowedOrigins("*")  // In production, specify allowed origins
                .withSockJS();  // Enable SockJS fallback for browsers that don't support WebSocket
        
        // TODO: Add terminal WebSocket handler
        // registry.addHandler(terminalWebSocketHandler, "/ws/terminal/{containerId}")
        //         .setAllowedOrigins("*");
    }
}
