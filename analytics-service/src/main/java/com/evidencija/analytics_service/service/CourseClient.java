package com.evidencija.analytics_service.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class CourseClient {

    private final RestTemplate restTemplate;

    public CourseClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean verifyOwnership(Long courseId, String employeeEmail) {
        String url = "http://evidencija-backend/api/courses/verify-owner?courseId=" + courseId
                + "&employeeEmail=" + employeeEmail;
        Map<String, Object> result = restTemplate.getForObject(url, Map.class);
        return result != null && Boolean.TRUE.equals(result.get("owns"));
    }
}