package com.campuscompute.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for organization statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationStatsResponse {

    // Organization info
    private Long organizationId;
    private String organizationName;
    private String organizationCode;

    // Device statistics
    private DeviceStats devices;

    // Student statistics
    private StudentStats students;

    // Container statistics
    private ContainerStats containers;

    // Resource statistics
    private ResourceStats resources;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceStats {
        private Long total;
        private Long online;
        private Long offline;
        private Long busy;
        private Long maintenance;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentStats {
        private Long total;
        private Long active;
        private Long approved;
        private Long pending;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContainerStats {
        private Long total;
        private Long running;
        private Long stopped;
        private Long pending;
        private Long failed;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceStats {
        private Long totalCpuCores;
        private Long usedCpuCores;
        private Long availableCpuCores;
        private Long totalRamBytes;
        private Long usedRamBytes;
        private Long availableRamBytes;
        private Double cpuUtilizationPercent;
        private Double ramUtilizationPercent;
    }
}
