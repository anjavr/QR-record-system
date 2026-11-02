package com.evidencija.analytics_service.repository;

import com.evidencija.analytics_service.model.StudentView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentViewRepository extends JpaRepository<StudentView, Long> {
    @Query(value = "SELECT s.* FROM students s JOIN course_student cs ON cs.student_id = s.id WHERE cs.course_id = :courseId", nativeQuery = true)
    List<StudentView> findByCourseId(@Param("courseId") Long courseId);
}