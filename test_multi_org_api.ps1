# CampusCompute Multi-Org API Testing Script
# PowerShell script for testing multi-organization endpoints

$BASE_URL = "http://localhost:8081"
$ADMIN_TOKEN = ""
$STUDENT_TOKEN = ""

Write-Host "==================================" -ForegroundColor Cyan
Write-Host "CampusCompute Multi-Org API Tests" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: Organization Registration
Write-Host "TEST 1: Register Organization (UPES)" -ForegroundColor Yellow
$orgRegister = @{
    name = "UPES Dehradun"
    code = "UPES"
    domain = "upes.ac.in"
    contactEmail = "admin@upes.ac.in"
    contactPhone = "+91-1234567890"
    address = "Dehradun, Uttarakhand"
    adminUsername = "upes_admin"
    adminEmail = "admin@upes.ac.in"
    adminPassword = "admin123456"
    adminFullName = "UPES Administrator"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "$BASE_URL/api/organizations/register" `
    -Method Post `
    -Body $orgRegister `
    -ContentType "application/json" `
    -ErrorAction SilentlyContinue

if ($response) {
    Write-Host "✓ Organization registered successfully!" -ForegroundColor Green
    Write-Host "  Organization ID: $($response.data.id)" -ForegroundColor Gray
    Write-Host "  Organization Code: $($response.data.code)" -ForegroundColor Gray
} else {
    Write-Host "✗ Organization registration failed" -ForegroundColor Red
}
Write-Host ""

# Test 2: Admin Login
Write-Host "TEST 2: Admin Login" -ForegroundColor Yellow
$loginRequest = @{
    username = "upes_admin"
    password = "admin123456"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "$BASE_URL/api/auth/login" `
    -Method Post `
    -Body $loginRequest `
    -ContentType "application/json" `
    -ErrorAction SilentlyContinue

if ($response -and $response.data.token) {
    $ADMIN_TOKEN = $response.data.token
    Write-Host "✓ Admin login successful!" -ForegroundColor Green
    Write-Host "  Token: $($ADMIN_TOKEN.Substring(0, 20))..." -ForegroundColor Gray
} else {
    Write-Host "✗ Admin login failed" -ForegroundColor Red
}
Write-Host ""

# Test 3: Get Organization Details
Write-Host "TEST 3: Get Organization Details" -ForegroundColor Yellow
$headers = @{
    "Authorization" = "Bearer $ADMIN_TOKEN"
}

$response = Invoke-RestMethod -Uri "$BASE_URL/api/organizations/me" `
    -Method Get `
    -Headers $headers `
    -ErrorAction SilentlyContinue

if ($response) {
    Write-Host "✓ Organization details retrieved!" -ForegroundColor Green
    Write-Host "  Name: $($response.data.name)" -ForegroundColor Gray
    Write-Host "  Code: $($response.data.code)" -ForegroundColor Gray
    Write-Host "  Max Devices: $($response.data.maxDevices)" -ForegroundColor Gray
    Write-Host "  Max Students: $($response.data.maxStudents)" -ForegroundColor Gray
} else {
    Write-Host "✗ Failed to get organization details" -ForegroundColor Red
}
Write-Host ""

# Test 4: Generate Enrollment Token
Write-Host "TEST 4: Generate Device Enrollment Token" -ForegroundColor Yellow
$response = Invoke-RestMethod -Uri "$BASE_URL/api/organizations/devices/token" `
    -Method Post `
    -Headers $headers `
    -ErrorAction SilentlyContinue

if ($response -and $response.data.token) {
    Write-Host "✓ Enrollment token generated!" -ForegroundColor Green
    Write-Host "  Token: $($response.data.token.Substring(0, 20))..." -ForegroundColor Gray
    Write-Host "  Expires: $($response.data.expiresAt)" -ForegroundColor Gray
    Write-Host "  Backend URL: $($response.data.backendUrl)" -ForegroundColor Gray
    
    # Save install script to file
    $response.data.installScript | Out-File -FilePath "agent_install.sh" -Encoding UTF8
    Write-Host "  Install script saved to: agent_install.sh" -ForegroundColor Gray
} else {
    Write-Host "✗ Failed to generate enrollment token" -ForegroundColor Red
}
Write-Host ""

# Test 5: Bulk Upload Students
Write-Host "TEST 5: Bulk Upload Students" -ForegroundColor Yellow
$studentsUpload = @{
    students = @(
        @{
            studentId = "500101234"
            email = "student1@upes.ac.in"
            fullName = "John Doe"
            department = "Computer Science"
        },
        @{
            studentId = "500101235"
            email = "student2@upes.ac.in"
            fullName = "Jane Smith"
            department = "Computer Science"
        },
        @{
            studentId = "500101236"
            email = "student3@upes.ac.in"
            fullName = "Alice Johnson"
            department = "Information Technology"
        }
    )
} | ConvertTo-Json -Depth 3

$response = Invoke-RestMethod -Uri "$BASE_URL/api/organizations/students/upload" `
    -Method Post `
    -Headers $headers `
    -Body $studentsUpload `
    -ContentType "application/json" `
    -ErrorAction SilentlyContinue

if ($response) {
    Write-Host "✓ Students uploaded successfully!" -ForegroundColor Green
    Write-Host "  Total Requested: $($response.data.totalRequested)" -ForegroundColor Gray
    Write-Host "  Total Created: $($response.data.totalCreated)" -ForegroundColor Gray
} else {
    Write-Host "✗ Failed to upload students" -ForegroundColor Red
}
Write-Host ""

# Test 6: Student First-Time Login
Write-Host "TEST 6: Student First-Time Login" -ForegroundColor Yellow
$firstLogin = @{
    studentId = "500101234"
    email = "student1@upes.ac.in"
    organizationCode = "UPES"
    newPassword = "student123"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "$BASE_URL/api/auth/first-login" `
    -Method Post `
    -Body $firstLogin `
    -ContentType "application/json" `
    -ErrorAction SilentlyContinue

if ($response -and $response.data.token) {
    $STUDENT_TOKEN = $response.data.token
    Write-Host "✓ Student first-time login successful!" -ForegroundColor Green
    Write-Host "  Student: $($response.data.username)" -ForegroundColor Gray
    Write-Host "  Token: $($STUDENT_TOKEN.Substring(0, 20))..." -ForegroundColor Gray
} else {
    Write-Host "✗ Student first-time login failed" -ForegroundColor Red
}
Write-Host ""

# Test 7: Student Regular Login (after approval)
Write-Host "TEST 7: Student Regular Login" -ForegroundColor Yellow
$studentLogin = @{
    username = "500101234"
    password = "student123"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "$BASE_URL/api/auth/login" `
    -Method Post `
    -Body $studentLogin `
    -ContentType "application/json" `
    -ErrorAction SilentlyContinue

if ($response -and $response.data.token) {
    Write-Host "✓ Student regular login successful!" -ForegroundColor Green
    Write-Host "  Username: $($response.data.username)" -ForegroundColor Gray
    Write-Host "  Role: $($response.data.role)" -ForegroundColor Gray
} else {
    Write-Host "✗ Student regular login failed" -ForegroundColor Red
}
Write-Host ""

# Test 8: Get Current User (Student)
Write-Host "TEST 8: Get Current User (Student)" -ForegroundColor Yellow
$studentHeaders = @{
    "Authorization" = "Bearer $STUDENT_TOKEN"
}

$response = Invoke-RestMethod -Uri "$BASE_URL/api/auth/me" `
    -Method Get `
    -Headers $studentHeaders `
    -ErrorAction SilentlyContinue

if ($response) {
    Write-Host "✓ Current user retrieved!" -ForegroundColor Green
    Write-Host "  Full Name: $($response.data.fullName)" -ForegroundColor Gray
    Write-Host "  Email: $($response.data.email)" -ForegroundColor Gray
    Write-Host "  Department: $($response.data.department)" -ForegroundColor Gray
    Write-Host "  Approved: $($response.data.approved)" -ForegroundColor Gray
} else {
    Write-Host "✗ Failed to get current user" -ForegroundColor Red
}
Write-Host ""

# Test 9: Create Container (as Student)
Write-Host "TEST 9: Create Container (as Student)" -ForegroundColor Yellow
$containerRequest = @{
    image = "alpine:latest"
    cpuCores = 1
    ramBytes = 1073741824
    diskBytes = 5368709120
    lifetimeMs = 14400000
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "$BASE_URL/api/containers" `
    -Method Post `
    -Headers $studentHeaders `
    -Body $containerRequest `
    -ContentType "application/json" `
    -ErrorAction SilentlyContinue

if ($response) {
    Write-Host "✓ Container creation initiated!" -ForegroundColor Green
    Write-Host "  Container ID: $($response.data.id)" -ForegroundColor Gray
    Write-Host "  Status: $($response.data.status)" -ForegroundColor Gray
    Write-Host "  Image: $($response.data.image)" -ForegroundColor Gray
} else {
    Write-Host "✗ Failed to create container" -ForegroundColor Red
}
Write-Host ""

# Summary
Write-Host "==================================" -ForegroundColor Cyan
Write-Host "Test Summary" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Admin Token: " -NoNewline
if ($ADMIN_TOKEN) {
    Write-Host "✓ Available" -ForegroundColor Green
} else {
    Write-Host "✗ Not available" -ForegroundColor Red
}

Write-Host "Student Token: " -NoNewline
if ($STUDENT_TOKEN) {
    Write-Host "✓ Available" -ForegroundColor Green
} else {
    Write-Host "✗ Not available" -ForegroundColor Red
}

Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "1. Check database to verify organization and users were created"
Write-Host "2. Install agent on a machine using agent_install.sh"
Write-Host "3. Test device connectivity"
Write-Host "4. Test container lifecycle with multi-org context"
Write-Host ""
