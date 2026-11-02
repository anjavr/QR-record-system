package com.evidencija.evidencija_backend.dto;

import com.evidencija.evidencija_backend.model.Session;
import java.time.LocalDate;
import java.time.LocalTime;

public class CreateSessionRequest {

    private Long courseId;
    private Session.ActivityType activityType;
    private LocalDate date;
    private LocalTime startTime;
    private String groupLabel;

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public Session.ActivityType getActivityType() { return activityType; }
    public void setActivityType(Session.ActivityType activityType) { this.activityType = activityType; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public String getGroupLabel() { return groupLabel; }
    public void setGroupLabel(String groupLabel) { this.groupLabel = groupLabel; }
}