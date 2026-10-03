package com.campuscompute.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for bulk student upload
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentBulkUploadRequest {

    private List<StudentData> students;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentData {
        private String studentId; // Roll number
        private String email;
        private String fullName;
        private String department;
    }
}
