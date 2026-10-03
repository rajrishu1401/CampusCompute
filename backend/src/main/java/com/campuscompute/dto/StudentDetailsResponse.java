package com.campuscompute.dto;

import com.campuscompute.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for student details with statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentDetailsResponse {

    private Long id;
    private String username;
    private String studentId;
    private String email;
    private String fullName;
    private String department;
    private Boolean approved;
    private Boolean active;

    // Quotas
    private Integer maxCpuCores;
    private Integer maxRamGb;
    private Integer maxContainers;

    // Current usage
    private Integer currentContainers;
    private Integer runningContainers;
    private Long totalCpuUsed;
    private Long totalRamUsed;

    // Activity
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;

    // Organization
    private Long organizationId;
    private String organizationName;

    /**
     * Create from User entity
     */
    public static StudentDetailsResponse fromUser(User user, Integer currentContainers, 
                                                  Integer runningContainers, Long cpuUsed, Long ramUsed) {
        StudentDetailsResponse response = new StudentDetailsResponse();
        
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setStudentId(user.getStudentId());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setDepartment(user.getDepartment());
        response.setApproved(user.getApproved());
        response.setActive(user.getActive());

        response.setMaxCpuCores(user.getMaxCpuCores());
        response.setMaxRamGb(user.getMaxRamGb());
        response.setMaxContainers(user.getMaxContainers());

        response.setCurrentContainers(currentContainers != null ? currentContainers : 0);
        response.setRunningContainers(runningContainers != null ? runningContainers : 0);
        response.setTotalCpuUsed(cpuUsed != null ? cpuUsed : 0L);
        response.setTotalRamUsed(ramUsed != null ? ramUsed : 0L);

        response.setCreatedAt(user.getCreatedAt());

        if (user.getOrganization() != null) {
            response.setOrganizationId(user.getOrganization().getId());
            response.setOrganizationName(user.getOrganization().getName());
        }

        return response;
    }
}
