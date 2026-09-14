package com.campuscompute.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Device entity representing lab computers (agents)
 */
@Entity
@Table(name = "devices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String deviceId; // Unique identifier (hostname or UUID)

    @Column(nullable = false, length = 100)
    private String hostname;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_id")
    private Lab lab;

    @Column(nullable = false, length = 50)
    private String labName; // Denormalized for quick access

    @Column(length = 20)
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeviceStatus status = DeviceStatus.OFFLINE;

    // Hardware specifications
    @Column(nullable = false)
    private Integer totalCpuCores;

    @Column(nullable = false)
    private Long totalRamBytes;

    @Column(nullable = false)
    private Long totalDiskBytes;

    // Current resource usage
    @Column(nullable = false)
    private Integer usedCpuCores = 0;

    @Column(nullable = false)
    private Long usedRamBytes = 0L;

    @Column(nullable = false)
    private Long usedDiskBytes = 0L;

    // Load metrics (0.0 to 100.0)
    @Column(nullable = false)
    private Double cpuLoadPercent = 0.0;

    @Column(nullable = false)
    private Double ramLoadPercent = 0.0;

    // Reliability score (0.0 to 1.0)
    @Column(nullable = false)
    private Double reliabilityScore = 1.0;

    // Connection info
    @Column
    private LocalDateTime lastHeartbeat;

    @Column
    private String agentVersion;

    @Column
    private String dockerVersion;

    @Column(nullable = false)
    private Boolean enabled = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum DeviceStatus {
        ONLINE,
        OFFLINE,
        BUSY,
        MAINTENANCE
    }
}
