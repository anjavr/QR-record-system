package com.evidencija.analytics_service.model;

import jakarta.persistence.*;

@Entity
@Table(name = "sessions")
public class SessionView {

    @Id
    private Long id;

    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "activity_type")
    private String activityType;

    private String status;

    @Column(name = "date")
    private java.time.LocalDate date;

    public Long getId() {
        return id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public String getStatus() {
        return status;
    }

    public java.time.LocalDate getDate() {
        return date;
    }

    public String getActivityType() { return activityType; }

}