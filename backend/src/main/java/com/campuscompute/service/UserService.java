package com.campuscompute.service;

import com.campuscompute.entity.User;
import com.campuscompute.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for user management operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Create a new user
     */
    public User createUser(User user) {
        log.info("Creating user: {}", user.getUsername());
        
        // Check if username already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }
        
        // Hash password
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        
        return userRepository.save(user);
    }

    /**
     * Authenticate user with username and password
     */
    public Optional<User> authenticateUser(String username, String password) {
        log.info("Authenticating user: {}", username);
        
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        
        User user = userOpt.get();
        
        // Check if user is active
        if (!user.getActive()) {
            log.warn("User account is inactive: {}", username);
            return Optional.empty();
        }
        
        // Verify password
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            log.warn("Invalid password for user: {}", username);
            return Optional.empty();
        }
        
        return Optional.of(user);
    }

    /**
     * Get user by ID
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Get user by username
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Get user by email
     */
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get users by role
     */
    public List<User> getUsersByRole(User.UserRole role) {
        return userRepository.findByRole(role);
    }

    /**
     * Get active users
     */
    public List<User> getActiveUsers() {
        return userRepository.findByActiveTrue();
    }

    /**
     * Update user
     */
    public User updateUser(User user) {
        log.info("Updating user: {}", user.getUsername());
        
        if (!userRepository.existsById(user.getId())) {
            throw new IllegalArgumentException("User not found: " + user.getId());
        }
        
        return userRepository.save(user);
    }

    /**
     * Update user password
     */
    public void updatePassword(Long userId, String newPassword) {
        log.info("Updating password for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Update user quotas
     */
    public void updateQuotas(Long userId, Integer maxCpuCores, Integer maxRamGb, Integer maxContainers) {
        log.info("Updating quotas for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        user.setMaxCpuCores(maxCpuCores);
        user.setMaxRamGb(maxRamGb);
        user.setMaxContainers(maxContainers);
        
        userRepository.save(user);
    }

    /**
     * Deactivate user
     */
    public void deactivateUser(Long userId) {
        log.info("Deactivating user ID: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        user.setActive(false);
        userRepository.save(user);
    }

    /**
     * Activate user
     */
    public void activateUser(Long userId) {
        log.info("Activating user ID: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        user.setActive(true);
        userRepository.save(user);
    }

    /**
     * Delete user
     */
    public void deleteUser(Long userId) {
        log.info("Deleting user ID: {}", userId);
        
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        
        userRepository.deleteById(userId);
    }

    /**
     * Check if user has resource quota available
     */
    public boolean hasQuotaAvailable(Long userId, Integer cpuCores, Long ramBytes) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        return cpuCores <= user.getMaxCpuCores() && 
               ramBytes <= (user.getMaxRamGb() * 1_000_000_000L);
    }

    /**
     * Get organization code by organization ID (helper method)
     */
    public String getOrganizationByCode(String code) {
        // This is a placeholder - will be properly implemented with OrganizationRepository injection
        return code;
    }

    /**
     * Find student by student ID, email and organization code
     */
    public Optional<User> findByStudentIdEmailAndOrg(String studentId, String email, String orgCode) {
        log.info("Finding student: {} in org: {}", studentId, orgCode);
        
        // Find all users by email
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        
        User user = userOpt.get();
        
        // Verify student ID matches and organization code matches
        if (user.getStudentId() != null && 
            user.getStudentId().equals(studentId) &&
            user.getOrganization() != null &&
            user.getOrganization().getCode().equalsIgnoreCase(orgCode)) {
            return Optional.of(user);
        }
        
        return Optional.empty();
    }

    /**
     * Update password and approve student account
     */
    public void updatePasswordAndApprove(Long userId, String newPassword) {
        log.info("Updating password and approving user ID: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setApproved(true);
        
        userRepository.save(user);
    }

    /**
     * Get all students in an organization
     */
    public List<User> getStudentsByOrganization(Long organizationId) {
        return userRepository.findByOrganizationIdAndUserType(organizationId, User.UserType.STUDENT);
    }

    /**
     * Get all users in an organization
     */
    public List<User> getUsersByOrganization(Long organizationId) {
        return userRepository.findByOrganizationId(organizationId);
    }

    /**
     * Count students in organization
     */
    public Long countStudentsByOrganization(Long organizationId) {
        return userRepository.countByOrganizationIdAndUserType(organizationId, User.UserType.STUDENT);
    }

    /**
     * Get unapproved students in organization
     */
    public List<User> getUnapprovedStudents(Long organizationId) {
        return userRepository.findByOrganizationIdAndApprovedFalse(organizationId);
    }

    /**
     * Approve student account
     */
    public void approveStudent(Long studentId) {
        log.info("Approving student ID: {}", studentId);
        
        User student = userRepository.findById(studentId)
            .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
        
        student.setApproved(true);
        userRepository.save(student);
    }
}
