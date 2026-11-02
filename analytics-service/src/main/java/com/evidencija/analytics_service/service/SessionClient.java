package com.evidencija.analytics_service.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class SessionClient {

    private final RestTemplate restTemplate;

    public SessionClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Map<String, Object>> getSessionsForCourse(Long courseId) {
        String url = "http://evidencija-backend/api/sessions/by-course/" + courseId;
        return restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}).getBody();
    }
}