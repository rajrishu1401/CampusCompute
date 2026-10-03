package com.campuscompute.repository;

import com.campuscompute.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Organization entity
 */
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    Optional<Organization> findByCode(String code);

    Optional<Organization> findByContactEmail(String contactEmail);

    boolean existsByCode(String code);

    boolean existsByContactEmail(String contactEmail);
}
