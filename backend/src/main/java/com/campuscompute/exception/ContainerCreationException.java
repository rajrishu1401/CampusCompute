package com.campuscompute.exception;

/**
 * Exception thrown when container creation fails
 */
public class ContainerCreationException extends RuntimeException {
    
    private String containerId;
    private String reason;
    
    public ContainerCreationException(String containerId, String reason) {
        super(String.format("Failed to create container %s: %s", containerId, reason));
        this.containerId = containerId;
        this.reason = reason;
    }
    
    public ContainerCreationException(String message) {
        super(message);
    }
    
    public String getContainerId() {
        return containerId;
    }
    
    public String getReason() {
        return reason;
    }
}
