package com.evidencija.analytics_service.repository;

import com.evidencija.analytics_service.model.SessionView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionViewRepository extends JpaRepository<SessionView, Long> {
    List<SessionView> findByCourseId(Long courseId);
}