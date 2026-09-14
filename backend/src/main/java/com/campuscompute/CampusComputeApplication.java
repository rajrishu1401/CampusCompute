package com.campuscompute;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * CampusCompute Backend Application
 * 
 * Campus-Aware Cloud Resource Pooling System
 * Central broker service for managing lab computers and containers
 * 
 * @author CampusCompute Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableScheduling
public class CampusComputeApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusComputeApplication.class, args);
        System.out.println("""
            
            ╔═══════════════════════════════════════════════════════╗
            ║                                                       ║
            ║          CampusCompute Backend Started               ║
            ║     Campus-Aware Cloud Resource Pooling System       ║
            ║                                                       ║
            ║     API: http://localhost:8080/api                   ║
            ║     WebSocket: ws://localhost:8080/ws                ║
            ║                                                       ║
            ╚═══════════════════════════════════════════════════════╝
            """);
    }
}
