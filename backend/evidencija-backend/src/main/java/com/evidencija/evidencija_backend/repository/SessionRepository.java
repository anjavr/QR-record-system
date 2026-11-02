package com.evidencija.evidencija_backend.repository;

import com.evidencija.evidencija_backend.model.Employee;
import com.evidencija.evidencija_backend.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findByEmployee(Employee employee);
    List<Session> findByCourseId(Long id);
    List<Session> findByEmployeeAndStatus(Employee employee, Session.SessionStatus status);
}