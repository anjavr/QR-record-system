package com.evidencija.evidencija_backend.service;

import com.evidencija.evidencija_backend.dto.StudentCourseSummaryDto;
import com.evidencija.evidencija_backend.model.Course;
import com.evidencija.evidencija_backend.model.Session;
import com.evidencija.evidencija_backend.model.Student;
import com.evidencija.evidencija_backend.repository.AttendanceRecordRepository;
import com.evidencija.evidencija_backend.repository.SessionRepository;
import com.evidencija.evidencija_backend.repository.StudentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.evidencija.evidencija_backend.service.GroupRulesService;

import java.util.ArrayList;
import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final SessionRepository sessionRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;

    public StudentService(StudentRepository studentRepository, SessionRepository sessionRepository, AttendanceRecordRepository attendanceRecordRepository) {
        this.studentRepository = studentRepository;
        this.sessionRepository = sessionRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
    }

    public Student findByEmail(String email) {
        return studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Student with email " + email + " was not found. " +
                                "Please check if the course instructor has uploaded the attendance list."
                ));
    }

    public List<StudentCourseSummaryDto> getCourseSummaries(Student student) {
        List<StudentCourseSummaryDto> result = new ArrayList<>();

        int idx = GroupRulesService.parseIndexNumber(student.getStudentNumber());
        String lectureGroup = idx >= 0 ? GroupRulesService.findGroup(idx, "G") : null;
        String labGroup = idx >= 0 ? GroupRulesService.findGroup(idx, "L") : null;

        for (Course course : student.getCourses()) {
            List<Session> sessions = sessionRepository.findByCourseId(course.getId());
            int totalSessions = sessions.size();

            int attended = 0;
            for (Session session : sessions) {
                boolean present = attendanceRecordRepository
                        .findBySessionAndStudent(session, student)
                        .isPresent();
                if (present) attended++;
            }

            double percentage = totalSessions == 0 ? 0.0 : (attended * 100.0) / totalSessions;

            result.add(new StudentCourseSummaryDto(
                    course.getId(), course.getName(), course.getCode(),
                    totalSessions, attended, Math.round(percentage * 10) / 10.0,
                    lectureGroup, labGroup
            ));
        }

        return result;
    }
}