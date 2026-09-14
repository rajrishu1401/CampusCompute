package com.campuscompute.service;

import com.campuscompute.entity.Lab;
import com.campuscompute.entity.Reservation;
import com.campuscompute.exception.InvalidReservationException;
import com.campuscompute.exception.ResourceNotFoundException;
import com.campuscompute.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing lab reservations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final LabService labService;

    /**
     * Create a new reservation
     */
    @Transactional
    public Reservation createReservation(Reservation reservation) {
        // Validate reservation times
        if (reservation.getStartTime().isAfter(reservation.getEndTime())) {
            throw new InvalidReservationException("Start time must be before end time");
        }

        if (reservation.getStartTime().isBefore(LocalDateTime.now())) {
            throw new InvalidReservationException("Cannot create reservation in the past");
        }

        // Validate lab exists
        Lab lab = labService.getLabById(reservation.getLab().getId());
        reservation.setLab(lab);
        reservation.setLabName(lab.getName());

        // Check for overlapping reservations
        List<Reservation> overlapping = reservationRepository.findOverlappingReservations(
            lab.getId(),
            reservation.getStartTime(),
            reservation.getEndTime()
        );

        if (!overlapping.isEmpty()) {
            throw new InvalidReservationException(
                "Time slot conflicts with existing reservation(s) in " + lab.getName()
            );
        }

        Reservation created = reservationRepository.save(reservation);
        log.info("Created reservation for lab {} from {} to {}",
            lab.getName(), reservation.getStartTime(), reservation.getEndTime());
        return created;
    }

    /**
     * Get reservation by ID
     */
    public Reservation getReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", id));
    }

    /**
     * Get all reservations
     */
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    /**
     * Get reservations for a lab
     */
    public List<Reservation> getReservationsForLab(Long labId) {
        return reservationRepository.findByLabId(labId);
    }

    /**
     * Get upcoming reservations for a lab
     */
    public List<Reservation> getUpcomingReservationsForLab(Long labId) {
        return reservationRepository.findUpcomingReservationsByLab(labId, LocalDateTime.now());
    }

    /**
     * Get active reservations (happening now)
     */
    public List<Reservation> getActiveReservations() {
        LocalDateTime now = LocalDateTime.now();
        return reservationRepository.findActiveReservations(now);
    }

    /**
     * Get next reservation for a lab after a specific time
     */
    public Reservation getNextReservation(Long labId, LocalDateTime after) {
        return reservationRepository.findNextReservation(labId, after)
                .orElse(null);
    }

    /**
     * Check if lab is available during time period
     */
    public boolean isLabAvailable(Long labId, LocalDateTime start, LocalDateTime end) {
        List<Reservation> overlapping = reservationRepository.findOverlappingReservations(
            labId, start, end
        );
        return overlapping.isEmpty();
    }

    /**
     * Update reservation
     */
    @Transactional
    public Reservation updateReservation(Long id, Reservation reservationUpdate) {
        Reservation reservation = getReservationById(id);

        // Validate times if changed
        LocalDateTime newStart = reservationUpdate.getStartTime() != null ?
            reservationUpdate.getStartTime() : reservation.getStartTime();
        LocalDateTime newEnd = reservationUpdate.getEndTime() != null ?
            reservationUpdate.getEndTime() : reservation.getEndTime();

        if (newStart.isAfter(newEnd)) {
            throw new InvalidReservationException("Start time must be before end time");
        }

        // Check for overlapping reservations (excluding current reservation)
        List<Reservation> overlapping = reservationRepository
            .findOverlappingReservationsExcluding(
                reservation.getLab().getId(),
                newStart,
                newEnd,
                id
            );

        if (!overlapping.isEmpty()) {
            throw new InvalidReservationException("Time slot conflicts with existing reservation(s)");
        }

        // Update fields
        if (reservationUpdate.getStartTime() != null) {
            reservation.setStartTime(reservationUpdate.getStartTime());
        }
        if (reservationUpdate.getEndTime() != null) {
            reservation.setEndTime(reservationUpdate.getEndTime());
        }
        if (reservationUpdate.getDescription() != null) {
            reservation.setDescription(reservationUpdate.getDescription());
        }
        if (reservationUpdate.getRecurring() != null) {
            reservation.setRecurring(reservationUpdate.getRecurring());
        }
        if (reservationUpdate.getDayOfWeek() != null) {
            reservation.setDayOfWeek(reservationUpdate.getDayOfWeek());
        }

        Reservation updated = reservationRepository.save(reservation);
        log.info("Updated reservation ID: {}", id);
        return updated;
    }

    /**
     * Delete reservation
     */
    @Transactional
    public void deleteReservation(Long id) {
        Reservation reservation = getReservationById(id);
        reservationRepository.delete(reservation);
        log.info("Deleted reservation ID: {} for lab {}", id, reservation.getLabName());
    }

    /**
     * Get reservations within a time range
     */
    public List<Reservation> getReservationsBetween(LocalDateTime start, LocalDateTime end) {
        return reservationRepository.findReservationsBetween(start, end);
    }
}
