package com.campuscompute.exception;

/**
 * Exception thrown when reservation time slot is invalid or conflicts
 */
public class InvalidReservationException extends RuntimeException {
    
    public InvalidReservationException(String message) {
        super(message);
    }
}
