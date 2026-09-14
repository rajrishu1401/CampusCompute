package com.campuscompute.repository;

import com.campuscompute.entity.Container;
import com.campuscompute.entity.Device;
import com.campuscompute.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Container entity
 * Provides CRUD operations and custom queries for Docker containers
 */
@Repository
public interface ContainerRepository extends JpaRepository<Container, Long> {

    /**
     * Find container by Docker container ID
     */
    Optional<Container> findByContainerId(String containerId);

    /**
     * Find container by name
     */
    Optional<Container> findByContainerName(String containerName);

    /**
     * Check if container ID exists
     */
    boolean existsByContainerId(String containerId);

    /**
     * Find all containers for a user
     */
    List<Container> findByUser(User user);

    /**
     * Find all containers by user ID
     */
    List<Container> findByUserId(Long userId);

    /**
     * Find running containers for a user
     */
    List<Container> findByUserAndStatus(User user, Container.ContainerStatus status);

    /**
     * Find all containers on a device
     */
    List<Container> findByDevice(Device device);

    /**
     * Find all containers by device ID
     */
    List<Container> findByDeviceId(Long deviceId);

    /**
     * Find running containers on a device
     */
    List<Container> findByDeviceAndStatus(Device device, Container.ContainerStatus status);

    /**
     * Find containers by status
     */
    List<Container> findByStatus(Container.ContainerStatus status);

    /**
     * Count running containers for a user
     */
    long countByUserAndStatus(User user, Container.ContainerStatus status);

    /**
     * Count containers by user ID and status
     */
    long countByUserIdAndStatus(Long userId, Container.ContainerStatus status);

    /**
     * Count running containers on a device
     */
    long countByDeviceIdAndStatus(Long deviceId, Container.ContainerStatus status);

    /**
     * Find expired containers (past expiration time)
     */
    @Query("SELECT c FROM Container c WHERE " +
           "c.expiresAt < :now AND " +
           "c.status IN ('RUNNING', 'STOPPED')")
    List<Container> findExpiredContainers(@Param("now") LocalDateTime now);

    /**
     * Find containers expiring soon (for notifications)
     */
    @Query("SELECT c FROM Container c WHERE " +
           "c.expiresAt BETWEEN :now AND :threshold AND " +
           "c.status = 'RUNNING'")
    List<Container> findContainersExpiringSoon(
        @Param("now") LocalDateTime now,
        @Param("threshold") LocalDateTime threshold
    );

    /**
     * Get total CPU cores allocated to user
     */
    @Query("SELECT SUM(c.allocatedCpuCores) FROM Container c WHERE " +
           "c.user.id = :userId AND c.status = 'RUNNING'")
    Long getTotalCpuCoresByUser(@Param("userId") Long userId);

    /**
     * Get total RAM allocated to user
     */
    @Query("SELECT SUM(c.allocatedRamBytes) FROM Container c WHERE " +
           "c.user.id = :userId AND c.status = 'RUNNING'")
    Long getTotalRamBytesByUser(@Param("userId") Long userId);

    /**
     * Get total CPU cores allocated on device
     */
    @Query("SELECT SUM(c.allocatedCpuCores) FROM Container c WHERE " +
           "c.device.id = :deviceId AND c.status = 'RUNNING'")
    Long getTotalCpuCoresByDevice(@Param("deviceId") Long deviceId);

    /**
     * Get total RAM allocated on device
     */
    @Query("SELECT SUM(c.allocatedRamBytes) FROM Container c WHERE " +
           "c.device.id = :deviceId AND c.status = 'RUNNING'")
    Long getTotalRamBytesByDevice(@Param("deviceId") Long deviceId);

    /**
     * Find recent containers (for activity tracking)
     */
    @Query("SELECT c FROM Container c WHERE " +
           "c.createdAt > :since " +
           "ORDER BY c.createdAt DESC")
    List<Container> findRecentContainers(@Param("since") LocalDateTime since);

    /**
     * Get container statistics by user
     */
    @Query("SELECT c.status, COUNT(c) FROM Container c WHERE " +
           "c.user.id = :userId " +
           "GROUP BY c.status")
    List<Object[]> getContainerStatsByUser(@Param("userId") Long userId);

    /**
     * Find long-running containers (running > threshold)
     */
    @Query("SELECT c FROM Container c WHERE " +
           "c.status = 'RUNNING' AND " +
           "c.startedAt < :threshold")
    List<Container> findLongRunningContainers(@Param("threshold") LocalDateTime threshold);

    /**
     * Delete old stopped containers (cleanup)
     */
    @Query("DELETE FROM Container c WHERE " +
           "c.status IN ('STOPPED', 'DELETED', 'FAILED') AND " +
           "c.updatedAt < :threshold")
    void deleteOldStoppedContainers(@Param("threshold") LocalDateTime threshold);
}
