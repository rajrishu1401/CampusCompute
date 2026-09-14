package com.campuscompute.controller;

import com.campuscompute.dto.ApiResponse;
import com.campuscompute.dto.ContainerRequest;
import com.campuscompute.entity.Container;
import com.campuscompute.service.ContainerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for container management
 */
@RestController
@RequestMapping("/api/containers")
@RequiredArgsConstructor
@Slf4j
public class ContainerController {

    private final ContainerService containerService;

    /**
     * POST /api/containers
     * Create a new container request
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Container>> createContainer(
        @Valid @RequestBody ContainerRequest request,
        HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        log.info("Container creation request from user {}: {}", userId, request.getImage());
        
        try {
            Container container = containerService.createContainerRequest(
                userId,
                request.getImage(),
                request.getCpuCores(),
                request.getRamBytes(),
                request.getDiskBytes(),
                request.getLifetimeMs()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Container request created", container));
                
        } catch (IllegalArgumentException e) {
            log.error("Container creation failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/containers
     * Get all containers for current user
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Container>>> getUserContainers(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        log.info("Fetching containers for user {}", userId);
        
        List<Container> containers = containerService.getContainersByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(containers));
    }

    /**
     * GET /api/containers/{id}
     * Get container by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Container>> getContainer(
        @PathVariable Long id,
        HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        log.info("Fetching container {} for user {}", id, userId);
        
        return containerService.getContainerById(id)
            .map(container -> {
                // Check ownership
                if (!container.getUser().getId().equals(userId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .<ApiResponse<Container>>body(ApiResponse.error("Access denied"));
                }
                return ResponseEntity.ok(ApiResponse.success(container));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/containers/{id}
     * Stop and delete a container
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteContainer(
        @PathVariable Long id,
        HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        log.info("Deleting container {} by user {}", id, userId);
        
        try {
            Container container = containerService.getContainerById(id)
                .orElseThrow(() -> new IllegalArgumentException("Container not found"));
            
            // Check ownership
            if (!container.getUser().getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied"));
            }
            
            containerService.deleteContainer(id);
            
            return ResponseEntity.ok(
                ApiResponse.success("Container deleted successfully", null)
            );
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * POST /api/containers/{id}/stop
     * Stop a running container
     */
    @PostMapping("/{id}/stop")
    public ResponseEntity<ApiResponse<Container>> stopContainer(
        @PathVariable Long id,
        HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        log.info("Stopping container {} by user {}", id, userId);
        
        try {
            Container container = containerService.getContainerById(id)
                .orElseThrow(() -> new IllegalArgumentException("Container not found"));
            
            // Check ownership
            if (!container.getUser().getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied"));
            }
            
            Container stoppedContainer = containerService.stopContainer(id);
            
            return ResponseEntity.ok(
                ApiResponse.success("Container stopped", stoppedContainer)
            );
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/containers/running
     * Get all running containers for current user
     */
    @GetMapping("/running")
    public ResponseEntity<ApiResponse<List<Container>>> getRunningContainers(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        log.info("Fetching running containers for user {}", userId);
        
        List<Container> containers = containerService.getRunningContainersByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(containers));
    }

    /**
     * GET /api/containers/stats
     * Get user's resource usage statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<ContainerService.UserResourceUsage>> getResourceStats(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        log.info("Fetching resource stats for user {}", userId);
        
        ContainerService.UserResourceUsage usage = containerService.getUserResourceUsage(userId);
        return ResponseEntity.ok(ApiResponse.success(usage));
    }
}
