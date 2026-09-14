package com.campuscompute.service;

import com.campuscompute.entity.Lab;
import com.campuscompute.exception.ResourceNotFoundException;
import com.campuscompute.repository.LabRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing computer labs
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LabService {

    private final LabRepository labRepository;

    /**
     * Create a new lab
     */
    @Transactional
    public Lab createLab(Lab lab) {
        // Check if lab with same name exists
        if (labRepository.findByName(lab.getName()).isPresent()) {
            throw new IllegalArgumentException("Lab with name '" + lab.getName() + "' already exists");
        }

        Lab createdLab = labRepository.save(lab);
        log.info("Created new lab: {} (ID: {})", createdLab.getName(), createdLab.getId());
        return createdLab;
    }

    /**
     * Get lab by ID
     */
    public Lab getLabById(Long id) {
        return labRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lab", "id", id));
    }

    /**
     * Get all labs
     */
    public List<Lab> getAllLabs() {
        return labRepository.findAll();
    }

    /**
     * Get all active labs
     */
    public List<Lab> getActiveLabs() {
        return labRepository.findByIsActiveTrue();
    }

    /**
     * Get labs by department
     */
    public List<Lab> getLabsByDepartment(String department) {
        return labRepository.findByDepartment(department);
    }

    /**
     * Update lab
     */
    @Transactional
    public Lab updateLab(Long id, Lab labUpdate) {
        Lab lab = getLabById(id);

        if (labUpdate.getName() != null) {
            lab.setName(labUpdate.getName());
        }
        if (labUpdate.getBuilding() != null) {
            lab.setBuilding(labUpdate.getBuilding());
        }
        if (labUpdate.getRoom() != null) {
            lab.setRoom(labUpdate.getRoom());
        }
        if (labUpdate.getDepartment() != null) {
            lab.setDepartment(labUpdate.getDepartment());
        }
        if (labUpdate.getDescription() != null) {
            lab.setDescription(labUpdate.getDescription());
        }
        if (labUpdate.getCapacity() != null) {
            lab.setCapacity(labUpdate.getCapacity());
        }
        if (labUpdate.getIsActive() != null) {
            lab.setIsActive(labUpdate.getIsActive());
        }

        Lab updated = labRepository.save(lab);
        log.info("Updated lab: {} (ID: {})", updated.getName(), updated.getId());
        return updated;
    }

    /**
     * Delete lab
     */
    @Transactional
    public void deleteLab(Long id) {
        Lab lab = getLabById(id);
        
        // Check if lab has active devices
        long activeDevices = labRepository.countActiveDevices(id);
        if (activeDevices > 0) {
            throw new IllegalStateException(
                "Cannot delete lab with active devices. Please disable or remove devices first.");
        }

        labRepository.delete(lab);
        log.info("Deleted lab: {} (ID: {})", lab.getName(), id);
    }

    /**
     * Disable lab
     */
    @Transactional
    public Lab disableLab(Long id) {
        Lab lab = getLabById(id);
        lab.setIsActive(false);
        Lab updated = labRepository.save(lab);
        log.info("Disabled lab: {} (ID: {})", lab.getName(), id);
        return updated;
    }

    /**
     * Enable lab
     */
    @Transactional
    public Lab enableLab(Long id) {
        Lab lab = getLabById(id);
        lab.setIsActive(true);
        Lab updated = labRepository.save(lab);
        log.info("Enabled lab: {} (ID: {})", lab.getName(), id);
        return updated;
    }

    /**
     * Get active device count for lab
     */
    public long getActiveDeviceCount(Long labId) {
        return labRepository.countActiveDevices(labId);
    }

    /**
     * Find labs with available resources
     */
    public List<Lab> findLabsWithAvailableResources(int cpuCores, long ramBytes) {
        return labRepository.findLabsWithAvailableResources(cpuCores, ramBytes);
    }
}
