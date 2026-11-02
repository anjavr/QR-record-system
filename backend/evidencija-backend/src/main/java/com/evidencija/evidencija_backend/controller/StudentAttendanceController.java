package com.evidencija.evidencija_backend.controller;

import com.evidencija.evidencija_backend.dto.CheckInRequest;
import com.evidencija.evidencija_backend.model.AttendanceRecord;
import com.evidencija.evidencija_backend.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance")
public class StudentAttendanceController {

    private final SessionService sessionService;

    public StudentAttendanceController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping("/check-in")
    public AttendanceRecord checkIn(@RequestBody CheckInRequest request,
                                    @AuthenticationPrincipal Jwt jwt,
                                    HttpServletRequest httpRequest) {
        String email = jwt.getClaimAsString("email");
        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");

        return sessionService.checkIn(request.getToken(), email, ipAddress, userAgent);
    }
}