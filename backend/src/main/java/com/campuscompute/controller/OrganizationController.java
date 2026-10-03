package com.campuscompute.controller;

import com.campuscompute.dto.*;
import com.campuscompute.entity.Organization;
import com.campuscompute.entity.User;
import com.campuscompute.service.OrganizationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for organization management endpoints
 */
@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
@Slf4j
public class OrganizationController {

    private final OrganizationService organizationService;

    /**
     * POST /api/organizations/register
     * Register a new organization (Public endpoint)
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Organization>> registerOrganization(
        @Valid @RequestBody OrganizationRegisterRequest request
    ) {
        log.info("Organization registration request: {}", request.getCode());

        try {
            Organization organization = organizationService.registerOrganization(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Organization registered successfully", organization));

        } catch (IllegalArgumentException e) {
            log.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/organizations/me
     * Get current user's organization details
     */
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ORG_ADMIN', 'STUDENT')")
    public ResponseEntity<ApiResponse<Organization>> getCurrentOrganization(
        HttpServletRequest request
    ) {
        try {
            Long orgId = (Long) request.getAttribute("organizationId");

            if (orgId == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("No organization associated with user"));
            }

            Organization organization = organizationService.getOrganizationById(orgId);
            return ResponseEntity.ok(
                ApiResponse.success("Organization retrieved successfully", organization)
            );

        } catch (Exception e) {
            log.error("Error fetching organization: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to fetch organization"));
        }
    }

    /**
     * PUT /api/organizations/me
     * Update current organization details (ORG_ADMIN only)
     */
    @PutMapping("/me")
    @PreAuthorize("hasRole('ORG_ADMIN')")
    public ResponseEntity<ApiResponse<Organization>> updateOrganization(
        HttpServletRequest request,
        @RequestBody Organization updatedOrg
    ) {
        try {
            Long orgId = (Long) request.getAttribute("organizationId");

            if (orgId == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("No organization associated with user"));
            }

            Organization organization = organizationService.updateOrganization(orgId, updatedOrg);
            return ResponseEntity.ok(
                ApiResponse.success("Organization updated successfully", organization)
            );

        } catch (Exception e) {
            log.error("Error updating organization: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to update organization"));
        }
    }

    /**
     * POST /api/organizations/devices/token
     * Generate device enrollment token (ORG_ADMIN only)
     */
    @PostMapping("/devices/token")
    @PreAuthorize("hasRole('ORG_ADMIN')")
    public ResponseEntity<ApiResponse<EnrollmentTokenResponse>> generateEnrollmentToken(
        HttpServletRequest request
    ) {
        try {
            Long orgId = (Long) request.getAttribute("organizationId");

            if (orgId == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("No organization associated with user"));
            }

            EnrollmentTokenResponse response = organizationService.generateEnrollmentToken(orgId);
            return ResponseEntity.ok(
                ApiResponse.success("Enrollment token generated successfully", response)
            );

        } catch (Exception e) {
            log.error("Error generating enrollment token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to generate enrollment token"));
        }
    }

    /**
     * POST /api/organizations/students/upload
     * Bulk upload students via CSV (ORG_ADMIN only)
     */
    @PostMapping("/students/upload")
    @PreAuthorize("hasRole('ORG_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> bulkUploadStudents(
        HttpServletRequest request,
        @Valid @RequestBody StudentBulkUploadRequest uploadRequest
    ) {
        try {
            Long orgId = (Long) request.getAttribute("organizationId");

            if (orgId == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("No organization associated with user"));
            }

            List<User> createdStudents = organizationService.bulkRegisterStudents(orgId, uploadRequest);

            Map<String, Object> result = Map.of(
                "totalRequested", uploadRequest.getStudents().size(),
                "totalCreated", createdStudents.size(),
                "students", createdStudents
            );

            return ResponseEntity.ok(
                ApiResponse.success("Students uploaded successfully", result)
            );

        } catch (Exception e) {
            log.error("Error uploading students: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to upload students"));
        }
    }

    /**
     * GET /api/organizations
     * Get all organizations (ROOT only)
     */
    @GetMapping
    @PreAuthorize("hasRole('ROOT')")
    public ResponseEntity<ApiResponse<List<Organization>>> getAllOrganizations() {
        try {
            List<Organization> organizations = organizationService.getAllOrganizations();
            return ResponseEntity.ok(
                ApiResponse.success("Organizations retrieved successfully", organizations)
            );

        } catch (Exception e) {
            log.error("Error fetching organizations: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to fetch organizations"));
        }
    }

    /**
     * GET /api/organizations/{id}
     * Get organization by ID (ROOT only)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROOT')")
    public ResponseEntity<ApiResponse<Organization>> getOrganizationById(@PathVariable Long id) {
        try {
            Organization organization = organizationService.getOrganizationById(id);
            return ResponseEntity.ok(
                ApiResponse.success("Organization retrieved successfully", organization)
            );

        } catch (Exception e) {
            log.error("Error fetching organization: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Organization not found"));
        }
    }

    /**
     * GET /api/organizations/stats
     * Get organization statistics (ORG_ADMIN only)
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ORG_ADMIN')")
    public ResponseEntity<ApiResponse<com.campuscompute.dto.OrganizationStatsResponse>> getOrganizationStats(
        HttpServletRequest request
    ) {
        try {
            Long orgId = (Long) request.getAttribute("organizationId");

            if (orgId == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("No organization associated with user"));
            }

            com.campuscompute.dto.OrganizationStatsResponse stats = organizationService.getOrganizationStats(orgId);
            return ResponseEntity.ok(
                ApiResponse.success("Statistics retrieved successfully", stats)
            );

        } catch (Exception e) {
            log.error("Error fetching statistics: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to fetch statistics"));
        }
    }

    /**
     * GET /api/organizations/devices
     * Get organization's devices with details (ORG_ADMIN only)
     */
    @GetMapping("/devices")
    @PreAuthorize("hasRole('ORG_ADMIN')")
    public ResponseEntity<ApiResponse<java.util.List<com.campuscompute.dto.DeviceDetailsResponse>>> getOrganizationDevices(
        HttpServletRequest request
    ) {
        try {
            Long orgId = (Long) request.getAttribute("organizationId");

            if (orgId == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("No organization associated with user"));
            }

            java.util.List<com.campuscompute.dto.DeviceDetailsResponse> devices = 
                organizationService.getOrganizationDevicesWithStats(orgId);

            return ResponseEntity.ok(
                ApiResponse.success("Devices retrieved successfully", devices)
            );

        } catch (Exception e) {
            log.error("Error fetching devices: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to fetch devices"));
        }
    }

    /**
     * GET /api/organizations/students
     * Get organization's students with details (ORG_ADMIN only)
     */
    @GetMapping("/students")
    @PreAuthorize("hasRole('ORG_ADMIN')")
    public ResponseEntity<ApiResponse<java.util.List<com.campuscompute.dto.StudentDetailsResponse>>> getOrganizationStudents(
        HttpServletRequest request
    ) {
        try {
            Long orgId = (Long) request.getAttribute("organizationId");

            if (orgId == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("No organization associated with user"));
            }

            java.util.List<com.campuscompute.dto.StudentDetailsResponse> students = 
                organizationService.getOrganizationStudentsWithStats(orgId);

            return ResponseEntity.ok(
                ApiResponse.success("Students retrieved successfully", students)
            );

        } catch (Exception e) {
            log.error("Error fetching students: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to fetch students"));
        }
    }
}
