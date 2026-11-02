package com.evidencija.evidencija_backend.repository;

import com.evidencija.evidencija_backend.model.AttendanceRecord;
import com.evidencija.evidencija_backend.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    List<AttendanceRecord> findBySession(Session session);
    List<AttendanceRecord> findBySessionId(Long sessionId);
    Optional<AttendanceRecord> findBySessionAndStudent(Session session, com.evidencija.evidencija_backend.model.Student student);
}