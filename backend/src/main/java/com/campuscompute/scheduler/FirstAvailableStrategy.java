package com.campuscompute.scheduler;

import com.campuscompute.dto.ContainerRequest;
import com.campuscompute.entity.Device;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Simple scheduling strategy: pick the first available device
 * No scoring, just returns first device that has enough resources
 * Useful for testing and as a fallback strategy
 */
@Component
@Slf4j
public class FirstAvailableStrategy implements SchedulingStrategy {
    
    @Override
    public Device selectDevice(List<Device> availableDevices, ContainerRequest request) {
        if (availableDevices == null || availableDevices.isEmpty()) {
            log.warn("No available devices to schedule");
            return null;
        }
        
        // Find first device with sufficient resources
        for (Device device : availableDevices) {
            if (hasEnoughResources(device, request)) {
                log.info("Selected device {} (first available)", device.getId());
                return device;
            }
        }
        
        log.warn("No device found with sufficient resources");
        return null;
    }
    
    @Override
    public double calculateDeviceScore(Device device, ContainerRequest request) {
        // Binary score: 1.0 if has resources, 0.0 otherwise
        return hasEnoughResources(device, request) ? 1.0 : 0.0;
    }
    
    @Override
    public String getStrategyName() {
        return "FirstAvailable";
    }
    
    /**
     * Check if device has enough resources
     */
    private boolean hasEnoughResources(Device device, ContainerRequest request) {
        long availableCpu = device.getTotalCpuCores() - device.getUsedCpuCores();
        long availableRam = device.getTotalRamBytes() - device.getUsedRamBytes();
        long availableDisk = device.getTotalDiskBytes() - device.getUsedDiskBytes();
        
        return availableCpu >= request.getCpuCores() &&
               availableRam >= request.getRamBytes() &&
               availableDisk >= request.getDiskBytes();
    }
}
