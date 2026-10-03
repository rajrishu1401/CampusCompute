package com.campuscompute.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Organization entity representing educational institutions (colleges, universities)
 */
@Entity
@Table(name = "organizations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(unique = true, nullable = false, length = 50)
    private String code; // e.g., 'UPES', 'MIT'

    @Column(length = 100)
    private String domain; // e.g., 'upes.ac.in' for email validation

    @Column(nullable = false, length = 255)
    private String contactEmail;

    @Column(length = 20)
    private String contactPhone;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 500)
    private String logoUrl;

    @Column(nullable = false)
    private Boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private SubscriptionTier subscriptionTier = SubscriptionTier.FREE;

    @Column(nullable = false)
    private Integer maxDevices = 10;

    @Column(nullable = false)
    private Integer maxStudents = 100;

    @Column(nullable = false)
    private Integer maxContainersPerStudent = 3;

    @Column(nullable = false)
    private Integer maxCpuCoresPerStudent = 4;

    @Column(nullable = false)
    private Integer maxRamGbPerStudent = 8;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum SubscriptionTier {
        FREE,
        BASIC,
        PREMIUM,
        ENTERPRISE
    }
}
