package com.evidencija.analytics_service.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_records")
public class AttendanceRecordView {

    @Id
    private Long id;

    @Column(name = "session_id")
    private Long sessionId;

    @Column(name = "student_id")
    private Long studentId;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    public Long getId() {
        return id;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public Long getStudentId() {
        return studentId;
    }
    public
    LocalDateTime getCheckInTime() {
        return checkInTime;
    }
}