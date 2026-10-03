package com.campuscompute.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for student first-time login password setup
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FirstTimeLoginRequest {

    @NotBlank(message = "Student ID is required")
    private String studentId;

    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Organization code is required")
    private String organizationCode;

    @NotBlank(message = "New password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String newPassword;
}
