package com.campuscompute.scheduler;

import com.campuscompute.dto.ContainerRequest;
import com.campuscompute.entity.Device;
import com.campuscompute.entity.Reservation;
import com.campuscompute.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Adaptive Scheduler with Reservation-Aware Scoring
 * 
 * This is the RESEARCH CONTRIBUTION of the project!
 * 
 * Scoring Factors:
 * 1. CPU Availability (30%) - Favor devices with more free CPU
 * 2. RAM Availability (30%) - Favor devices with more free RAM
 * 3. Load Balance (20%) - Favor devices with fewer containers
 * 4. Reservation Proximity (15%) - NOVEL: Avoid devices with upcoming reservations
 * 5. Device Reliability (5%) - Favor devices with high uptime
 * 
 * The reservation proximity factor prevents scheduling containers on PCs
 * that have classes starting soon, reducing forced migrations and improving
 * user experience.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdaptiveSchedulerStrategy implements SchedulingStrategy {
    
    private final ReservationService reservationService;
    
    // Configurable weights from application.yml
    @Value("${scheduler.weights.cpu:0.3}")
    private double cpuWeight;
    
    @Value("${scheduler.weights.ram:0.3}")
    private double ramWeight;
    
    @Value("${scheduler.weights.load:0.2}")
    private double loadWeight;
    
    @Value("${scheduler.weights.reservation-proximity:0.15}")
    private double reservationWeight;
    
    @Value("${scheduler.weights.reliability:0.05}")
    private double reliabilityWeight;
    
    @Override
    public Device selectDevice(List<Device> availableDevices, ContainerRequest request) {
        if (availableDevices == null || availableDevices.isEmpty()) {
            log.warn("No available devices to schedule");
            return null;
        }
        
        // Filter devices with sufficient resources
        List<Device> suitableDevices = availableDevices.stream()
            .filter(device -> hasEnoughResources(device, request))
            .toList();
        
        if (suitableDevices.isEmpty()) {
            log.warn("No devices with sufficient resources found");
            return null;
        }
        
        // Calculate scores and select best device
        Device bestDevice = suitableDevices.stream()
            .max(Comparator.comparingDouble(device -> calculateDeviceScore(device, request)))
            .orElse(null);
        
        if (bestDevice != null) {
            double score = calculateDeviceScore(bestDevice, request);
            log.info("Selected device {} with score {:.3f} (Adaptive Strategy)", 
                bestDevice.getId(), score);
        }
        
        return bestDevice;
    }
    
    @Override
    public double calculateDeviceScore(Device device, ContainerRequest request) {
        // Calculate individual scores
        double cpuScore = calculateCpuScore(device, request);
        double ramScore = calculateRamScore(device, request);
        double loadScore = calculateLoadScore(device);
        double reservationScore = calculateReservationProximityScore(device, request);
        double reliabilityScore = calculateReliabilityScore(device);
        
        // Weighted sum
        double totalScore = 
            (cpuScore * cpuWeight) +
            (ramScore * ramWeight) +
            (loadScore * loadWeight) +
            (reservationScore * reservationWeight) +
            (reliabilityScore * reliabilityWeight);
        
        log.debug("Device {} scores: CPU={:.2f}, RAM={:.2f}, Load={:.2f}, Reservation={:.2f}, Reliability={:.2f}, Total={:.3f}",
            device.getId(), cpuScore, ramScore, loadScore, reservationScore, reliabilityScore, totalScore);
        
        return totalScore;
    }
    
    @Override
    public String getStrategyName() {
        return "AdaptiveReservationAware";
    }
    
    /**
     * CPU availability score (0.0 to 1.0)
     * Higher score = more free CPU
     */
    private double calculateCpuScore(Device device, ContainerRequest request) {
        long availableCpu = device.getTotalCpuCores() - device.getUsedCpuCores();
        long totalCpu = device.getTotalCpuCores();
        
        if (totalCpu == 0) {
            return 0.0;
        }
        
        // Percentage of CPU available
        double availablePercent = (double) availableCpu / totalCpu;
        
        // Bonus if this device has way more CPU than needed (good for future growth)
        double requestPercent = (double) request.getCpuCores() / totalCpu;
        double excessBonus = Math.max(0, availablePercent - requestPercent);
        
        return Math.min(1.0, availablePercent + (excessBonus * 0.2));
    }
    
    /**
     * RAM availability score (0.0 to 1.0)
     * Higher score = more free RAM
     */
    private double calculateRamScore(Device device, ContainerRequest request) {
        long availableRam = device.getTotalRamBytes() - device.getUsedRamBytes();
        long totalRam = device.getTotalRamBytes();
        
        if (totalRam == 0) {
            return 0.0;
        }
        
        // Percentage of RAM available
        double availablePercent = (double) availableRam / totalRam;
        
        // Bonus if this device has way more RAM than needed
        double requestPercent = (double) request.getRamBytes() / totalRam;
        double excessBonus = Math.max(0, availablePercent - requestPercent);
        
        return Math.min(1.0, availablePercent + (excessBonus * 0.2));
    }
    
    /**
     * Load balance score (0.0 to 1.0)
     * Higher score = fewer containers on device
     * Helps distribute load evenly
     */
    private double calculateLoadScore(Device device) {
        // Use CPU load as proxy for container load
        double cpuLoad = device.getCpuLoadPercent() != null ? device.getCpuLoadPercent() : 0.0;
        
        // Invert: lower CPU load = higher score
        // Score = 1.0 when CPU is 0%, 0.0 when CPU is 100%
        return Math.max(0.0, 1.0 - (cpuLoad / 100.0));
    }
    
    /**
     * Reservation proximity score (0.0 to 1.0)
     * 
     * THIS IS THE NOVEL CONTRIBUTION!
     * 
     * Penalizes devices with upcoming reservations.
     * If a lab PC has a class starting in 30 minutes, avoid scheduling there.
     * 
     * Score calculation:
     * - No reservation: 1.0 (best)
     * - Reservation in 4+ hours: 0.9
     * - Reservation in 2-4 hours: 0.6
     * - Reservation in 1-2 hours: 0.3
     * - Reservation in < 1 hour: 0.1 (worst)
     */
    private double calculateReservationProximityScore(Device device, ContainerRequest request) {
        // Skip if device has no lab association
        if (device.getLab() == null) {
            return 1.0;  // Non-lab devices have no reservations
        }
        
        // Get next reservation for this device's lab
        Reservation nextReservation = reservationService.getNextReservation(
            device.getLab().getId(), 
            LocalDateTime.now()
        );
        
        if (nextReservation == null) {
            return 1.0;  // No upcoming reservation, perfect score
        }
        
        Reservation reservation = nextReservation;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reservationStart = reservation.getStartTime();
        
        // Calculate time until reservation starts
        Duration timeUntilReservation = Duration.between(now, reservationStart);
        long minutesUntilReservation = timeUntilReservation.toMinutes();
        
        // If reservation is in the past or already started, score is 0
        if (minutesUntilReservation <= 0) {
            return 0.0;
        }
        
        // Score based on time until reservation
        // Using exponential decay: more time = higher score
        if (minutesUntilReservation >= 240) {  // 4+ hours
            return 0.9;
        } else if (minutesUntilReservation >= 120) {  // 2-4 hours
            return 0.6;
        } else if (minutesUntilReservation >= 60) {  // 1-2 hours
            return 0.3;
        } else {  // < 1 hour
            return 0.1;
        }
    }
    
    /**
     * Device reliability score (0.0 to 1.0)
     * Based on device uptime and failure history
     * Higher score = more reliable device
     */
    private double calculateReliabilityScore(Device device) {
        // Simple reliability based on status
        // In production, track failure rates and uptime percentage
        
        return switch (device.getStatus()) {
            case ONLINE -> 1.0;
            case OFFLINE -> 0.0;
            case MAINTENANCE -> 0.0;
            case BUSY -> 0.5;
            default -> 0.5;
        };
    }
    
    /**
     * Check if device has enough resources
     */
    private boolean hasEnoughResources(Device device, ContainerRequest request) {
        long availableCpu = device.getTotalCpuCores() - device.getUsedCpuCores();
        long availableRam = device.getTotalRamBytes() - device.getUsedRamBytes();
        long availableDisk = device.getTotalDiskBytes() - device.getUsedDiskBytes();
        
        return availableCpu >= request.getCpuCores() &&
               availableRam >= request.getRamBytes() &&
               availableDisk >= request.getDiskBytes();
    }
}
