package com.campuscompute.controller;

import com.campuscompute.dto.ApiResponse;
import com.campuscompute.entity.Device;
import com.campuscompute.service.DeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for device management (admin only)
 */
@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@Slf4j
public class DeviceController {

    private final DeviceService deviceService;

    /**
     * GET /api/devices
     * Get all devices
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Device>>> getAllDevices() {
        log.info("Fetching all devices");
        
        List<Device> devices = deviceService.getAllDevices();
        return ResponseEntity.ok(ApiResponse.success(devices));
    }

    /**
     * GET /api/devices/{id}
     * Get device by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Device>> getDevice(@PathVariable Long id) {
        log.info("Fetching device {}", id);
        
        return deviceService.getDeviceById(id)
            .map(device -> ResponseEntity.ok(ApiResponse.success(device)))
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/devices/online
     * Get all online devices
     */
    @GetMapping("/online")
    public ResponseEntity<ApiResponse<List<Device>>> getOnlineDevices() {
        log.info("Fetching online devices");
        
        List<Device> devices = deviceService.getOnlineDevices();
        return ResponseEntity.ok(ApiResponse.success(devices));
    }

    /**
     * GET /api/devices/lab/{labName}
     * Get devices by lab name
     */
    @GetMapping("/lab/{labName}")
    public ResponseEntity<ApiResponse<List<Device>>> getDevicesByLab(
        @PathVariable String labName
    ) {
        log.info("Fetching devices for lab: {}", labName);
        
        List<Device> devices = deviceService.getDevicesByLab(labName);
        return ResponseEntity.ok(ApiResponse.success(devices));
    }

    /**
     * GET /api/devices/available
     * Get available devices with sufficient resources
     */
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<Device>>> getAvailableDevices(
        @RequestParam Integer cpuCores,
        @RequestParam Long ramBytes
    ) {
        log.info("Finding available devices: {} cores, {} bytes RAM", cpuCores, ramBytes);
        
        List<Device> devices = deviceService.findAvailableDevices(cpuCores, ramBytes);
        return ResponseEntity.ok(ApiResponse.success(devices));
    }

    /**
     * PUT /api/devices/{id}/enable
     * Enable a device
     */
    @PutMapping("/{id}/enable")
    public ResponseEntity<ApiResponse<String>> enableDevice(@PathVariable Long id) {
        log.info("Enabling device {}", id);
        
        try {
            deviceService.enableDevice(id);
            return ResponseEntity.ok(
                ApiResponse.success("Device enabled successfully", null)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * PUT /api/devices/{id}/disable
     * Disable a device
     */
    @PutMapping("/{id}/disable")
    public ResponseEntity<ApiResponse<String>> disableDevice(@PathVariable Long id) {
        log.info("Disabling device {}", id);
        
        try {
            deviceService.disableDevice(id);
            return ResponseEntity.ok(
                ApiResponse.success("Device disabled successfully", null)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * DELETE /api/devices/{id}
     * Delete a device
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteDevice(@PathVariable Long id) {
        log.info("Deleting device {}", id);
        
        try {
            deviceService.deleteDevice(id);
            return ResponseEntity.ok(
                ApiResponse.success("Device deleted successfully", null)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/devices/stats/resources
     * Get total available resources
     */
    @GetMapping("/stats/resources")
    public ResponseEntity<ApiResponse<DeviceService.ResourceStats>> getTotalResources() {
        log.info("Fetching total available resources");
        
        DeviceService.ResourceStats stats = deviceService.getTotalAvailableResources();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
