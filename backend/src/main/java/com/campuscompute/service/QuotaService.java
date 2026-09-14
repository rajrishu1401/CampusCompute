package com.campuscompute.service;

import com.campuscompute.entity.User;
import com.campuscompute.exception.QuotaExceededException;
import com.campuscompute.repository.ContainerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for managing and enforcing user quotas
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QuotaService {

    private final ContainerRepository containerRepository;

    /**
     * Check if user can create a new container with specified resources
     * Throws QuotaExceededException if quota would be exceeded
     */
    public void checkQuota(User user, int requestedCpuCores, long requestedRamBytes) {
        // Check max containers
        long currentContainerCount = containerRepository.countByUserIdAndStatus(
            user.getId(), 
            com.campuscompute.entity.Container.ContainerStatus.RUNNING
        );
        if (currentContainerCount >= user.getMaxContainers()) {
            throw new QuotaExceededException(
                "containers",
                currentContainerCount + 1,
                user.getMaxContainers()
            );
        }

        // Check CPU cores
        Long currentCpuUsage = containerRepository.getTotalCpuCoresByUser(user.getId());
        int totalCpuUsage = (currentCpuUsage != null ? currentCpuUsage.intValue() : 0) + requestedCpuCores;
        if (totalCpuUsage > user.getMaxCpuCores()) {
            throw new QuotaExceededException(
                "CPU cores",
                totalCpuUsage,
                user.getMaxCpuCores()
            );
        }

        // Check RAM
        Long currentRamUsage = containerRepository.getTotalRamBytesByUser(user.getId());
        long totalRamUsage = (currentRamUsage != null ? currentRamUsage : 0) + requestedRamBytes;
        long maxRamBytes = user.getMaxRamGb() * 1024L * 1024L * 1024L;
        if (totalRamUsage > maxRamBytes) {
            throw new QuotaExceededException(
                "RAM",
                totalRamUsage / (1024 * 1024 * 1024),
                user.getMaxRamGb()
            );
        }

        log.debug("Quota check passed for user {} - Containers: {}/{}, CPU: {}/{}, RAM: {}GB/{}GB",
            user.getUsername(),
            currentContainerCount + 1, user.getMaxContainers(),
            totalCpuUsage, user.getMaxCpuCores(),
            totalRamUsage / (1024 * 1024 * 1024), user.getMaxRamGb()
        );
    }

    /**
     * Get remaining quota for user
     */
    public QuotaInfo getRemainingQuota(User user) {
        Long currentCpuUsage = containerRepository.getTotalCpuCoresByUser(user.getId());
        Long currentRamUsage = containerRepository.getTotalRamBytesByUser(user.getId());
        long currentContainerCount = containerRepository.countByUserIdAndStatus(
            user.getId(), 
            com.campuscompute.entity.Container.ContainerStatus.RUNNING
        );

        int usedCpu = currentCpuUsage != null ? currentCpuUsage.intValue() : 0;
        long usedRamGb = currentRamUsage != null ? currentRamUsage / (1024 * 1024 * 1024) : 0;

        return new QuotaInfo(
            user.getMaxContainers() - currentContainerCount,
            user.getMaxCpuCores() - usedCpu,
            user.getMaxRamGb() - usedRamGb,
            currentContainerCount,
            usedCpu,
            usedRamGb
        );
    }

    /**
     * DTO for quota information
     */
    public record QuotaInfo(
        long remainingContainers,
        int remainingCpuCores,
        long remainingRamGb,
        long usedContainers,
        int usedCpuCores,
        long usedRamGb
    ) {}
}
