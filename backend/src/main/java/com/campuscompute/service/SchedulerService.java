package com.campuscompute.service;

import com.campuscompute.dto.ContainerRequest;
import com.campuscompute.entity.Device;
import com.campuscompute.exception.NoDeviceAvailableException;
import com.campuscompute.repository.DeviceRepository;
import com.campuscompute.scheduler.AdaptiveSchedulerStrategy;
import com.campuscompute.scheduler.FirstAvailableStrategy;
import com.campuscompute.scheduler.SchedulingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Scheduler Service - Core component for device selection
 * 
 * Selects the best device to run a container based on:
 * - Available resources
 * - Current load
 * - Upcoming lab reservations (novel contribution)
 * - Device reliability
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {
    
    private final DeviceRepository deviceRepository;
    private final AdaptiveSchedulerStrategy adaptiveStrategy;
    private final FirstAvailableStrategy firstAvailableStrategy;
    
    @Value("${scheduler.strategy:adaptive}")
    private String strategyName;
    
    /**
     * Select the best device for a container request
     * 
     * @param request Container creation request
     * @return Selected device
     * @throws NoDeviceAvailableException if no suitable device found
     */
    public Device selectBestDevice(ContainerRequest request) {
        log.info("Selecting device for container request: {} cores, {} MB RAM", 
            request.getCpuCores(), request.getRamBytes() / (1024 * 1024));
        
        // Get all online devices
        List<Device> onlineDevices = deviceRepository.findByStatus(Device.DeviceStatus.ONLINE);
        
        if (onlineDevices.isEmpty()) {
            log.error("No online devices available");
            throw new NoDeviceAvailableException("No online devices available for scheduling");
        }
        
        log.info("Found {} online devices", onlineDevices.size());
        
        // Select strategy
        SchedulingStrategy strategy = getStrategy();
        log.info("Using scheduling strategy: {}", strategy.getStrategyName());
        
        // Select device
        Device selectedDevice = strategy.selectDevice(onlineDevices, request);
        
        if (selectedDevice == null) {
            log.error("No suitable device found for request");
            throw new NoDeviceAvailableException(
                String.format("No device available with %d cores, %d MB RAM, %d MB disk",
                    request.getCpuCores(),
                    request.getRamBytes() / (1024 * 1024),
                    request.getDiskBytes() / (1024 * 1024))
            );
        }
        
        log.info("Selected device: {} ({})", selectedDevice.getId(), selectedDevice.getHostname());
        return selectedDevice;
    }
    
    /**
     * Select device with explicit strategy
     */
    public Device selectBestDevice(ContainerRequest request, String strategyName) {
        SchedulingStrategy strategy = getStrategyByName(strategyName);
        
        List<Device> onlineDevices = deviceRepository.findByStatus(Device.DeviceStatus.ONLINE);
        
        if (onlineDevices.isEmpty()) {
            throw new NoDeviceAvailableException("No online devices available");
        }
        
        Device selectedDevice = strategy.selectDevice(onlineDevices, request);
        
        if (selectedDevice == null) {
            throw new NoDeviceAvailableException("No suitable device found");
        }
        
        return selectedDevice;
    }
    
    /**
     * Calculate score for a device (for testing/debugging)
     */
    public double calculateDeviceScore(Device device, ContainerRequest request) {
        SchedulingStrategy strategy = getStrategy();
        return strategy.calculateDeviceScore(device, request);
    }
    
    /**
     * Calculate scores for all online devices (for debugging)
     */
    public List<DeviceScore> calculateAllDeviceScores(ContainerRequest request) {
        List<Device> onlineDevices = deviceRepository.findByStatus(Device.DeviceStatus.ONLINE);
        SchedulingStrategy strategy = getStrategy();
        
        return onlineDevices.stream()
            .map(device -> new DeviceScore(
                device.getId(),
                device.getHostname(),
                strategy.calculateDeviceScore(device, request)
            ))
            .sorted((a, b) -> Double.compare(b.score(), a.score()))  // Descending order
            .toList();
    }
    
    /**
     * Get currently configured strategy
     */
    private SchedulingStrategy getStrategy() {
        return getStrategyByName(strategyName);
    }
    
    /**
     * Get strategy by name
     */
    private SchedulingStrategy getStrategyByName(String name) {
        return switch (name.toLowerCase()) {
            case "adaptive", "reservation-aware" -> adaptiveStrategy;
            case "first-available", "simple" -> firstAvailableStrategy;
            default -> {
                log.warn("Unknown strategy '{}', using adaptive", name);
                yield adaptiveStrategy;
            }
        };
    }
    
    /**
     * Get available scheduling strategies
     */
    public List<String> getAvailableStrategies() {
        return List.of(
            adaptiveStrategy.getStrategyName(),
            firstAvailableStrategy.getStrategyName()
        );
    }
    
    /**
     * Device score record for debugging
     */
    public record DeviceScore(Long deviceId, String hostname, double score) {}
}
