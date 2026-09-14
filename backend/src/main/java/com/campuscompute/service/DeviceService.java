package com.campuscompute.service;

import com.campuscompute.entity.Device;
import com.campuscompute.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for device (lab computer) management
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DeviceService {

    private final DeviceRepository deviceRepository;

    /**
     * Register a new device (agent enrollment)
     */
    public Device registerDevice(Device device) {
        log.info("Registering device: {}", device.getDeviceId());
        
        // Check if device already exists
        Optional<Device> existing = deviceRepository.findByDeviceId(device.getDeviceId());
        if (existing.isPresent()) {
            log.info("Device already registered, updating: {}", device.getDeviceId());
            Device existingDevice = existing.get();
            
            // Update hardware specs (may have changed)
            existingDevice.setHostname(device.getHostname());
            existingDevice.setLabName(device.getLabName());
            existingDevice.setIpAddress(device.getIpAddress());
            existingDevice.setTotalCpuCores(device.getTotalCpuCores());
            existingDevice.setTotalRamBytes(device.getTotalRamBytes());
            existingDevice.setTotalDiskBytes(device.getTotalDiskBytes());
            existingDevice.setAgentVersion(device.getAgentVersion());
            existingDevice.setDockerVersion(device.getDockerVersion());
            existingDevice.setStatus(Device.DeviceStatus.ONLINE);
            existingDevice.setLastHeartbeat(LocalDateTime.now());
            
            return deviceRepository.save(existingDevice);
        }
        
        // New device
        device.setStatus(Device.DeviceStatus.ONLINE);
        device.setLastHeartbeat(LocalDateTime.now());
        return deviceRepository.save(device);
    }

    /**
     * Update device heartbeat (agent health check)
     */
    public void updateHeartbeat(String deviceId, Double cpuLoad, Double ramLoad, 
                                Integer usedCpuCores, Long usedRamBytes) {
        log.debug("Heartbeat from device: {}", deviceId);
        
        Device device = deviceRepository.findByDeviceId(deviceId)
            .orElseThrow(() -> new IllegalArgumentException("Device not found: " + deviceId));
        
        device.setLastHeartbeat(LocalDateTime.now());
        device.setStatus(Device.DeviceStatus.ONLINE);
        device.setCpuLoadPercent(cpuLoad);
        device.setRamLoadPercent(ramLoad);
        device.setUsedCpuCores(usedCpuCores);
        device.setUsedRamBytes(usedRamBytes);
        
        deviceRepository.save(device);
    }

    /**
     * Get device by ID
     */
    public Optional<Device> getDeviceById(Long id) {
        return deviceRepository.findById(id);
    }

    /**
     * Get device by device ID
     */
    public Optional<Device> getDeviceByDeviceId(String deviceId) {
        return deviceRepository.findByDeviceId(deviceId);
    }

    /**
     * Get all devices
     */
    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    /**
     * Get online devices
     */
    public List<Device> getOnlineDevices() {
        return deviceRepository.findByStatusAndEnabledTrue(Device.DeviceStatus.ONLINE);
    }

    /**
     * Get devices by lab
     */
    public List<Device> getDevicesByLab(String labName) {
        return deviceRepository.findByLabName(labName);
    }

    /**
     * Get online devices in a specific lab
     */
    public List<Device> getOnlineDevicesByLab(String labName) {
        return deviceRepository.findByLabNameAndStatusAndEnabledTrue(
            labName, Device.DeviceStatus.ONLINE
        );
    }

    /**
     * Find available devices with sufficient resources
     */
    public List<Device> findAvailableDevices(Integer cpuCores, Long ramBytes) {
        return deviceRepository.findAvailableDevices(
            cpuCores, ramBytes, 80.0, 80.0
        );
    }

    /**
     * Update device status
     */
    public void updateDeviceStatus(String deviceId, Device.DeviceStatus status) {
        log.info("Updating device {} status to {}", deviceId, status);
        
        Device device = deviceRepository.findByDeviceId(deviceId)
            .orElseThrow(() -> new IllegalArgumentException("Device not found: " + deviceId));
        
        device.setStatus(status);
        deviceRepository.save(device);
    }

    /**
     * Enable device
     */
    public void enableDevice(Long deviceId) {
        log.info("Enabling device ID: {}", deviceId);
        
        Device device = deviceRepository.findById(deviceId)
            .orElseThrow(() -> new IllegalArgumentException("Device not found: " + deviceId));
        
        device.setEnabled(true);
        deviceRepository.save(device);
    }

    /**
     * Disable device
     */
    public void disableDevice(Long deviceId) {
        log.info("Disabling device ID: {}", deviceId);
        
        Device device = deviceRepository.findById(deviceId)
            .orElseThrow(() -> new IllegalArgumentException("Device not found: " + deviceId));
        
        device.setEnabled(false);
        device.setStatus(Device.DeviceStatus.MAINTENANCE);
        deviceRepository.save(device);
    }

    /**
     * Allocate resources on device
     */
    public void allocateResources(Long deviceId, Integer cpuCores, Long ramBytes, Long diskBytes) {
        Device device = deviceRepository.findById(deviceId)
            .orElseThrow(() -> new IllegalArgumentException("Device not found: " + deviceId));
        
        device.setUsedCpuCores(device.getUsedCpuCores() + cpuCores);
        device.setUsedRamBytes(device.getUsedRamBytes() + ramBytes);
        device.setUsedDiskBytes(device.getUsedDiskBytes() + diskBytes);
        
        deviceRepository.save(device);
    }

    /**
     * Release resources on device
     */
    public void releaseResources(Long deviceId, Integer cpuCores, Long ramBytes, Long diskBytes) {
        Device device = deviceRepository.findById(deviceId)
            .orElseThrow(() -> new IllegalArgumentException("Device not found: " + deviceId));
        
        device.setUsedCpuCores(Math.max(0, device.getUsedCpuCores() - cpuCores));
        device.setUsedRamBytes(Math.max(0, device.getUsedRamBytes() - ramBytes));
        device.setUsedDiskBytes(Math.max(0, device.getUsedDiskBytes() - diskBytes));
        
        deviceRepository.save(device);
    }

    /**
     * Mark stale devices as offline (haven't sent heartbeat)
     */
    public void markStaleDevicesOffline(int timeoutSeconds) {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(timeoutSeconds);
        List<Device> staleDevices = deviceRepository.findStaleDevices(threshold);
        
        for (Device device : staleDevices) {
            log.warn("Marking device offline (stale): {}", device.getDeviceId());
            device.setStatus(Device.DeviceStatus.OFFLINE);
            deviceRepository.save(device);
        }
    }

    /**
     * Get total available resources across all online devices
     */
    public ResourceStats getTotalAvailableResources() {
        Long totalCpu = deviceRepository.getTotalAvailableCpuCores();
        Long totalRam = deviceRepository.getTotalAvailableRamBytes();
        
        return new ResourceStats(
            totalCpu != null ? totalCpu : 0L,
            totalRam != null ? totalRam : 0L
        );
    }

    /**
     * Delete device
     */
    public void deleteDevice(Long deviceId) {
        log.info("Deleting device ID: {}", deviceId);
        
        if (!deviceRepository.existsById(deviceId)) {
            throw new IllegalArgumentException("Device not found: " + deviceId);
        }
        
        deviceRepository.deleteById(deviceId);
    }

    /**
     * Resource statistics holder
     */
    public record ResourceStats(Long availableCpuCores, Long availableRamBytes) {}
}
