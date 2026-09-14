package com.campuscompute.repository;

import com.campuscompute.entity.Lab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Lab entity
 */
@Repository
public interface LabRepository extends JpaRepository<Lab, Long> {

    /**
     * Find lab by name
     */
    Optional<Lab> findByName(String name);

    /**
     * Find all active labs
     */
    List<Lab> findByIsActiveTrue();

    /**
     * Find labs by department
     */
    List<Lab> findByDepartment(String department);

    /**
     * Find labs by building
     */
    List<Lab> findByBuilding(String building);

    /**
     * Count active devices in a lab
     */
    @Query("SELECT COUNT(d) FROM Device d WHERE d.lab.id = :labId AND d.status = 'ONLINE'")
    long countActiveDevices(Long labId);

    /**
     * Find labs with available devices
     */
    @Query("SELECT DISTINCT l FROM Lab l " +
           "JOIN l.devices d " +
           "WHERE l.isActive = true " +
           "AND d.status = 'ONLINE' " +
           "AND d.enabled = true " +
           "AND (d.totalCpuCores - d.usedCpuCores) >= :cpuCores " +
           "AND (d.totalRamBytes - d.usedRamBytes) >= :ramBytes")
    List<Lab> findLabsWithAvailableResources(int cpuCores, long ramBytes);
}
