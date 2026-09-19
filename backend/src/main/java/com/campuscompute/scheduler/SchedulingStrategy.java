package com.campuscompute.scheduler;

import com.campuscompute.dto.ContainerRequest;
import com.campuscompute.entity.Device;

import java.util.List;

/**
 * Interface for device scheduling strategies
 * Different implementations can provide different scheduling algorithms
 */
public interface SchedulingStrategy {
    
    /**
     * Select the best device for a container request
     * 
     * @param availableDevices List of online devices with sufficient resources
     * @param request Container creation request
     * @return Selected device, or null if no suitable device found
     */
    Device selectDevice(List<Device> availableDevices, ContainerRequest request);
    
    /**
     * Calculate score for a device
     * Higher score = better fit
     * 
     * @param device Device to score
     * @param request Container request
     * @return Score (0.0 to 1.0)
     */
    double calculateDeviceScore(Device device, ContainerRequest request);
    
    /**
     * Get strategy name
     */
    String getStrategyName();
}
