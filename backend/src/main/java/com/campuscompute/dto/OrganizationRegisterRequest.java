package com.campuscompute.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for organization registration
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationRegisterRequest {

    @NotBlank(message = "Organization name is required")
    @Size(min = 3, max = 255, message = "Organization name must be between 3 and 255 characters")
    private String name;

    @NotBlank(message = "Organization code is required")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Organization code must contain only uppercase letters, numbers, hyphens, and underscores")
    @Size(min = 2, max = 50, message = "Organization code must be between 2 and 50 characters")
    private String code;

    @Size(max = 100, message = "Domain must not exceed 100 characters")
    private String domain;

    @NotBlank(message = "Contact email is required")
    @Email(message = "Invalid email format")
    private String contactEmail;

    @Size(max = 20, message = "Contact phone must not exceed 20 characters")
    private String contactPhone;

    private String address;

    // Admin user details
    @NotBlank(message = "Admin username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String adminUsername;

    @NotBlank(message = "Admin email is required")
    @Email(message = "Invalid admin email format")
    private String adminEmail;

    @NotBlank(message = "Admin password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String adminPassword;

    @NotBlank(message = "Admin full name is required")
    private String adminFullName;
}
