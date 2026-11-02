package com.evidencija.evidencija_backend.controller;

import com.evidencija.evidencija_backend.dto.CourseSummaryDto;
import com.evidencija.evidencija_backend.dto.UploadResultDto;
import com.evidencija.evidencija_backend.model.Course;
import com.evidencija.evidencija_backend.model.Employee;
import com.evidencija.evidencija_backend.repository.CourseRepository;
import com.evidencija.evidencija_backend.service.CourseService;
import com.evidencija.evidencija_backend.service.CourseUploadService;
import com.evidencija.evidencija_backend.service.EmployeeService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
public class CourseUploadController {

    private final CourseUploadService courseUploadService;
    private final EmployeeService employeeService;
    private final CourseService courseService;
    private final CourseRepository courseRepository;

    public CourseUploadController(CourseUploadService courseUploadService,
                                  EmployeeService employeeService,
                                  CourseService courseService,
                                  CourseRepository courseRepository) {
        this.courseUploadService = courseUploadService;
        this.employeeService = employeeService;
        this.courseService = courseService;
        this.courseRepository = courseRepository;
    }

    @PostMapping("/upload")
    public UploadResultDto upload(@RequestParam("file") MultipartFile file,
                                  @RequestParam("academicYear") Integer academicYear,
                                  @RequestParam(value = "confirmReplace", defaultValue = "false") boolean confirmReplace,
                                  @AuthenticationPrincipal Jwt jwt) {
        Employee employee = employeeService.findOrCreate(jwt);
        return courseUploadService.processXlsx(file, employee, academicYear, confirmReplace);
    }

    @GetMapping("/mine")
    public List<CourseSummaryDto> myCourses(@AuthenticationPrincipal Jwt jwt) {
        Employee employee = employeeService.findOrCreate(jwt);
        return courseService.myCourses(employee);
    }

    @GetMapping("/verify-owner")
    public Map<String, Object> verifyOwner(@RequestParam Long courseId, @RequestParam String employeeEmail) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        boolean owns = course.getEmployee().getEmail().equalsIgnoreCase(employeeEmail);

        return Map.of(
                "owns", owns,
                "courseName", course.getName(),
                "courseCode", course.getCode()
        );
    }
}