package com.evidencija.evidencija_backend.dto;

public class UploadConflictDto {
    private Long existingCourseId;
    private String courseName;
    private int existingStudentCount;

    public UploadConflictDto(Long existingCourseId, String courseName, int existingStudentCount) {
        this.existingCourseId = existingCourseId;
        this.courseName = courseName;
        this.existingStudentCount = existingStudentCount;
    }

    public Long getExistingCourseId() { return existingCourseId; }
    public String getCourseName() { return courseName; }
    public int getExistingStudentCount() { return existingStudentCount; }
}