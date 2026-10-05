package com.campuscompute.service;

import com.campuscompute.entity.Container;
import com.campuscompute.entity.Device;
import com.campuscompute.entity.User;
import com.campuscompute.repository.ContainerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for container lifecycle management
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ContainerService {

    private final ContainerRepository containerRepository;
    private final DeviceService deviceService;
    private final UserService userService;
    private final QuotaService quotaService;
    private final SchedulerService schedulerService;
    private final com.campuscompute.websocket.WebSocketSessionManager webSocketSessionManager;
    private final com.campuscompute.websocket.AgentWebSocketHandler agentWebSocketHandler;

    /**
     * Create a container request with integrated quota checking and scheduling
     * This creates a PENDING container, checks quota, schedules it, and assigns to device
     */
    @Transactional
    public Container createContainerRequest(Long userId, String image, 
                                           Integer cpuCores, Long ramBytes, Long diskBytes,
                                           Long lifetimeMs) {
        log.info("Creating container request for user ID: {}", userId);
        
        User user = userService.getUserById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        // STEP 1: Check user quota (using QuotaService)
        log.info("Checking quota for user {}", userId);
        quotaService.checkQuota(user, cpuCores, ramBytes);
        
        // STEP 2: Create container entity (PENDING state)
        Container container = new Container();
        container.setContainerId(UUID.randomUUID().toString());
        container.setContainerName("container-" + userId + "-" + System.currentTimeMillis());
        container.setUser(user);
        container.setStatus(Container.ContainerStatus.PENDING);
        container.setImage(image);
        container.setAllocatedCpuCores(cpuCores);
        container.setAllocatedRamBytes(ramBytes);
        container.setAllocatedDiskBytes(diskBytes);
        container.setExpiresAt(LocalDateTime.now().plus(
            java.time.Duration.ofMillis(lifetimeMs)
        ));
        
        container = containerRepository.save(container);
        log.info("Created container {} in PENDING state", container.getId());
        
        // STEP 3: Schedule container to a device
        try {
            com.campuscompute.dto.ContainerRequest request = new com.campuscompute.dto.ContainerRequest();
            request.setImage(image);
            request.setCpuCores(cpuCores);
            request.setRamBytes(ramBytes);
            request.setDiskBytes(diskBytes);
            request.setLifetimeMs(lifetimeMs);
            
            Device selectedDevice = schedulerService.selectBestDevice(request);
            log.info("Scheduler selected device {} for container {}", selectedDevice.getId(), container.getId());
            
            // STEP 4: Assign container to device
            container = assignContainerToDevice(container.getId(), selectedDevice.getId());
            
            // STEP 5: Send CREATE_CONTAINER message to agent via WebSocket
            try {
                sendCreateContainerToAgent(container, selectedDevice);
            } catch (Exception e) {
                log.error("Failed to send CREATE_CONTAINER to agent: {}", e.getMessage());
                // Don't fail the request - agent might reconnect and pick up PENDING containers
            }
            
            return container;
            
        } catch (Exception e) {
            log.error("Failed to schedule/assign container {}: {}", container.getId(), e.getMessage());
            markContainerFailed(container.getId());
            throw e;
        }
    }
    
    /**
     * Send CREATE_CONTAINER message to agent via WebSocket
     */
    private void sendCreateContainerToAgent(Container container, Device device) {
        log.info("Sending CREATE_CONTAINER message to device {} for container {}", 
            device.getId(), container.getId());
        
        // Build container specification
        java.util.Map<String, Object> containerSpec = new java.util.HashMap<>();
        containerSpec.put("containerId", container.getId().toString());
        containerSpec.put("containerName", container.getContainerName());
        containerSpec.put("image", container.getImage());
        containerSpec.put("cpuCores", container.getAllocatedCpuCores());
        containerSpec.put("ramBytes", container.getAllocatedRamBytes());
        containerSpec.put("diskBytes", container.getAllocatedDiskBytes());
        
        // Create message
        com.campuscompute.dto.AgentMessage message = 
            com.campuscompute.dto.AgentMessage.createContainer(
                device.getId(),
                container.getId().toString(),
                containerSpec
            );
        
        // Get agent session and send message
        webSocketSessionManager.getSession(device.getId()).ifPresentOrElse(
            session -> {
                try {
                    agentWebSocketHandler.sendMessage(session, message);
                    log.info("CREATE_CONTAINER message sent successfully to device {}", device.getId());
                } catch (Exception e) {
                    log.error("Failed to send message to agent: {}", e.getMessage());
                }
            },
            () -> log.error("No WebSocket session found for device {}", device.getId())
        );
    }
    
    /**
     * Send STOP_CONTAINER message to agent via WebSocket
     */
    private void sendStopContainerToAgent(Container container) {
        Device device = container.getDevice();
        log.info("Sending STOP_CONTAINER message to device {} for container {}", 
            device.getId(), container.getId());
        
        // Create message using factory method
        com.campuscompute.dto.AgentMessage message = 
            com.campuscompute.dto.AgentMessage.stopContainer(
                device.getId(),
                container.getId().toString(),
                container.getContainerId() // Docker container ID
            );
        
        // Send message
        webSocketSessionManager.getSession(device.getId()).ifPresentOrElse(
            session -> {
                try {
                    agentWebSocketHandler.sendMessage(session, message);
                    log.info("STOP_CONTAINER message sent successfully to device {}", device.getId());
                } catch (Exception e) {
                    log.error("Failed to send STOP_CONTAINER to agent: {}", e.getMessage());
                }
            },
            () -> log.error("No WebSocket session found for device {}", device.getId())
        );
    }
    
    /**
     * Send DELETE_CONTAINER message to agent via WebSocket
     */
    private void sendDeleteContainerToAgent(Container container) {
        Device device = container.getDevice();
        log.info("Sending DELETE_CONTAINER message to device {} for container {}", 
            device.getId(), container.getId());
        
        // Create message using factory method
        com.campuscompute.dto.AgentMessage message = 
            com.campuscompute.dto.AgentMessage.deleteContainer(
                device.getId(),
                container.getId().toString(),
                container.getContainerId() // Docker container ID
            );
        
        // Send message
        webSocketSessionManager.getSession(device.getId()).ifPresentOrElse(
            session -> {
                try {
                    agentWebSocketHandler.sendMessage(session, message);
                    log.info("DELETE_CONTAINER message sent successfully to device {}", device.getId());
                } catch (Exception e) {
                    log.error("Failed to send DELETE_CONTAINER to agent: {}", e.getMessage());
                }
            },
            () -> log.error("No WebSocket session found for device {}", device.getId())
        );
    }

    /**
     * Assign container to a device (after scheduling)
     */
    public Container assignContainerToDevice(Long containerId, Long deviceId) {
        log.info("Assigning container {} to device {}", containerId, deviceId);
        
        Container container = containerRepository.findById(containerId)
            .orElseThrow(() -> new IllegalArgumentException("Container not found: " + containerId));
        
        Device device = deviceService.getDeviceById(deviceId)
            .orElseThrow(() -> new IllegalArgumentException("Device not found: " + deviceId));
        
        // Allocate resources on device
        deviceService.allocateResources(
            deviceId,
            container.getAllocatedCpuCores(),
            container.getAllocatedRamBytes(),
            container.getAllocatedDiskBytes()
        );
        
        container.setDevice(device);
        container.setStatus(Container.ContainerStatus.CREATING);
        
        return containerRepository.save(container);
    }

    /**
     * Mark container as running (after Docker creation)
     */
    public Container markContainerRunning(Long containerId, String dockerContainerId) {
        log.info("Marking container {} as running", containerId);
        
        Container container = containerRepository.findById(containerId)
            .orElseThrow(() -> new IllegalArgumentException("Container not found: " + containerId));
        
        container.setContainerId(dockerContainerId);
        container.setStatus(Container.ContainerStatus.RUNNING);
        container.setStartedAt(LocalDateTime.now());
        
        return containerRepository.save(container);
    }

    /**
     * Mark container as failed
     */
    public Container markContainerFailed(Long containerId) {
        log.error("Marking container {} as failed", containerId);
        
        Container container = containerRepository.findById(containerId)
            .orElseThrow(() -> new IllegalArgumentException("Container not found: " + containerId));
        
        // Release resources if device was assigned
        if (container.getDevice() != null) {
            deviceService.releaseResources(
                container.getDevice().getId(),
                container.getAllocatedCpuCores(),
                container.getAllocatedRamBytes(),
                container.getAllocatedDiskBytes()
            );
        }
        
        container.setStatus(Container.ContainerStatus.FAILED);
        
        return containerRepository.save(container);
    }

    /**
     * Stop a running container
     */
    public Container stopContainer(Long containerId) {
        log.info("Stopping container ID: {}", containerId);
        
        Container container = containerRepository.findById(containerId)
            .orElseThrow(() -> new IllegalArgumentException("Container not found: " + containerId));
        
        if (container.getStatus() != Container.ContainerStatus.RUNNING) {
            throw new IllegalStateException("Container is not running: " + containerId);
        }
        
        // Send STOP_CONTAINER message to agent
        if (container.getDevice() != null && container.getContainerId() != null) {
            try {
                sendStopContainerToAgent(container);
            } catch (Exception e) {
                log.error("Failed to send STOP_CONTAINER to agent: {}", e.getMessage());
                // Continue with database update even if WebSocket fails
            }
        }
        
        container.setStatus(Container.ContainerStatus.STOPPING);
        container.setStoppedAt(LocalDateTime.now());
        
        // Don't release resources yet - wait for agent confirmation
        // Resources will be released when CONTAINER_STOPPED message arrives
        
        containerRepository.save(container);
        
        // Fetch with eager loading to avoid lazy initialization exception
        return containerRepository.findByIdWithRelationships(containerId)
            .orElseThrow(() -> new IllegalArgumentException("Container not found after stop: " + containerId));
    }

    /**
     * Restart a stopped container
     */
    public Container restartContainer(Long containerId) {
        log.info("Restarting container ID: {}", containerId);
        
        Container container = containerRepository.findById(containerId)
            .orElseThrow(() -> new IllegalArgumentException("Container not found: " + containerId));
        
        if (container.getStatus() != Container.ContainerStatus.STOPPED) {
            throw new IllegalStateException("Container must be stopped to restart. Current status: " + container.getStatus());
        }
        
        if (container.getDevice() == null) {
            throw new IllegalStateException("Container has no assigned device");
        }
        
        // Send RESTART_CONTAINER message to agent
        log.info("Sending RESTART_CONTAINER message to device {} for container {}", 
            container.getDevice().getId(), containerId);
        
        com.campuscompute.dto.AgentMessage message = 
            com.campuscompute.dto.AgentMessage.restartContainer(
                container.getDevice().getId(),
                container.getId().toString(),
                container.getContainerId()
            );
        
        webSocketSessionManager.getSession(container.getDevice().getId()).ifPresentOrElse(
            session -> {
                try {
                    agentWebSocketHandler.sendMessage(session, message);
                    log.info("RESTART_CONTAINER message sent successfully");
                } catch (Exception e) {
                    log.error("Failed to send RESTART_CONTAINER to agent: {}", e.getMessage());
                }
            },
            () -> log.error("No WebSocket session found for device {}", container.getDevice().getId())
        );
        
        // Reserve resources again
        deviceService.allocateResources(
            container.getDevice().getId(),
            container.getAllocatedCpuCores(),
            container.getAllocatedRamBytes(),
            container.getAllocatedDiskBytes()
        );
        
        // Update status to RESTARTING (will be confirmed by agent)
        container.setStatus(Container.ContainerStatus.RESTARTING);
        container.setStartedAt(LocalDateTime.now());
        container.setStoppedAt(null);
        
        containerRepository.save(container);
        
        // Fetch with eager loading to avoid lazy initialization exception
        return containerRepository.findByIdWithRelationships(containerId)
            .orElseThrow(() -> new IllegalArgumentException("Container not found after restart: " + containerId));
    }

    /**
     * Delete a container
     */
    public void deleteContainer(Long containerId) {
        log.info("Deleting container ID: {}", containerId);
        
        Container container = containerRepository.findById(containerId)
            .orElseThrow(() -> new IllegalArgumentException("Container not found: " + containerId));
        
        // If already DELETED, just remove from database
        if (container.getStatus() == Container.ContainerStatus.DELETED) {
            log.info("Container {} already DELETED, removing from database", containerId);
            containerRepository.delete(container);
            return;
        }
        
        // Send DELETE_CONTAINER message to agent if container is running or stopped
        if (container.getDevice() != null && container.getContainerId() != null &&
            (container.getStatus() == Container.ContainerStatus.RUNNING || 
             container.getStatus() == Container.ContainerStatus.STOPPED)) {
            try {
                sendDeleteContainerToAgent(container);
            } catch (Exception e) {
                log.error("Failed to send DELETE_CONTAINER to agent: {}", e.getMessage());
                // Continue with database update even if WebSocket fails
            }
        }
        
        // Release resources if running
        if (container.getStatus() == Container.ContainerStatus.RUNNING) {
            if (container.getDevice() != null) {
                deviceService.releaseResources(
                    container.getDevice().getId(),
                    container.getAllocatedCpuCores(),
                    container.getAllocatedRamBytes(),
                    container.getAllocatedDiskBytes()
                );
            }
        }
        
        // Mark as DELETED (or remove from database if you prefer)
        container.setStatus(Container.ContainerStatus.DELETED);
        containerRepository.save(container);
        log.info("Container {} marked as DELETED", containerId);
    }

    /**
     * Get container by ID
     */
    public Optional<Container> getContainerById(Long id) {
        return containerRepository.findById(id);
    }

    /**
     * Get container by Docker container ID
     */
    public Optional<Container> getContainerByContainerId(String containerId) {
        return containerRepository.findByContainerId(containerId);
    }

    /**
     * Get all containers for a user
     */
    public List<Container> getContainersByUser(Long userId) {
        return containerRepository.findByUserIdWithDevice(userId);
    }

    /**
     * Get running containers for a user
     */
    public List<Container> getRunningContainersByUser(Long userId) {
        User user = userService.getUserById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        return containerRepository.findByUserAndStatus(user, Container.ContainerStatus.RUNNING);
    }

    /**
     * Get all containers on a device
     */
    public List<Container> getContainersByDevice(Long deviceId) {
        return containerRepository.findByDeviceId(deviceId);
    }

    /**
     * Get running containers on a device
     */
    public List<Container> getRunningContainersByDevice(Long deviceId) {
        Device device = deviceService.getDeviceById(deviceId)
            .orElseThrow(() -> new IllegalArgumentException("Device not found: " + deviceId));
        return containerRepository.findByDeviceAndStatus(device, Container.ContainerStatus.RUNNING);
    }

    /**
     * Get all containers by status
     */
    public List<Container> getContainersByStatus(Container.ContainerStatus status) {
        return containerRepository.findByStatus(status);
    }

    /**
     * Get all pending containers (need scheduling)
     */
    public List<Container> getPendingContainers() {
        return containerRepository.findByStatus(Container.ContainerStatus.PENDING);
    }

    /**
     * Find and handle expired containers
     */
    public List<Container> handleExpiredContainers() {
        LocalDateTime now = LocalDateTime.now();
        List<Container> expired = containerRepository.findExpiredContainers(now);
        
        log.info("Found {} expired containers", expired.size());
        
        for (Container container : expired) {
            log.info("Stopping expired container: {}", container.getId());
            stopContainer(container.getId());
        }
        
        return expired;
    }

    /**
     * Get containers expiring soon (for notifications)
     */
    public List<Container> getContainersExpiringSoon(int minutesThreshold) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusMinutes(minutesThreshold);
        
        return containerRepository.findContainersExpiringSoon(now, threshold);
    }

    /**
     * Get user's current resource usage
     */
    public UserResourceUsage getUserResourceUsage(Long userId) {
        Long cpuCores = containerRepository.getTotalCpuCoresByUser(userId);
        Long ramBytes = containerRepository.getTotalRamBytesByUser(userId);
        long runningContainers = containerRepository.countByUserIdAndStatus(
            userId, Container.ContainerStatus.RUNNING
        );
        
        return new UserResourceUsage(
            cpuCores != null ? cpuCores : 0L,
            ramBytes != null ? ramBytes : 0L,
            runningContainers
        );
    }

    /**
     * Cleanup old stopped/deleted containers
     */
    public void cleanupOldContainers(int daysThreshold) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(daysThreshold);
        containerRepository.deleteOldStoppedContainers(threshold);
        log.info("Cleaned up containers older than {} days", daysThreshold);
    }

    /**
     * User resource usage holder
     */
    public record UserResourceUsage(Long usedCpuCores, Long usedRamBytes, Long runningContainers) {}
}
