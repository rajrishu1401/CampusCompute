package com.campuscompute.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for device enrollment token
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentTokenResponse {

    private String token;
    
    private Long organizationId;
    
    private String organizationCode;
    
    private LocalDateTime expiresAt;
    
    private String installScript;
    
    private String backendUrl;
}
