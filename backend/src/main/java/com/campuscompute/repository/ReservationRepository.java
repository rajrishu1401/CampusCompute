package com.campuscompute.repository;

import com.campuscompute.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Reservation entity
 * Manages lab schedule and class reservations
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * Find all reservations for a specific lab by labName (legacy)
     */
    List<Reservation> findByLabName(String labName);

    /**
     * Find reservations by lab ID
     */
    List<Reservation> findByLabId(Long labId);

    /**
     * Find upcoming reservations for a lab by ID
     */
    @Query("SELECT r FROM Reservation r WHERE " +
           "r.lab.id = :labId AND " +
           "r.endTime > :now " +
           "ORDER BY r.startTime ASC")
    List<Reservation> findUpcomingReservationsByLab(
        @Param("labId") Long labId,
        @Param("now") LocalDateTime now
    );

    /**
     * Find reservations that overlap with a given time range
     * Critical for adaptive scheduler!
     */
    @Query("SELECT r FROM Reservation r WHERE " +
           "r.lab.id = :labId AND " +
           "((r.startTime <= :endTime AND r.endTime >= :startTime))")
    List<Reservation> findOverlappingReservations(
        @Param("labId") Long labId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * Find overlapping reservations excluding a specific reservation
     */
    @Query("SELECT r FROM Reservation r WHERE r.lab.id = :labId " +
           "AND r.id != :excludeId " +
           "AND ((r.startTime <= :endTime AND r.endTime >= :startTime))")
    List<Reservation> findOverlappingReservationsExcluding(
        @Param("labId") Long labId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("excludeId") Long excludeId
    );

    /**
     * Find next reservation after a given time
     * Used by scheduler to calculate reservation proximity
     */
    @Query("SELECT r FROM Reservation r WHERE " +
           "r.lab.id = :labId AND " +
           "r.startTime > :afterTime " +
           "ORDER BY r.startTime ASC " +
           "LIMIT 1")
    java.util.Optional<Reservation> findNextReservation(
        @Param("labId") Long labId,
        @Param("afterTime") LocalDateTime afterTime
    );

    /**
     * Find active reservations (currently happening)
     */
    @Query("SELECT r FROM Reservation r WHERE :now BETWEEN r.startTime AND r.endTime")
    List<Reservation> findActiveReservations(@Param("now") LocalDateTime now);

    /**
     * Find reservations between start and end times
     */
    @Query("SELECT r FROM Reservation r WHERE " +
           "(r.startTime >= :start AND r.startTime <= :end) " +
           "OR (r.endTime >= :start AND r.endTime <= :end)")
    List<Reservation> findReservationsBetween(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    /**
     * Check if lab has any reservation in time range
     */
    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE " +
           "r.labName = :labName AND " +
           "((r.startTime <= :endTime AND r.endTime >= :startTime))")
    boolean hasReservationInRange(
        @Param("labName") String labName,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * Find recurring reservations by day of week
     */
    List<Reservation> findByRecurringTrueAndDayOfWeek(Integer dayOfWeek);

    /**
     * Delete old non-recurring reservations (cleanup)
     */
    @Query("DELETE FROM Reservation r WHERE " +
           "r.recurring = false AND " +
           "r.endTime < :threshold")
    void deleteOldReservations(@Param("threshold") LocalDateTime threshold);
}
