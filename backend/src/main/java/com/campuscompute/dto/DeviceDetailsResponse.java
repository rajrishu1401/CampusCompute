package com.campuscompute.dto;

import com.campuscompute.entity.Device;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for device details with computed fields
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceDetailsResponse {

    private Long id;
    private String deviceId;
    private String hostname;
    private String labName;
    private String ipAddress;
    private Device.DeviceStatus status;
    private Boolean enabled;

    // Hardware specs
    private Integer totalCpuCores;
    private Long totalRamBytes;
    private Long totalDiskBytes;

    // Current usage
    private Integer usedCpuCores;
    private Long usedRamBytes;
    private Long usedDiskBytes;

    // Computed availability
    private Integer availableCpuCores;
    private Long availableRamBytes;
    private Long availableDiskBytes;

    // Load metrics
    private Double cpuLoadPercent;
    private Double ramLoadPercent;
    private Double reliabilityScore;

    // Container info
    private Integer activeContainers;

    // Timestamps
    private LocalDateTime lastHeartbeat;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Versions
    private String agentVersion;
    private String dockerVersion;

    // Organization
    private Long organizationId;
    private String organizationName;

    /**
     * Create from Device entity
     */
    public static DeviceDetailsResponse fromDevice(Device device, Integer activeContainers) {
        DeviceDetailsResponse response = new DeviceDetailsResponse();
        
        response.setId(device.getId());
        response.setDeviceId(device.getDeviceId());
        response.setHostname(device.getHostname());
        response.setLabName(device.getLabName());
        response.setIpAddress(device.getIpAddress());
        response.setStatus(device.getStatus());
        response.setEnabled(device.getEnabled());

        response.setTotalCpuCores(device.getTotalCpuCores());
        response.setTotalRamBytes(device.getTotalRamBytes());
        response.setTotalDiskBytes(device.getTotalDiskBytes());

        response.setUsedCpuCores(device.getUsedCpuCores());
        response.setUsedRamBytes(device.getUsedRamBytes());
        response.setUsedDiskBytes(device.getUsedDiskBytes());

        // Compute available resources
        response.setAvailableCpuCores(device.getTotalCpuCores() - device.getUsedCpuCores());
        response.setAvailableRamBytes(device.getTotalRamBytes() - device.getUsedRamBytes());
        response.setAvailableDiskBytes(device.getTotalDiskBytes() - device.getUsedDiskBytes());

        response.setCpuLoadPercent(device.getCpuLoadPercent());
        response.setRamLoadPercent(device.getRamLoadPercent());
        response.setReliabilityScore(device.getReliabilityScore());

        response.setActiveContainers(activeContainers != null ? activeContainers : 0);

        response.setLastHeartbeat(device.getLastHeartbeat());
        response.setCreatedAt(device.getCreatedAt());
        response.setUpdatedAt(device.getUpdatedAt());

        response.setAgentVersion(device.getAgentVersion());
        response.setDockerVersion(device.getDockerVersion());

        if (device.getOrganization() != null) {
            response.setOrganizationId(device.getOrganization().getId());
            response.setOrganizationName(device.getOrganization().getName());
        }

        return response;
    }
}
