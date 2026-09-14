package com.campuscompute.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * User entity representing students, faculty, and admins
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(length = 100)
    private String fullName;

    @Column(length = 20)
    private String sapId;

    @Column(length = 50)
    private String department;

    @Column(nullable = false)
    private Boolean active = true;

    // Resource quotas
    @Column(nullable = false)
    private Integer maxCpuCores = 4;

    @Column(nullable = false)
    private Integer maxRamGb = 8;

    @Column(nullable = false)
    private Integer maxContainers = 3;

    @Column(nullable = false)
    private Long maxContainerLifetimeMs = 14400000L; // 4 hours default

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum UserRole {
        STUDENT,
        FACULTY,
        ADMIN
    }
}
