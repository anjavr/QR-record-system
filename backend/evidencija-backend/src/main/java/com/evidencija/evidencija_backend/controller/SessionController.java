package com.evidencija.evidencija_backend.controller;

import com.evidencija.evidencija_backend.dto.AttendanceRecordDto;
import com.evidencija.evidencija_backend.dto.CreateSessionRequest;
import com.evidencija.evidencija_backend.model.AttendanceRecord;
import com.evidencija.evidencija_backend.model.Employee;
import com.evidencija.evidencija_backend.model.Session;
import com.evidencija.evidencija_backend.service.EmployeeService;
import com.evidencija.evidencija_backend.service.GroupRulesService;
import com.evidencija.evidencija_backend.service.SessionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;
    private final EmployeeService employeeService;

    public SessionController(SessionService sessionService, EmployeeService employeeService) {
        this.sessionService = sessionService;
        this.employeeService = employeeService;
    }

    @PostMapping
    public Session create(@RequestBody CreateSessionRequest request, @AuthenticationPrincipal Jwt jwt) {
        Employee employee = employeeService.findOrCreate(jwt);
        return sessionService.create(request, employee);
    }

    @PostMapping("/{id}/activate")
    public Session activate(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Employee employee = employeeService.findOrCreate(jwt);
        return sessionService.activate(id, employee);
    }

    @PostMapping("/{id}/close")
    public Session close(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Employee employee = employeeService.findOrCreate(jwt);
        return sessionService.close(id, employee);
    }

    @GetMapping("/mine")
    public List<Session> mySessions(@AuthenticationPrincipal Jwt jwt) {
        Employee employee = employeeService.findOrCreate(jwt);
        return sessionService.mySessions(employee);
    }

    @GetMapping("/{id}/qr")
    public java.util.Map<String, String> getQrCode(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Employee employee = employeeService.findOrCreate(jwt);
        String qrBase64 = sessionService.getCurrentQrCodeImage(id, employee);
        return java.util.Map.of("qrCodeBase64", qrBase64);
    }

    // privremeni test
    @GetMapping("/{id}/current-token")
    public java.util.Map<String, String> getCurrentToken(@PathVariable Long id) {
        Session session = sessionService.findById(id);
        String token = session.getCurrentQrToken();
        return java.util.Map.of("token", token != null ? token : "NULL");
    }

    @GetMapping("/{id}/attendance")
    public List<AttendanceRecordDto> getAttendance(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Employee employee = employeeService.findOrCreate(jwt);
        return sessionService.getAttendanceForSessionWithFlags(id, employee);
    }

    @DeleteMapping("/{sessionId}/attendance/{recordId}")
    public void deleteAttendanceRecord(@PathVariable Long sessionId,
                                       @PathVariable Long recordId,
                                       @AuthenticationPrincipal Jwt jwt) {
        Employee employee = employeeService.findOrCreate(jwt);
        sessionService.deleteAttendanceRecord(sessionId, recordId, employee);
    }

    @GetMapping("/by-course/{courseId}")
    public List<Session> byCourse(@PathVariable Long courseId) {
        return sessionService.findSessionsByCourse(courseId);
    }

    @GetMapping("/group-labels")
    public List<String> groupLabels(@RequestParam String activityType) {
        return GroupRulesService.labelsForCategory(GroupRulesService.category(activityType));
    }
}