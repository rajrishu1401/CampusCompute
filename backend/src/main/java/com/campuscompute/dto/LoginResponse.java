package com.campuscompute.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO for login response
 */
@Data
@AllArgsConstructor
public class LoginResponse {
    
    private String token;
    private String type = "Bearer";
    private Long userId;
    private String username;
    private String email;
    private String role;
    
    public LoginResponse(String token, Long userId, String username, String email, String role) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
    }
}
