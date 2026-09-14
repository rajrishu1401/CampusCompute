package com.campuscompute.repository;

import com.campuscompute.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * Repository for User entity
 * Provides CRUD operations and custom queries for users
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by username
     * Used for authentication
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by SAP ID
     */
    Optional<User> findBySapId(String sapId);

    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Find all users by role
     */
    List<User> findByRole(User.UserRole role);

    /**
     * Find all active users
     */
    List<User> findByActiveTrue();

    /**
     * Find users by department
     */
    List<User> findByDepartment(String department);

    /**
     * Find active users by role
     */
    List<User> findByRoleAndActiveTrue(User.UserRole role);
}
