package com.evidencija.evidencija_backend.controller;

import com.evidencija.evidencija_backend.dto.StudentCourseSummaryDto;
import com.evidencija.evidencija_backend.model.Student;
import com.evidencija.evidencija_backend.service.StudentService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/me/courses")
    public List<StudentCourseSummaryDto> myCourses(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        Student student = studentService.findByEmail(email);
        return studentService.getCourseSummaries(student);
    }
}