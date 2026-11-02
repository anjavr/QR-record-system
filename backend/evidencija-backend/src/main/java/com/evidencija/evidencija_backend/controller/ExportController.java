package com.evidencija.evidencija_backend.controller;

import com.evidencija.evidencija_backend.model.Course;
import com.evidencija.evidencija_backend.model.Employee;
import com.evidencija.evidencija_backend.model.Session;
import com.evidencija.evidencija_backend.repository.CourseRepository;
import com.evidencija.evidencija_backend.service.EmployeeService;
import com.evidencija.evidencija_backend.service.ExportService;
import com.evidencija.evidencija_backend.service.SessionService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
public class ExportController {

    private final ExportService exportService;
    private final SessionService sessionService;
    private final CourseRepository courseRepository;
    private final EmployeeService employeeService;

    public ExportController(ExportService exportService, SessionService sessionService,
                            CourseRepository courseRepository, EmployeeService employeeService) {
        this.exportService = exportService;
        this.sessionService = sessionService;
        this.courseRepository = courseRepository;
        this.employeeService = employeeService;
    }

    @GetMapping("/api/sessions/{id}/export/excel")
    public ResponseEntity<byte[]> exportSessionExcel(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) throws Exception {
        Employee employee = employeeService.findOrCreate(jwt);
        Session session = sessionService.findByIdVerified(id, employee);
        byte[] data = exportService.exportSessionToExcel(session);
        return buildFileResponse(data, "sesija-" + id + ".xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @GetMapping("/api/sessions/{id}/export/pdf")
    public ResponseEntity<byte[]> exportSessionPdf(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) throws Exception {
        Employee employee = employeeService.findOrCreate(jwt);
        Session session = sessionService.findByIdVerified(id, employee);
        byte[] data = exportService.exportSessionToPdf(session);
        return buildFileResponse(data, "sesija-" + id + ".pdf", "application/pdf");
    }

    @GetMapping("/api/courses/{id}/export/excel")
    public ResponseEntity<byte[]> exportCourseExcel(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) throws Exception {
        Employee employee = employeeService.findOrCreate(jwt);
        Course course = findCourseVerified(id, employee);
        byte[] data = exportService.exportCourseToExcel(course);
        return buildFileResponse(data, "predmet-" + id + "-kumulativno.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @GetMapping("/api/courses/{id}/export/pdf")
    public ResponseEntity<byte[]> exportCoursePdf(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) throws Exception {
        Employee employee = employeeService.findOrCreate(jwt);
        Course course = findCourseVerified(id, employee);
        byte[] data = exportService.exportCourseToPdf(course);
        return buildFileResponse(data, "predmet-" + id + "-kumulativno.pdf", "application/pdf");
    }

    private Course findCourseVerified(Long id, Employee employee) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        if (!course.getEmployee().getId().equals(employee.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your course");
        }
        return course;
    }

    private ResponseEntity<byte[]> buildFileResponse(byte[] data, String filename, String contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(contentType))
                .body(data);
    }
}