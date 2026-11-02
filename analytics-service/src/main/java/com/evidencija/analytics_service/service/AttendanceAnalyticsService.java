package com.evidencija.analytics_service.service;

import com.evidencija.analytics_service.model.AttendanceRecordView;
import com.evidencija.analytics_service.model.SessionView;
import com.evidencija.analytics_service.model.StudentView;
import com.evidencija.analytics_service.repository.AttendanceRecordViewRepository;
import com.evidencija.analytics_service.repository.SessionViewRepository;
import com.evidencija.analytics_service.repository.SettingViewRepository;
import com.evidencija.analytics_service.repository.StudentViewRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttendanceAnalyticsService {

    private static final String THRESHOLD_KEY = "attendance.threshold.percentage";
    private static final double DEFAULT_THRESHOLD = 60.0;

    private final SessionViewRepository sessionViewRepository;
    private final AttendanceRecordViewRepository attendanceRecordViewRepository;
    private final StudentViewRepository studentViewRepository;
    private final SettingViewRepository settingViewRepository;

    public AttendanceAnalyticsService(SessionViewRepository sessionViewRepository,
                                      AttendanceRecordViewRepository attendanceRecordViewRepository,
                                      StudentViewRepository studentViewRepository,
                                      SettingViewRepository settingViewRepository) {
        this.sessionViewRepository = sessionViewRepository;
        this.attendanceRecordViewRepository = attendanceRecordViewRepository;
        this.studentViewRepository = studentViewRepository;
        this.settingViewRepository = settingViewRepository;
    }

    public List<Map<String, Object>> attendanceByStudent(Long courseId) {
        List<SessionView> sessions = sessionViewRepository.findByCourseId(courseId);
        List<Long> sessionIds = sessions.stream().map(SessionView::getId).toList();
        int totalSessions = sessions.size();

        List<AttendanceRecordView> records = sessionIds.isEmpty()
                ? List.of()
                : attendanceRecordViewRepository.findBySessionIdIn(sessionIds);

        Map<Long, Long> countByStudent = records.stream()
                .collect(Collectors.groupingBy(AttendanceRecordView::getStudentId, Collectors.counting()));

        List<StudentView> enrolled = studentViewRepository.findByCourseId(courseId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (StudentView student : enrolled) {
            long attended = countByStudent.getOrDefault(student.getId(), 0L);
            double percentage = totalSessions == 0 ? 0.0 : Math.round((attended * 1000.0) / totalSessions) / 10.0;

            Map<String, Object> row = new HashMap<>();
            row.put("studentId", student.getId());
            row.put("firstName", student.getFirstName());
            row.put("lastName", student.getLastName());
            row.put("attended", attended);
            row.put("totalSessions", totalSessions);
            row.put("percentage", percentage);
            result.add(row);
        }

        result.sort((a, b) -> ((Long) b.get("attended")).compareTo((Long) a.get("attended")));
        return result;
    }

    public List<Map<String, Object>> attendanceBySession(Long courseId) {
        List<SessionView> sessions = sessionViewRepository.findByCourseId(courseId);
        long totalEnrolled = studentViewRepository.findByCourseId(courseId).size();

        List<Map<String, Object>> result = new ArrayList<>();
        for (SessionView session : sessions) {
            long presentCount = attendanceRecordViewRepository.findBySessionIdIn(List.of(session.getId())).size();
            double percentage = totalEnrolled == 0 ? 0.0 : Math.round((presentCount * 1000.0) / totalEnrolled) / 10.0;

            Map<String, Object> row = new HashMap<>();
            row.put("sessionId", session.getId());
            row.put("date", session.getDate());
            row.put("status", session.getStatus());
            row.put("presentCount", presentCount);
            row.put("totalEnrolled", totalEnrolled);
            row.put("attendancePercentage", percentage);
            result.add(row);
        }

        result.sort(Comparator.comparing(r -> r.get("date").toString()));
        return result;
    }

    public Map<String, Object> belowThreshold(Long courseId) {
        double threshold = settingViewRepository.findById(THRESHOLD_KEY)
                .map(s -> Double.parseDouble(s.getValue()))
                .orElse(DEFAULT_THRESHOLD);

        List<Map<String, Object>> allStudents = attendanceByStudent(courseId);
        List<Map<String, Object>> below = allStudents.stream()
                .filter(s -> ((Double) s.get("percentage")) < threshold)
                .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("threshold", threshold);
        result.put("students", below);
        return result;
    }

    public List<Map<String, Object>> groupComparison(Long courseId) {
        List<SessionView> sessions = sessionViewRepository.findByCourseId(courseId);
        List<StudentView> enrolled = studentViewRepository.findByCourseId(courseId);

        Map<String, List<Double>> percentagesByGroup = new HashMap<>();

        for (SessionView session : sessions) {
            String category = GroupRules.category(session.getActivityType());

            Set<Long> presentIds = attendanceRecordViewRepository.findBySessionIdIn(List.of(session.getId()))
                    .stream().map(AttendanceRecordView::getStudentId).collect(Collectors.toSet());

            Map<String, int[]> groupCounts = new HashMap<>();

            for (StudentView student : enrolled) {
                int idx = GroupRules.parseIndexNumber(student.getStudentNumber());
                if (idx < 0) continue;
                String group = GroupRules.findGroup(idx, category);
                if (group == null) continue;

                groupCounts.putIfAbsent(group, new int[2]);
                groupCounts.get(group)[1]++;
                if (presentIds.contains(student.getId())) {
                    groupCounts.get(group)[0]++;
                }
            }

            for (Map.Entry<String, int[]> e : groupCounts.entrySet()) {
                double pct = e.getValue()[1] == 0 ? 0.0 : (e.getValue()[0] * 100.0) / e.getValue()[1];
                percentagesByGroup.computeIfAbsent(e.getKey(), k -> new ArrayList<>()).add(pct);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<Double>> e : percentagesByGroup.entrySet()) {
            double avg = e.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            Map<String, Object> row = new HashMap<>();
            row.put("group", e.getKey());
            row.put("averageAttendancePercentage", Math.round(avg * 10) / 10.0);
            row.put("sessionsCounted", e.getValue().size());
            result.add(row);
        }

        result.sort(Comparator.comparing(r -> (String) r.get("group")));
        return result;
    }
}