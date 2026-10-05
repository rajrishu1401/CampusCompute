package com.campuscompute.service;

import com.campuscompute.dto.EnrollmentTokenResponse;
import com.campuscompute.dto.OrganizationRegisterRequest;
import com.campuscompute.dto.StudentBulkUploadRequest;
import com.campuscompute.entity.Organization;
import com.campuscompute.entity.User;
import com.campuscompute.exception.ResourceNotFoundException;
import com.campuscompute.repository.OrganizationRepository;
import com.campuscompute.repository.UserRepository;
import com.campuscompute.repository.DeviceRepository;
import com.campuscompute.repository.ContainerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing organizations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final ContainerRepository containerRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.backend-url:http://localhost:8081}")
    private String backendUrl;

    /**
     * Register a new organization with admin user
     */
    @Transactional
    public Organization registerOrganization(OrganizationRegisterRequest request) {
        log.info("Registering organization: {}", request.getCode());

        // Validate uniqueness
        if (organizationRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Organization code already exists");
        }

        if (organizationRepository.existsByContactEmail(request.getContactEmail())) {
            throw new IllegalArgumentException("Contact email already registered");
        }

        if (userRepository.existsByUsername(request.getAdminUsername())) {
            throw new IllegalArgumentException("Admin username already exists");
        }

        if (userRepository.existsByEmail(request.getAdminEmail())) {
            throw new IllegalArgumentException("Admin email already exists");
        }

        // Create organization
        Organization organization = new Organization();
        organization.setName(request.getName());
        organization.setCode(request.getCode().toUpperCase());
        organization.setDomain(request.getDomain());
        organization.setContactEmail(request.getContactEmail());
        organization.setContactPhone(request.getContactPhone());
        organization.setAddress(request.getAddress());
        organization.setActive(true);
        organization.setSubscriptionTier(Organization.SubscriptionTier.FREE);

        organization = organizationRepository.save(organization);
        log.info("Organization created with ID: {}", organization.getId());

        // Create admin user
        User admin = new User();
        admin.setUsername(request.getAdminUsername());
        admin.setEmail(request.getAdminEmail());
        admin.setPasswordHash(passwordEncoder.encode(request.getAdminPassword()));
        admin.setFullName(request.getAdminFullName());
        admin.setRole(User.UserRole.ORG_ADMIN);
        admin.setUserType(User.UserType.ORG_ADMIN);
        admin.setOrganization(organization);
        admin.setActive(true);
        admin.setApproved(true);

        userRepository.save(admin);
        log.info("Admin user created for organization: {}", organization.getCode());

        return organization;
    }

    /**
     * Get organization by ID
     */
    public Organization getOrganizationById(Long id) {
        return organizationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
    }

    /**
     * Get organization by code
     */
    public Optional<Organization> getOrganizationByCode(String code) {
        return organizationRepository.findByCode(code.toUpperCase());
    }

    /**
     * Update organization details
     */
    @Transactional
    public Organization updateOrganization(Long id, Organization updatedOrg) {
        Organization org = getOrganizationById(id);

        if (updatedOrg.getName() != null) {
            org.setName(updatedOrg.getName());
        }
        if (updatedOrg.getContactEmail() != null) {
            org.setContactEmail(updatedOrg.getContactEmail());
        }
        if (updatedOrg.getContactPhone() != null) {
            org.setContactPhone(updatedOrg.getContactPhone());
        }
        if (updatedOrg.getAddress() != null) {
            org.setAddress(updatedOrg.getAddress());
        }
        if (updatedOrg.getLogoUrl() != null) {
            org.setLogoUrl(updatedOrg.getLogoUrl());
        }

        return organizationRepository.save(org);
    }

    /**
     * Generate device enrollment token
     */
    public EnrollmentTokenResponse generateEnrollmentToken(Long organizationId) {
        Organization org = getOrganizationById(organizationId);

        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);

        // TODO: Store token in Redis with expiration

        String installScript = generateInstallScript(token);

        return new EnrollmentTokenResponse(
            token,
            org.getId(),
            org.getCode(),
            expiresAt,
            installScript,
            backendUrl
        );
    }

    /**
     * Generate agent installation script
     */
    private String generateInstallScript(String token) {
        return String.format("""
            #!/bin/bash
            # CampusCompute Agent Installer
            # Generated: %s
            
            ENROLLMENT_TOKEN="%s"
            BACKEND_URL="%s"
            
            # Check if running as root
            if [ "$EUID" -ne 0 ]; then
                echo "Please run as root: sudo bash install.sh"
                exit 1
            fi
            
            echo "Installing CampusCompute Agent..."
            
            # Install Docker
            if ! command -v docker &> /dev/null; then
                echo "Installing Docker..."
                curl -fsSL https://get.docker.com | sh
                systemctl enable docker
                systemctl start docker
            else
                echo "Docker already installed"
            fi
            
            # Install Python
            apt-get update
            apt-get install -y python3 python3-pip wget
            
            # Download agent
            echo "Downloading agent..."
            mkdir -p /opt/campuscompute
            wget ${BACKEND_URL}/download/agent.tar.gz -O /tmp/agent.tar.gz
            tar -xzf /tmp/agent.tar.gz -C /opt/campuscompute
            
            # Install dependencies
            cd /opt/campuscompute/agent
            pip3 install -r requirements.txt
            
            # Configure agent
            cat > config.yaml <<EOF
            broker:
              url: "${BACKEND_URL}/ws/agent"
              enrollment_token: "${ENROLLMENT_TOKEN}"
            
            docker:
              socket: "unix:///var/run/docker.sock"
            
            logging:
              level: "INFO"
              file: "/var/log/campuscompute/agent.log"
            EOF
            
            # Create log directory
            mkdir -p /var/log/campuscompute
            
            # Create systemd service
            cat > /etc/systemd/system/campuscompute-agent.service <<EOF
            [Unit]
            Description=CampusCompute Agent
            After=docker.service
            
            [Service]
            Type=simple
            User=root
            WorkingDirectory=/opt/campuscompute/agent
            ExecStart=/usr/bin/python3 /opt/campuscompute/agent/src/main.py
            Restart=always
            RestartSec=10
            
            [Install]
            WantedBy=multi-user.target
            EOF
            
            # Start service
            systemctl daemon-reload
            systemctl enable campuscompute-agent
            systemctl start campuscompute-agent
            
            echo ""
            echo "======================================"
            echo "CampusCompute Agent installed successfully!"
            echo "Device will appear in dashboard shortly."
            echo "======================================"
            """,
            LocalDateTime.now(),
            token,
            backendUrl
        );
    }

    /**
     * Bulk register students from CSV
     */
    @Transactional
    public List<Map<String, Object>> bulkRegisterStudents(Long organizationId, StudentBulkUploadRequest request) {
        log.info("Bulk registering {} students for organization {}", 
            request.getStudents().size(), organizationId);

        Organization org = getOrganizationById(organizationId);

        List<Map<String, Object>> createdStudents = new ArrayList<>();

        for (StudentBulkUploadRequest.StudentData data : request.getStudents()) {
            try {
                // Validate email domain if org has domain
                if (org.getDomain() != null && !data.getEmail().endsWith("@" + org.getDomain())) {
                    log.warn("Skipping student {}: email domain mismatch", data.getStudentId());
                    continue;
                }

                // Check if student already exists
                if (userRepository.existsByEmail(data.getEmail())) {
                    log.warn("Skipping student {}: email already exists", data.getStudentId());
                    continue;
                }

                // Generate temporary password BEFORE encoding
                String temporaryPassword = generateTemporaryPassword();

                User student = new User();
                student.setUsername(data.getStudentId()); // Use student ID as username
                student.setStudentId(data.getStudentId());
                student.setEmail(data.getEmail());
                student.setFullName(data.getFullName());
                student.setDepartment(data.getDepartment());
                student.setPasswordHash(passwordEncoder.encode(temporaryPassword));
                student.setRole(User.UserRole.STUDENT);
                student.setUserType(User.UserType.STUDENT);
                student.setOrganization(org);
                student.setActive(true);
                student.setApproved(false); // Requires first-time login

                // Apply org-level quotas
                student.setMaxContainers(org.getMaxContainersPerStudent());
                student.setMaxCpuCores(org.getMaxCpuCoresPerStudent());
                student.setMaxRamGb(org.getMaxRamGbPerStudent());

                User saved = userRepository.save(student);
                
                // Return student info WITH temporary password
                Map<String, Object> studentInfo = new HashMap<>();
                studentInfo.put("id", saved.getId());
                studentInfo.put("studentId", saved.getStudentId());
                studentInfo.put("username", saved.getUsername());
                studentInfo.put("email", saved.getEmail());
                studentInfo.put("fullName", saved.getFullName());
                studentInfo.put("temporaryPassword", temporaryPassword);
                
                createdStudents.add(studentInfo);

                log.info("Student created: {} (password will be displayed to admin)", data.getStudentId());

            } catch (Exception e) {
                log.error("Failed to create student {}: {}", data.getStudentId(), e.getMessage());
            }
        }

        log.info("Successfully created {} out of {} students", 
            createdStudents.size(), request.getStudents().size());

        return createdStudents;
    }

    /**
     * Generate temporary password for new students
     */
    private String generateTemporaryPassword() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Get all organizations (ROOT access)
     */
    public List<Organization> getAllOrganizations() {
        return organizationRepository.findAll();
    }

    /**
     * Get organization statistics for dashboard
     */
    public com.campuscompute.dto.OrganizationStatsResponse getOrganizationStats(Long organizationId) {
        log.info("Getting statistics for organization {}", organizationId);

        Organization org = getOrganizationById(organizationId);

        // Get device statistics
        Long totalDevices = deviceRepository.countByOrganizationId(organizationId);
        Long onlineDevices = deviceRepository.countByOrganizationIdAndStatus(
            organizationId, com.campuscompute.entity.Device.DeviceStatus.ONLINE
        );
        Long offlineDevices = deviceRepository.countByOrganizationIdAndStatus(
            organizationId, com.campuscompute.entity.Device.DeviceStatus.OFFLINE
        );
        Long busyDevices = deviceRepository.countByOrganizationIdAndStatus(
            organizationId, com.campuscompute.entity.Device.DeviceStatus.BUSY
        );
        Long maintenanceDevices = deviceRepository.countByOrganizationIdAndStatus(
            organizationId, com.campuscompute.entity.Device.DeviceStatus.MAINTENANCE
        );

        // Get student statistics
        Long totalStudents = userRepository.countByOrganizationIdAndUserType(
            organizationId, com.campuscompute.entity.User.UserType.STUDENT
        );
        Long approvedStudents = userRepository.countByOrganizationIdAndUserTypeAndApprovedTrue(
            organizationId, com.campuscompute.entity.User.UserType.STUDENT
        );
        Long pendingStudents = totalStudents - approvedStudents;

        // Get container statistics
        Long totalContainers = containerRepository.countByOrganizationIdAndStatus(
            organizationId, com.campuscompute.entity.Container.ContainerStatus.RUNNING
        ) + containerRepository.countByOrganizationIdAndStatus(
            organizationId, com.campuscompute.entity.Container.ContainerStatus.STOPPED
        ) + containerRepository.countByOrganizationIdAndStatus(
            organizationId, com.campuscompute.entity.Container.ContainerStatus.PENDING
        );

        Long runningContainers = containerRepository.countByOrganizationIdAndStatus(
            organizationId, com.campuscompute.entity.Container.ContainerStatus.RUNNING
        );
        Long stoppedContainers = containerRepository.countByOrganizationIdAndStatus(
            organizationId, com.campuscompute.entity.Container.ContainerStatus.STOPPED
        );
        Long pendingContainers = containerRepository.countByOrganizationIdAndStatus(
            organizationId, com.campuscompute.entity.Container.ContainerStatus.PENDING
        );
        Long failedContainers = containerRepository.countByOrganizationIdAndStatus(
            organizationId, com.campuscompute.entity.Container.ContainerStatus.FAILED
        );

        // Get resource statistics
        List<com.campuscompute.entity.Device> devices = deviceRepository.findByOrganizationId(organizationId);
        
        Long totalCpu = devices.stream()
            .mapToLong(d -> d.getTotalCpuCores() != null ? d.getTotalCpuCores() : 0)
            .sum();
        Long usedCpu = devices.stream()
            .mapToLong(d -> d.getUsedCpuCores() != null ? d.getUsedCpuCores() : 0)
            .sum();
        Long totalRam = devices.stream()
            .mapToLong(d -> d.getTotalRamBytes() != null ? d.getTotalRamBytes() : 0)
            .sum();
        Long usedRam = devices.stream()
            .mapToLong(d -> d.getUsedRamBytes() != null ? d.getUsedRamBytes() : 0)
            .sum();

        Double cpuUtil = totalCpu > 0 ? (usedCpu * 100.0 / totalCpu) : 0.0;
        Double ramUtil = totalRam > 0 ? (usedRam * 100.0 / totalRam) : 0.0;

        // Build response
        com.campuscompute.dto.OrganizationStatsResponse response = new com.campuscompute.dto.OrganizationStatsResponse();
        response.setOrganizationId(org.getId());
        response.setOrganizationName(org.getName());
        response.setOrganizationCode(org.getCode());

        response.setDevices(new com.campuscompute.dto.OrganizationStatsResponse.DeviceStats(
            totalDevices, onlineDevices, offlineDevices, busyDevices, maintenanceDevices
        ));

        response.setStudents(new com.campuscompute.dto.OrganizationStatsResponse.StudentStats(
            totalStudents, approvedStudents, approvedStudents, pendingStudents
        ));

        response.setContainers(new com.campuscompute.dto.OrganizationStatsResponse.ContainerStats(
            totalContainers, runningContainers, stoppedContainers, pendingContainers, failedContainers
        ));

        response.setResources(new com.campuscompute.dto.OrganizationStatsResponse.ResourceStats(
            totalCpu, usedCpu, totalCpu - usedCpu,
            totalRam, usedRam, totalRam - usedRam,
            cpuUtil, ramUtil
        ));

        return response;
    }

    /**
     * Get organization devices with statistics
     */
    public java.util.List<com.campuscompute.dto.DeviceDetailsResponse> getOrganizationDevicesWithStats(Long organizationId) {
        log.info("Getting devices with stats for organization {}", organizationId);

        java.util.List<com.campuscompute.entity.Device> devices = deviceRepository.findByOrganizationId(organizationId);
        java.util.List<com.campuscompute.dto.DeviceDetailsResponse> responses = new java.util.ArrayList<>();

        for (com.campuscompute.entity.Device device : devices) {
            // Get active container count
            Long containerCount = containerRepository.countByDeviceIdAndStatus(
                device.getId(), 
                com.campuscompute.entity.Container.ContainerStatus.RUNNING
            );

            com.campuscompute.dto.DeviceDetailsResponse response = 
                com.campuscompute.dto.DeviceDetailsResponse.fromDevice(device, containerCount.intValue());

            responses.add(response);
        }

        return responses;
    }

    /**
     * Get organization students with statistics
     */
    public java.util.List<com.campuscompute.dto.StudentDetailsResponse> getOrganizationStudentsWithStats(Long organizationId) {
        log.info("Getting students with stats for organization {}", organizationId);

        java.util.List<com.campuscompute.entity.User> students = 
            userRepository.findByOrganizationIdAndUserType(
                organizationId, 
                com.campuscompute.entity.User.UserType.STUDENT
            );

        java.util.List<com.campuscompute.dto.StudentDetailsResponse> responses = new java.util.ArrayList<>();

        for (com.campuscompute.entity.User student : students) {
            // Get container statistics
            Long totalContainers = containerRepository.countByUserId(student.getId());
            Long runningContainers = containerRepository.countByUserIdAndStatus(
                student.getId(),
                com.campuscompute.entity.Container.ContainerStatus.RUNNING
            );

            // Get resource usage
            Long cpuUsed = containerRepository.getTotalCpuCoresByUser(student.getId());
            Long ramUsed = containerRepository.getTotalRamBytesByUser(student.getId());

            com.campuscompute.dto.StudentDetailsResponse response = 
                com.campuscompute.dto.StudentDetailsResponse.fromUser(
                    student,
                    totalContainers != null ? totalContainers.intValue() : 0,
                    runningContainers != null ? runningContainers.intValue() : 0,
                    cpuUsed,
                    ramUsed
                );

            responses.add(response);
        }

        return responses;
    }
}
