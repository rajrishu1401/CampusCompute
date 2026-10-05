package com.campuscompute.controller;

import com.campuscompute.dto.ApiResponse;
import com.campuscompute.dto.FirstTimeLoginRequest;
import com.campuscompute.dto.LoginRequest;
import com.campuscompute.dto.LoginResponse;
import com.campuscompute.dto.RegisterRequest;
import com.campuscompute.entity.User;
import com.campuscompute.security.JwtUtil;
import com.campuscompute.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST controller for authentication endpoints
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * POST /api/auth/register
     * Register a new user
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request for username: {}", request.getUsername());
        
        // NOTE: This endpoint is for backward compatibility only
        // For multi-org platform, use /api/organizations/register for org admins
        // and bulk student upload for students
        
        try {
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPasswordHash(request.getPassword()); // Will be hashed in service
            user.setFullName(request.getFullName());
            user.setSapId(request.getSapId());
            user.setDepartment(request.getDepartment());
            user.setRole(User.UserRole.valueOf(request.getRole().toUpperCase()));
            user.setUserType(User.UserType.STUDENT); // Default to STUDENT
            user.setApproved(true); // Auto-approve for direct registration
            user.setActive(true);
            // Note: organization will be null for direct registrations
            // This is for testing/development only
            
            User createdUser = userService.createUser(user);
            
            // Remove password hash from response
            createdUser.setPasswordHash(null);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", createdUser));
                
        } catch (IllegalArgumentException e) {
            log.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * POST /api/auth/login
     * Authenticate user and return JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for username: {}", request.getUsername());
        
        Optional<User> userOpt = userService.authenticateUser(
            request.getUsername(), 
            request.getPassword()
        );
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid username or password"));
        }
        
        User user = userOpt.get();
        
        // TODO: Implement first-time password change flow
        // For now, allow students to login with temporary password
        // Mark as approved on first successful login
        if (!user.getApproved() && user.getUserType() == User.UserType.STUDENT) {
            log.info("Student {} logging in for first time, marking as approved", user.getUsername());
            user.setApproved(true);
            userService.updateUser(user);
        }
        
        // Generate JWT token with organization context
        Long organizationId = user.getOrganization() != null ? user.getOrganization().getId() : null;
        String userType = user.getUserType() != null ? user.getUserType().name() : "STUDENT";
        
        String token = jwtUtil.generateToken(
            user.getUsername(), 
            user.getId(), 
            user.getRole().name(),
            organizationId,
            userType
        );
        
        log.info("JWT token generated for user: {} (org: {})", user.getUsername(), organizationId);
        
        LoginResponse response = new LoginResponse(
            token,
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole().name()
        );
        
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    /**
     * GET /api/auth/me
     * Get current authenticated user details
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> getCurrentUser(HttpServletRequest request) {
        try {
            // Extract userId from request attribute (set by JwtAuthenticationFilter)
            Long userId = (Long) request.getAttribute("userId");
            
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("User not authenticated"));
            }
            
            Optional<User> userOpt = userService.getUserById(userId);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found"));
            }
            
            User user = userOpt.get();
            // Remove password hash from response
            user.setPasswordHash(null);
            
            return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
            
        } catch (Exception e) {
            log.error("Error fetching current user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to fetch user details"));
        }
    }

    /**
     * POST /api/auth/logout
     * Logout current user
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        // TODO: Invalidate JWT token if using token blacklist
        return ResponseEntity.ok(
            ApiResponse.success("Logged out successfully", null)
        );
    }

    /**
     * POST /api/auth/first-login
     * Student first-time login password setup
     */
    @PostMapping("/first-login")
    public ResponseEntity<ApiResponse<LoginResponse>> firstTimeLogin(
        @Valid @RequestBody FirstTimeLoginRequest request
    ) {
        log.info("First-time login for student: {}", request.getStudentId());

        try {
            // Find student by student ID, email and organization
            Optional<User> userOpt = userService.findByStudentIdEmailAndOrg(
                request.getStudentId(),
                request.getEmail(),
                request.getOrganizationCode()
            );

            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid credentials or organization code"));
            }

            User student = userOpt.get();

            // Check if already approved
            if (student.getApproved()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Account already activated. Please use regular login."));
            }

            // Update password and approve
            userService.updatePasswordAndApprove(student.getId(), request.getNewPassword());

            // Generate JWT token
            Long organizationId = student.getOrganization() != null ? student.getOrganization().getId() : null;
            String token = jwtUtil.generateToken(
                student.getUsername(),
                student.getId(),
                student.getRole().name(),
                organizationId,
                student.getUserType().name()
            );

            LoginResponse response = new LoginResponse(
                token,
                student.getId(),
                student.getUsername(),
                student.getEmail(),
                student.getRole().name()
            );

            log.info("First-time login successful for: {}", request.getStudentId());
            return ResponseEntity.ok(ApiResponse.success("Account activated successfully", response));

        } catch (Exception e) {
            log.error("First-time login failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("First-time login failed"));
        }
    }
}
