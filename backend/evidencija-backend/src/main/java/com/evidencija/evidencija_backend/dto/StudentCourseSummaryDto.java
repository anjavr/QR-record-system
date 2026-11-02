package com.evidencija.evidencija_backend.dto;

public class StudentCourseSummaryDto {
    private Long courseId;
    private String courseName;
    private String courseCode;
    private int totalSessions;
    private int attendedSessions;
    private double attendancePercentage;
    private String lectureGroup;
    private String labGroup;

    public StudentCourseSummaryDto(Long courseId, String courseName, String courseCode,
                                   int totalSessions, int attendedSessions, double attendancePercentage,
                                   String lectureGroup, String labGroup) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.courseCode = courseCode;
        this.totalSessions = totalSessions;
        this.attendedSessions = attendedSessions;
        this.attendancePercentage = attendancePercentage;
        this.lectureGroup = lectureGroup;
        this.labGroup = labGroup;
    }

    public Long getCourseId() { return courseId; }
    public String getCourseName() { return courseName; }
    public String getCourseCode() { return courseCode; }
    public int getTotalSessions() { return totalSessions; }
    public int getAttendedSessions() { return attendedSessions; }
    public double getAttendancePercentage() { return attendancePercentage; }
    public String getLectureGroup() { return lectureGroup; }
    public String getLabGroup() { return labGroup; }
}