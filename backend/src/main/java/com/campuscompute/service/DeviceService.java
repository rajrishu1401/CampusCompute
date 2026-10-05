package com.campuscompute.service;

import com.campuscompute.entity.Device;
import com.campuscompute.entity.Organization;
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
    private final com.campuscompute.service.OrganizationService organizationService;
    private final com.campuscompute.repository.OrganizationRepository organizationRepository;

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
     * Register device with enrollment token (Phase 2: Multi-org support)
     * 
     * @param enrollmentToken The enrollment token from organization
     * @param queryString WebSocket query string with device info
     * @return Registered device
     */
    public Device registerDeviceWithToken(String enrollmentToken, String queryString) {
        log.info("Registering device with enrollment token: {}...", enrollmentToken.substring(0, Math.min(8, enrollmentToken.length())));
        
        // Parse query string for device info
        String deviceId = extractParamFromQuery(queryString, "deviceId");
        if (deviceId == null) {
            throw new IllegalArgumentException("Device ID required for enrollment");
        }
        
        // Extract organization ID from query string
        String orgIdStr = extractParamFromQuery(queryString, "organizationId");
        Long organizationId = null;
        if (orgIdStr != null) {
            try {
                organizationId = Long.parseLong(orgIdStr);
                log.info("Organization ID from query: {}", organizationId);
            } catch (NumberFormatException e) {
                log.warn("Invalid organization ID in query: {}", orgIdStr);
            }
        }
        
        // Verify organization exists
        Organization organization = null;
        if (organizationId != null) {
            organization = organizationRepository.findById(organizationId).orElse(null);
            if (organization == null) {
                log.warn("Organization {} not found, device will be unassigned", organizationId);
            }
        }
        
        // Check if device already exists
        Optional<Device> existing = deviceRepository.findByDeviceId(deviceId);
        if (existing.isPresent()) {
            log.info("Device already registered: {}, updating status", deviceId);
            Device device = existing.get();
            device.setStatus(Device.DeviceStatus.ONLINE);
            device.setLastHeartbeat(LocalDateTime.now());
            
            // Update organization if provided and different
            if (organization != null && !organization.equals(device.getOrganization())) {
                log.info("Updating device organization to: {}", organization.getCode());
                device.setOrganization(organization);
            }
            
            return deviceRepository.save(device);
        }
        
        // Create new device
        Device newDevice = new Device();
        newDevice.setDeviceId(deviceId);
        newDevice.setHostname(extractParamFromQuery(queryString, "hostname", deviceId));
        newDevice.setLabName("Unassigned");
        newDevice.setStatus(Device.DeviceStatus.ONLINE);
        newDevice.setEnabled(true);
        newDevice.setLastHeartbeat(LocalDateTime.now());
        
        // Assign organization
        if (organization != null) {
            newDevice.setOrganization(organization);
            log.info("✅ Device will be assigned to organization: {}", organization.getCode());
        } else {
            log.warn("⚠️  Device registered without organization assignment");
        }
        
        // Default hardware specs (will be updated on first heartbeat)
        newDevice.setTotalCpuCores(4);
        newDevice.setTotalRamBytes(8L * 1024 * 1024 * 1024);  // 8GB
        newDevice.setTotalDiskBytes(100L * 1024 * 1024 * 1024); // 100GB
        newDevice.setUsedCpuCores(0);
        newDevice.setUsedRamBytes(0L);
        newDevice.setUsedDiskBytes(0L);
        newDevice.setCpuLoadPercent(0.0);
        newDevice.setRamLoadPercent(0.0);
        newDevice.setReliabilityScore(1.0);
        
        Device saved = deviceRepository.save(newDevice);
        log.info("✅ New device registered: {} (ID: {})", deviceId, saved.getId());
        
        return saved;
    }
    
    /**
     * Helper to extract parameter from query string
     */
    private String extractParamFromQuery(String query, String paramName) {
        return extractParamFromQuery(query, paramName, null);
    }
    
    /**
     * Helper to extract parameter from query string with default value
     */
    private String extractParamFromQuery(String query, String paramName, String defaultValue) {
        if (query == null || query.isEmpty()) {
            return defaultValue;
        }
        
        String[] params = query.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2 && paramName.equals(keyValue[0])) {
                return keyValue[1];
            }
        }
        
        return defaultValue;
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
     * Update device status (by ID)
     */
    public void updateDeviceStatus(Long deviceId, Device.DeviceStatus status) {
        log.info("Updating device {} status to {}", deviceId, status);
        
        Device device = deviceRepository.findById(deviceId)
            .orElseThrow(() -> new IllegalArgumentException("Device not found: " + deviceId));
        
        device.setStatus(status);
        deviceRepository.save(device);
    }
    
    /**
     * Update device status (by device ID string)
     */
    public void updateDeviceStatus(String deviceId, Device.DeviceStatus status) {
        log.info("Updating device {} status to {}", deviceId, status);
        
        Device device = deviceRepository.findByDeviceId(deviceId)
            .orElseThrow(() -> new IllegalArgumentException("Device not found: " + deviceId));
        
        device.setStatus(status);
        deviceRepository.save(device);
    }
    
    /**
     * Update last heartbeat timestamp
     */
    public void updateLastHeartbeat(Long deviceId) {
        log.debug("Updating heartbeat for device ID: {}", deviceId);
        
        Device device = deviceRepository.findById(deviceId)
            .orElseThrow(() -> new IllegalArgumentException("Device not found: " + deviceId));
        
        device.setLastHeartbeat(LocalDateTime.now());
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
