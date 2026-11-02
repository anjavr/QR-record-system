package com.evidencija.evidencija_backend.repository;

import com.evidencija.evidencija_backend.model.Course;
import com.evidencija.evidencija_backend.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByEmployee(Employee employee);
    Optional<Course> findByEmployeeAndCodeAndAcademicYearAndGroupNumber(
            Employee employee, String code, Integer academicYear, Integer groupNumber);
}