package com.evidencija.evidencija_backend.dto;

public class UploadResultDto {
    private String courseName;
    private int studentsProcessed;
    private int studentsCreated;
    private int studentsLinked;
    private int studentsUpdated;

    public UploadResultDto(String courseName, int studentsProcessed, int studentsCreated, int studentsLinked, int studentsUpdated) {
        this.courseName = courseName;
        this.studentsProcessed = studentsProcessed;
        this.studentsCreated = studentsCreated;
        this.studentsLinked = studentsLinked;
        this.studentsUpdated = studentsUpdated;
    }

    public String getCourseName() { return courseName; }
    public int getStudentsProcessed() { return studentsProcessed; }
    public int getStudentsCreated() { return studentsCreated; }
    public int getStudentsLinked() { return studentsLinked; }
    public int getStudentsUpdated() { return studentsUpdated; }
}