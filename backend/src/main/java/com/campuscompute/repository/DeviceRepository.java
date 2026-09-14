package com.campuscompute.repository;

import com.campuscompute.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Device entity
 * Provides CRUD operations and custom queries for lab computers
 */
@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    /**
     * Find device by unique device ID
     */
    Optional<Device> findByDeviceId(String deviceId);

    /**
     * Find device by hostname
     */
    Optional<Device> findByHostname(String hostname);

    /**
     * Check if device ID exists
     */
    boolean existsByDeviceId(String deviceId);

    /**
     * Find all devices by status
     */
    List<Device> findByStatus(Device.DeviceStatus status);

    /**
     * Find all online devices
     */
    List<Device> findByStatusAndEnabledTrue(Device.DeviceStatus status);

    /**
     * Find devices by lab name
     */
    List<Device> findByLabName(String labName);

    /**
     * Find enabled devices in a specific lab
     */
    List<Device> findByLabNameAndEnabledTrue(String labName);

    /**
     * Find online devices in a specific lab
     */
    List<Device> findByLabNameAndStatusAndEnabledTrue(
        String labName, 
        Device.DeviceStatus status
    );

    /**
     * Find devices that haven't sent heartbeat recently (considered offline)
     */
    @Query("SELECT d FROM Device d WHERE d.lastHeartbeat < :threshold")
    List<Device> findStaleDevices(@Param("threshold") LocalDateTime threshold);

    /**
     * Find available devices with sufficient resources
     * Used by scheduler to find suitable devices
     */
    @Query("SELECT d FROM Device d WHERE " +
           "d.status = 'ONLINE' AND " +
           "d.enabled = true AND " +
           "(d.totalCpuCores - d.usedCpuCores) >= :cpuCores AND " +
           "(d.totalRamBytes - d.usedRamBytes) >= :ramBytes AND " +
           "d.cpuLoadPercent < :maxCpuLoad AND " +
           "d.ramLoadPercent < :maxRamLoad")
    List<Device> findAvailableDevices(
        @Param("cpuCores") Integer cpuCores,
        @Param("ramBytes") Long ramBytes,
        @Param("maxCpuLoad") Double maxCpuLoad,
        @Param("maxRamLoad") Double maxRamLoad
    );

    /**
     * Find devices ordered by reliability score (for scheduler)
     */
    @Query("SELECT d FROM Device d WHERE " +
           "d.status = 'ONLINE' AND d.enabled = true " +
           "ORDER BY d.reliabilityScore DESC, d.cpuLoadPercent ASC")
    List<Device> findDevicesOrderedByReliability();

    /**
     * Count online devices by lab
     */
    long countByLabNameAndStatus(String labName, Device.DeviceStatus status);

    /**
     * Get total available CPU cores across all online devices
     */
    @Query("SELECT SUM(d.totalCpuCores - d.usedCpuCores) FROM Device d WHERE " +
           "d.status = 'ONLINE' AND d.enabled = true")
    Long getTotalAvailableCpuCores();

    /**
     * Get total available RAM across all online devices
     */
    @Query("SELECT SUM(d.totalRamBytes - d.usedRamBytes) FROM Device d WHERE " +
           "d.status = 'ONLINE' AND d.enabled = true")
    Long getTotalAvailableRamBytes();
}
