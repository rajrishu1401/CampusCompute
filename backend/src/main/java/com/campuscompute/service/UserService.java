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
}
