package com.evidencija.analytics_service.controller;

import com.evidencija.analytics_service.service.AttendanceAnalyticsService;
import com.evidencija.analytics_service.service.CourseClient;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
public class AnalyticsController {

    private final AttendanceAnalyticsService attendanceAnalyticsService;
    private final CourseClient courseClient;

    public AnalyticsController(AttendanceAnalyticsService attendanceAnalyticsService, CourseClient courseClient) {
        this.attendanceAnalyticsService = attendanceAnalyticsService;
        this.courseClient = courseClient;
    }

    @GetMapping("/api/analytics/course/{courseId}/attendance-by-student")
    public List<Map<String, Object>> attendanceByStudent(@PathVariable Long courseId, @AuthenticationPrincipal Jwt jwt) {
        verifyAccess(courseId, jwt);
        return attendanceAnalyticsService.attendanceByStudent(courseId);
    }

    @GetMapping("/api/analytics/course/{courseId}/attendance-by-session")
    public List<Map<String, Object>> attendanceBySession(@PathVariable Long courseId, @AuthenticationPrincipal Jwt jwt) {
        verifyAccess(courseId, jwt);
        return attendanceAnalyticsService.attendanceBySession(courseId);
    }

    @GetMapping("/api/analytics/course/{courseId}/below-threshold")
    public Map<String, Object> belowThreshold(@PathVariable Long courseId, @AuthenticationPrincipal Jwt jwt) {
        verifyAccess(courseId, jwt);
        return attendanceAnalyticsService.belowThreshold(courseId);
    }

    @GetMapping("/api/analytics/course/{courseId}/group-comparison")
    public List<Map<String, Object>> groupComparison(@PathVariable Long courseId, @AuthenticationPrincipal Jwt jwt) {
        verifyAccess(courseId, jwt);
        return attendanceAnalyticsService.groupComparison(courseId);
    }

    private void verifyAccess(Long courseId, Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        if (!courseClient.verifyOwnership(courseId, email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this course's analytics");
        }
    }
}