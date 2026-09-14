package com.campuscompute.controller;

import com.campuscompute.dto.ApiResponse;
import com.campuscompute.entity.Lab;
import com.campuscompute.service.LabService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for lab management (Admin only)
 */
@RestController
@RequestMapping("/api/admin/labs")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class LabController {

    private final LabService labService;

    /**
     * GET /api/admin/labs
     * Get all labs
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Lab>>> getAllLabs(
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(required = false) String department) {
        
        List<Lab> labs;
        
        if (department != null) {
            labs = labService.getLabsByDepartment(department);
        } else if (activeOnly != null && activeOnly) {
            labs = labService.getActiveLabs();
        } else {
            labs = labService.getAllLabs();
        }
        
        return ResponseEntity.ok(ApiResponse.success(labs));
    }

    /**
     * GET /api/admin/labs/:id
     * Get lab by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Lab>> getLabById(@PathVariable Long id) {
        Lab lab = labService.getLabById(id);
        return ResponseEntity.ok(ApiResponse.success(lab));
    }

    /**
     * POST /api/admin/labs
     * Create a new lab
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Lab>> createLab(@Valid @RequestBody Lab lab) {
        log.info("Creating new lab: {}", lab.getName());
        Lab created = labService.createLab(lab);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Lab created successfully", created));
    }

    /**
     * PUT /api/admin/labs/:id
     * Update lab
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Lab>> updateLab(
            @PathVariable Long id,
            @Valid @RequestBody Lab lab) {
        
        log.info("Updating lab ID: {}", id);
        Lab updated = labService.updateLab(id, lab);
        return ResponseEntity.ok(ApiResponse.success("Lab updated successfully", updated));
    }

    /**
     * DELETE /api/admin/labs/:id
     * Delete lab
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLab(@PathVariable Long id) {
        log.info("Deleting lab ID: {}", id);
        labService.deleteLab(id);
        return ResponseEntity.ok(ApiResponse.success("Lab deleted successfully", null));
    }

    /**
     * PATCH /api/admin/labs/:id/enable
     * Enable lab
     */
    @PatchMapping("/{id}/enable")
    public ResponseEntity<ApiResponse<Lab>> enableLab(@PathVariable Long id) {
        log.info("Enabling lab ID: {}", id);
        Lab lab = labService.enableLab(id);
        return ResponseEntity.ok(ApiResponse.success("Lab enabled successfully", lab));
    }

    /**
     * PATCH /api/admin/labs/:id/disable
     * Disable lab
     */
    @PatchMapping("/{id}/disable")
    public ResponseEntity<ApiResponse<Lab>> disableLab(@PathVariable Long id) {
        log.info("Disabling lab ID: {}", id);
        Lab lab = labService.disableLab(id);
        return ResponseEntity.ok(ApiResponse.success("Lab disabled successfully", lab));
    }

    /**
     * GET /api/admin/labs/:id/devices/count
     * Get active device count for lab
     */
    @GetMapping("/{id}/devices/count")
    public ResponseEntity<ApiResponse<Long>> getActiveDeviceCount(@PathVariable Long id) {
        long count = labService.getActiveDeviceCount(id);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
