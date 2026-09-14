package com.campuscompute.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO for container creation request
 */
@Data
public class ContainerRequest {
    
    @NotBlank(message = "Image is required")
    private String image;
    
    @NotNull(message = "CPU cores is required")
    @Min(value = 1, message = "CPU cores must be at least 1")
    private Integer cpuCores;
    
    @NotNull(message = "RAM is required")
    @Min(value = 1024 * 1024 * 1024, message = "RAM must be at least 1GB")
    private Long ramBytes;
    
    private Long diskBytes = 10L * 1024 * 1024 * 1024; // 10GB default
    
    private Long lifetimeMs = 14400000L; // 4 hours default
}
