package com.campuscompute.exception;

/**
 * Exception thrown when user quota is exceeded
 */
public class QuotaExceededException extends RuntimeException {
    
    private String quotaType;
    private long requested;
    private long available;
    
    public QuotaExceededException(String quotaType, long requested, long available) {
        super(String.format("Quota exceeded for %s: requested %d, available %d", 
            quotaType, requested, available));
        this.quotaType = quotaType;
        this.requested = requested;
        this.available = available;
    }
    
    public QuotaExceededException(String message) {
        super(message);
    }
    
    public String getQuotaType() {
        return quotaType;
    }
    
    public long getRequested() {
        return requested;
    }
    
    public long getAvailable() {
        return available;
    }
}
