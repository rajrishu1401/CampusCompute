package com.campuscompute.exception;

/**
 * Exception thrown when no suitable device is available for scheduling
 */
public class NoDeviceAvailableException extends RuntimeException {
    
    private String reason;
    
    public NoDeviceAvailableException(String reason) {
        super("No device available for container scheduling: " + reason);
        this.reason = reason;
    }
    
    public NoDeviceAvailableException() {
        super("No device available for container scheduling");
    }
    
    public String getReason() {
        return reason;
    }
}
