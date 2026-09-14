package com.campuscompute.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Container entity representing Docker containers
 */
@Entity
@Table(name = "containers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Container {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String containerId; // Docker container ID

    @Column(nullable = false, length = 100)
    private String containerName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContainerStatus status = ContainerStatus.PENDING;

    @Column(nullable = false, length = 100)
    private String image; // Docker image (e.g., ubuntu:24.04)

    // Resource allocation
    @Column(nullable = false)
    private Integer allocatedCpuCores;

    @Column(nullable = false)
    private Long allocatedRamBytes;

    @Column(nullable = false)
    private Long allocatedDiskBytes;

    // Port mappings
    @Column
    private Integer sshPort;

    @Column
    private Integer terminalPort;

    // Lifetime management
    @Column
    private LocalDateTime expiresAt;

    @Column
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime stoppedAt;

    @Column(nullable = false)
    private Boolean persistent = false; // Files persist after stop

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum ContainerStatus {
        PENDING,
        CREATING,
        RUNNING,
        STOPPED,
        FAILED,
        DELETED
    }
}
