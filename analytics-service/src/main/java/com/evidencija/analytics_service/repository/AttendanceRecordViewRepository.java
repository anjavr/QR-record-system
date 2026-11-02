package com.evidencija.analytics_service.repository;

import com.evidencija.analytics_service.model.AttendanceRecordView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttendanceRecordViewRepository extends JpaRepository<AttendanceRecordView, Long> {
    List<AttendanceRecordView> findBySessionIdIn(List<Long> sessionIds);
}