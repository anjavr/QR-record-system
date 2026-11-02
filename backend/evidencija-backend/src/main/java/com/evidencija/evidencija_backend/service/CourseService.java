package com.evidencija.evidencija_backend.service;

import com.evidencija.evidencija_backend.dto.CourseSummaryDto;
import com.evidencija.evidencija_backend.model.Course;
import com.evidencija.evidencija_backend.model.Employee;
import com.evidencija.evidencija_backend.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public List<CourseSummaryDto> myCourses(Employee employee) {
        return courseRepository.findByEmployee(employee).stream()
                .sorted(Comparator
                        .comparing(Course::getStudyYear, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Course::getAcademicYear, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(c -> new CourseSummaryDto(
                        c.getId(), c.getCode(), c.getName(), c.getStudyProgram(), c.getStudyType(),
                        c.getStudyYear(), c.getSemester(), c.getTeachingForm(), c.getGroupNumber(),
                        c.getStudents().size(),c.getAcademicYear()
                ))
                .toList();
    }
}