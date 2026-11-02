package com.evidencija.evidencija_backend.dto;

import com.evidencija.evidencija_backend.model.AttendanceRecord;

import java.time.LocalDateTime;

public class AttendanceRecordDto {
    private Long id;
    private LocalDateTime checkInTime;
    private String ipAddress;
    private String userAgent;
    private StudentInfo student;
    private boolean suspicious;

    public AttendanceRecordDto(AttendanceRecord record, boolean suspicious) {
        this.id = record.getId();
        this.checkInTime = record.getCheckInTime();
        this.ipAddress = record.getIpAddress();
        this.userAgent = record.getUserAgent();
        this.student = new StudentInfo(
                record.getStudent().getFirstName(),
                record.getStudent().getLastName(),
                record.getStudent().getStudentNumber()
        );
        this.suspicious = suspicious;
    }

    public Long getId() { return id; }
    public LocalDateTime getCheckInTime() { return checkInTime; }
    public String getIpAddress() { return ipAddress; }
    public String getUserAgent() { return userAgent; }
    public StudentInfo getStudent() { return student; }
    public boolean isSuspicious() { return suspicious; }

    public static class StudentInfo {
        private String firstName;
        private String lastName;
        private String studentNumber;

        public StudentInfo(String firstName, String lastName, String studentNumber) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.studentNumber = studentNumber;
        }

        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getStudentNumber() { return studentNumber; }
    }
}
