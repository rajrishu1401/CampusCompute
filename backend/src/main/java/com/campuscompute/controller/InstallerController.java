package com.campuscompute.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Controller for downloading the agent installer
 */
@RestController
@RequestMapping("/api/installer")
public class InstallerController {

    private static final String INSTALLER_RELATIVE_PATH = "agent-installer/dist/CampusCompute-Agent-Installer.exe";

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadInstaller() {
        try {
            // Get the installer file from the project root (one level up from backend/)
            Path projectRoot = Paths.get(System.getProperty("user.dir")).getParent();
            Path filePath = projectRoot.resolve(INSTALLER_RELATIVE_PATH).normalize();
            
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION, 
                                "attachment; filename=\"CampusCompute-Agent-Installer.exe\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/info")
    public ResponseEntity<?> getInstallerInfo() {
        try {
            // Get the installer file from the project root (one level up from backend/)
            Path projectRoot = Paths.get(System.getProperty("user.dir")).getParent();
            Path filePath = projectRoot.resolve(INSTALLER_RELATIVE_PATH).normalize();
            
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .body(new InstallerInfo(
                                "CampusCompute-Agent-Installer.exe",
                                resource.contentLength(),
                                "1.0.0",
                                true
                        ));
            } else {
                return ResponseEntity.ok()
                        .body(new InstallerInfo(
                                "CampusCompute-Agent-Installer.exe",
                                0L,
                                "1.0.0",
                                false
                        ));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // DTO for installer info
    private record InstallerInfo(
            String filename,
            long size,
            String version,
            boolean available
    ) {}
}
