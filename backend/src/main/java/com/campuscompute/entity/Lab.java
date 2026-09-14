package com.campuscompute.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Lab entity representing a computer lab on campus
 */
@Entity
@Table(name = "labs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lab {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 100)
    private String building;

    @Column(length = 50)
    private String room;

    @Column(length = 100)
    private String department;

    @Column(length = 500)
    private String description;

    @Column(name = "capacity")
    private Integer capacity; // Number of PCs in lab

    @Column(name = "is_active")
    private Boolean isActive = true;

    @OneToMany(mappedBy = "lab", cascade = CascadeType.ALL)
    private List<Device> devices;

    @OneToMany(mappedBy = "lab", cascade = CascadeType.ALL)
    private List<Reservation> reservations;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
