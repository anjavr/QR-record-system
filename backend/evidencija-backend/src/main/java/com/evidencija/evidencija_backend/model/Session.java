package com.evidencija.evidencija_backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "sessions")
public class Session {

    public enum ActivityType {
        LECTURE, TUTORIAL, LAB
    }

    public enum SessionStatus {
        CREATED, ACTIVE, CLOSED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Enumerated(EnumType.STRING)
    private ActivityType activityType;

    @Enumerated(EnumType.STRING)
    private SessionStatus status = SessionStatus.CREATED;

    private LocalDate date;
    private LocalTime startTime;
    private String groupLabel;
    private String currentQrToken;
    private LocalDateTime qrTokenExpiresAt;

    public Session() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public ActivityType getActivityType() { return activityType; }
    public void setActivityType(ActivityType activityType) { this.activityType = activityType; }

    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public String getCurrentQrToken() { return currentQrToken; }
    public void setCurrentQrToken(String currentQrToken) { this.currentQrToken = currentQrToken; }

    public LocalDateTime getQrTokenExpiresAt() { return qrTokenExpiresAt; }
    public void setQrTokenExpiresAt(LocalDateTime qrTokenExpiresAt) { this.qrTokenExpiresAt = qrTokenExpiresAt; }

    public String getGroupLabel() { return groupLabel; }
    public void setGroupLabel(String groupLabel) { this.groupLabel = groupLabel; }
}