# Commit Message: Multi-Organization Architecture - Phase 1

## Title
feat: Implement multi-organization platform architecture (Phase 1)

## Description

Transform CampusCompute into a multi-tenant SaaS platform supporting multiple educational institutions. This is Phase 1 implementing the complete backend foundation for organization management, student bulk registration, and device enrollment.

### What's New

#### 1. Multi-Tenant Data Model
- **New Entity**: `Organization` - Represents educational institutions
  - Subscription tiers (FREE, BASIC, PREMIUM, ENTERPRISE)
  - Per-organization quotas (devices, students, resources)
  - Contact information and branding (logo URL)

- **Updated Entities**:
  - `User`: Added organization relationship, userType (STUDENT/ORG_ADMIN), studentId, approved flag
  - `Device`: Added organization relationship for device scoping
  - `Lab`: Added organization relationship for lab scoping
  - New roles: ORG_ADMIN, ROOT (in addition to existing STUDENT/FACULTY/ADMIN)

#### 2. Organization Management Service
- **OrganizationService**: Complete service layer for multi-org operations
  - Organization registration with automatic admin user creation
  - Device enrollment token generation with installation script
  - Student bulk upload from CSV with validation
  - Organization CRUD operations

- **OrganizationRepository**: JPA repository with org-specific queries

#### 3. Enhanced Authentication Flow
- **JWT Updates**: Tokens now include organizationId and userType claims
- **JwtAuthenticationFilter**: Automatically sets organization context in request attributes
- **First-Time Login**: New endpoint for student password setup after bulk upload
- **Login Validation**: Checks student approval status before authentication

#### 4. Organization-Aware Repositories
All repositories updated with organization filtering:
- **UserRepository**: Find users by org, count students, unapproved students
- **DeviceRepository**: Find devices by org, count online devices per org
- **LabRepository**: Find labs by org, org-specific queries
- **ContainerRepository**: Find containers by org (via user), resource tracking per org

#### 5. New API Endpoints
- `POST /api/organizations/register` - Public org registration (creates org + admin)
- `GET /api/organizations/me` - Get current user's organization
- `PUT /api/organizations/me` - Update organization (ORG_ADMIN only)
- `POST /api/organizations/devices/token` - Generate device enrollment token
- `POST /api/organizations/students/upload` - Bulk student registration
- `POST /api/auth/first-login` - Student first-time password setup
- `GET /api/organizations` - List all orgs (ROOT only)
- `GET /api/organizations/{id}` - Get org by ID (ROOT only)

#### 6. New DTOs
- `OrganizationRegisterRequest` - Org registration with validation
- `EnrollmentTokenResponse` - Device token + installation script
- `StudentBulkUploadRequest` - Bulk student data
- `FirstTimeLoginRequest` - First-time password setup

#### 7. Agent Installation Script
Auto-generated bash script that:
- Installs Docker if not present
- Downloads and configures Python agent
- Registers device with enrollment token
- Creates systemd service for auto-start

#### 8. Data Isolation & Security
- Organization context enforced at filter level
- All queries automatically scoped to user's organization
- ORG_ADMIN can only manage their organization's resources
- Students can only see their own containers
- ROOT role for platform-wide administration

### Technical Details

**Files Added** (9):
- backend/src/main/java/com/campuscompute/entity/Organization.java
- backend/src/main/java/com/campuscompute/repository/OrganizationRepository.java
- backend/src/main/java/com/campuscompute/service/OrganizationService.java
- backend/src/main/java/com/campuscompute/controller/OrganizationController.java
- backend/src/main/java/com/campuscompute/dto/OrganizationRegisterRequest.java
- backend/src/main/java/com/campuscompute/dto/EnrollmentTokenResponse.java
- backend/src/main/java/com/campuscompute/dto/StudentBulkUploadRequest.java
- backend/src/main/java/com/campuscompute/dto/FirstTimeLoginRequest.java
- test_multi_org_api.ps1 (PowerShell testing script)

**Files Modified** (13):
- backend/src/main/java/com/campuscompute/entity/User.java
- backend/src/main/java/com/campuscompute/entity/Device.java
- backend/src/main/java/com/campuscompute/entity/Lab.java
- backend/src/main/java/com/campuscompute/repository/UserRepository.java
- backend/src/main/java/com/campuscompute/repository/DeviceRepository.java
- backend/src/main/java/com/campuscompute/repository/LabRepository.java
- backend/src/main/java/com/campuscompute/repository/ContainerRepository.java
- backend/src/main/java/com/campuscompute/service/UserService.java
- backend/src/main/java/com/campuscompute/controller/AuthController.java
- backend/src/main/java/com/campuscompute/security/JwtUtil.java
- backend/src/main/java/com/campuscompute/security/JwtAuthenticationFilter.java
- backend/src/main/resources/application.yml
- README.md

**Documentation Added** (2):
- MULTI_ORG_IMPLEMENTATION_PHASE1.md (Complete implementation guide)
- COMMIT_MESSAGE_MULTI_ORG.md (This file)

**Build Status**: ✅ 55 source files compiled successfully
**Backward Compatibility**: ✅ Existing /api/auth/register endpoint maintained for testing

### Database Changes
Hibernate will auto-create/update:
- New table: `organizations`
- New columns in `users`: organization_id, user_type, student_id, approved
- New columns in `devices`: organization_id
- New columns in `labs`: organization_id
- Foreign key constraints for organization relationships

**Note**: Existing data will have NULL organization_id. Either start with fresh DB or manually create default org and update existing records.

### Configuration Changes
New application.yml settings:
```yaml
app:
  backend-url: http://localhost:8081
  frontend-url: http://localhost:5173
  organization:
    default-max-devices: 10
    default-max-students: 100
    default-student-containers: 3
    default-student-cpu-cores: 4
    default-student-ram-gb: 8
```

### Testing
Use the provided PowerShell script:
```powershell
.\test_multi_org_api.ps1
```

This tests:
1. Organization registration
2. Admin login
3. Enrollment token generation
4. Student bulk upload
5. Student first-time login
6. Regular login after approval
7. Container creation with org context

### What's Next (Phase 2-4)

**Phase 2**: Agent enrollment token support, org context in WebSocket
**Phase 3**: Organization statistics, admin analytics endpoints
**Phase 4**: React frontend (admin + student dashboards)

### Breaking Changes
⚠️ **JWT Token Structure Changed**: Tokens now include organizationId and userType claims. Old tokens will not work with new code.

⚠️ **User Entity Updated**: New required fields. Direct user registration (without org) sets organization to NULL - intended for testing only.

⚠️ **Login Flow Updated**: Students must be approved (approved=true) to login. Use first-time login endpoint for initial setup.

### Migration Path
1. **For existing installations**: Create a default organization and update all existing users/devices to reference it
2. **For fresh installations**: Start with org registration flow

### Academic Significance
Demonstrates:
- Multi-tenancy pattern in Spring Boot
- Role-Based Access Control (RBAC) with JWT
- Transactional data operations
- Bulk data processing with validation
- RESTful API design for SaaS platforms
- Security-first architecture with data isolation

### Research Contribution
Extends the original adaptive scheduling contribution by adding institutional multi-tenancy, enabling cross-campus resource pooling while maintaining data isolation and administrative boundaries.

---

**Author**: Rishabh Raj  
**Institution**: UPES Dehradun  
**Date**: October 3, 2026  
**Compiled**: ✅ Success (55 files)  
**Status**: Phase 1 Complete, Ready for Testing
