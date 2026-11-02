package com.evidencija.evidencija_backend.service;

import com.evidencija.evidencija_backend.dto.AttendanceRecordDto;
import com.evidencija.evidencija_backend.dto.CreateSessionRequest;
import com.evidencija.evidencija_backend.model.Course;
import com.evidencija.evidencija_backend.model.Employee;
import com.evidencija.evidencija_backend.model.Session;
import com.evidencija.evidencija_backend.repository.AttendanceRecordRepository;
import com.evidencija.evidencija_backend.repository.CourseRepository;
import com.evidencija.evidencija_backend.repository.SessionRepository;
import com.evidencija.evidencija_backend.repository.SettingRepository;
import com.evidencija.evidencija_backend.model.AttendanceRecord;
import com.evidencija.evidencija_backend.model.Student;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final CourseRepository courseRepository;
    private final SettingRepository settingRepository;
    private final QrTokenService qrTokenService;
    private final QrCodeService qrCodeService;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final StudentService studentService;

    private static final String QR_INTERVAL_KEY = "qr.refresh.interval.seconds";
    private static final long DEFAULT_QR_INTERVAL = 15L;


    public SessionService(SessionRepository sessionRepository, CourseRepository courseRepository, SettingRepository settingRepository, QrTokenService qrTokenService, QrCodeService qrCodeService, AttendanceRecordRepository attendanceRecordRepository, StudentService studentService) {
        this.sessionRepository = sessionRepository;
        this.courseRepository = courseRepository;
        this.settingRepository = settingRepository;
        this.qrTokenService = qrTokenService;
        this.qrCodeService = qrCodeService;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.studentService = studentService;
    }

    // CRUD operations for sessions (admin)
    public Session create(CreateSessionRequest request, Employee employee) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        String category = GroupRulesService.category(request.getActivityType().name());
        if (!GroupRulesService.labelsForCategory(category).contains(request.getGroupLabel())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid group label for this activity type: " + request.getGroupLabel());
        }

        Session session = new Session();
        session.setCourse(course);
        session.setEmployee(employee);
        session.setActivityType(request.getActivityType());
        session.setDate(request.getDate());
        session.setStartTime(request.getStartTime());
        session.setGroupLabel(request.getGroupLabel());
        session.setStatus(Session.SessionStatus.CREATED);

        return sessionRepository.save(session);
    }

    //read my sessions
    public List<Session> mySessions(Employee employee) {
        return sessionRepository.findByEmployee(employee);
    }

    //read session by id
    public Session findById(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));
    }

    public Session findByIdVerified(Long sessionId, Employee employee) {
        return findAndVerifyOwnership(sessionId, employee);
    }

    public List<Session> findSessionsByCourse(Long courseId) {
        return sessionRepository.findByCourseId(courseId);
    }

    // lifecycle of a session (admin)
    public Session activate(Long sessionId, Employee employee) {
        Session session = findAndVerifyOwnership(sessionId, employee);

        if (session.getStatus() != Session.SessionStatus.CREATED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Session must be in CREATED status to be activated");
        }

        List<Session> otherActive = sessionRepository.findByEmployeeAndStatus(employee, Session.SessionStatus.ACTIVE);
        if (!otherActive.isEmpty()) {
            Session activeOne = otherActive.get(0);
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "You already have an active session (course: " + activeOne.getCourse().getName()
                            + ", started " + activeOne.getDate() + " " + activeOne.getStartTime()
                            + "). Close it before activating another.");
        }

        session.setStatus(Session.SessionStatus.ACTIVE);
        refreshQrToken(session);
        return sessionRepository.save(session);
    }

    public Session close(Long sessionId, Employee employee) {
        Session session = findAndVerifyOwnership(sessionId, employee);

        if (session.getStatus() != Session.SessionStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Session must be ACTIVE to be closed");
        }

        session.setStatus(Session.SessionStatus.CLOSED);
        return sessionRepository.save(session);
    }

    // qr code admin operations
    //returns base64 image of an qr code
    public String getCurrentQrCodeImage(Long sessionId, Employee employee) {
        Session session = findAndVerifyOwnership(sessionId, employee);

        if (session.getStatus() != Session.SessionStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Session is not active");
        }

        if (session.getCurrentQrToken() == null || LocalDateTime.now().isAfter(session.getQrTokenExpiresAt())) {
            refreshQrToken(session);
        }

        return qrCodeService.generateQrCodeBase64(session.getCurrentQrToken());
    }

    //generating qr token
    public Session refreshQrToken(Session session) {
        long interval = getQrRefreshInterval();
        String token = qrTokenService.generateToken(session.getId(), interval);
        session.setCurrentQrToken(token);
        session.setQrTokenExpiresAt(LocalDateTime.now().plusSeconds(interval));
        return sessionRepository.save(session);
    }

    // reads configurable interval from settings
    private long getQrRefreshInterval() {
        return settingRepository.findById(QR_INTERVAL_KEY)
                .map(s -> Long.parseLong(s.getValue()))
                .orElse(DEFAULT_QR_INTERVAL);
    }

    // attendance records (admin)
    //read all
    public List<AttendanceRecord> getAttendanceForSession(Long sessionId, Employee employee) {
        Session session = findAndVerifyOwnership(sessionId, employee);
        return attendanceRecordRepository.findBySessionId(session.getId());
    }

    //delete student check in
    public void deleteAttendanceRecord(Long sessionId, Long recordId, Employee employee) {
        Session session = findAndVerifyOwnership(sessionId, employee);

        AttendanceRecord record = attendanceRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attendance record not found"));

        if (!record.getSession().getId().equals(session.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Record does not belong to this session");
        }

        attendanceRecordRepository.delete(record);
    }

    public List<AttendanceRecordDto> getAttendanceForSessionWithFlags(Long sessionId, Employee employee) {
        List<AttendanceRecord> records = getAttendanceForSession(sessionId, employee);

        Map<String, Long> ipCounts = records.stream()
                .collect(Collectors.groupingBy(AttendanceRecord::getIpAddress, Collectors.counting()));

        return records.stream()
                .map(r -> new AttendanceRecordDto(r, ipCounts.getOrDefault(r.getIpAddress(), 0L) > 1))
                .toList();
    }

    // student method for checking in into session
    public AttendanceRecord checkIn(String qrToken, String studentEmail, String ipAddress, String userAgent) {
        Long sessionId;
        try {
            sessionId = qrTokenService.validateAndExtractSessionId(qrToken);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "QR code has expired. Please scan the current code.");
        } catch (io.jsonwebtoken.JwtException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid QR code token.");
        }

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        if (session.getStatus() != Session.SessionStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Session is not active");
        }

        if (!qrToken.equals(session.getCurrentQrToken())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "QR code has expired or been refreshed. Please scan the current code.");
        }

        Student student = studentService.findByEmail(studentEmail);

        boolean belongsToCourse = student.getCourses().stream()
                .anyMatch(c -> c.getId().equals(session.getCourse().getId()));
        if (!belongsToCourse) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student is not enrolled in this course");
        }

        String category = GroupRulesService.category(session.getActivityType().name());
        int idx = GroupRulesService.parseIndexNumber(student.getStudentNumber());
        String studentGroup = GroupRulesService.findGroup(idx, category);
        if (studentGroup == null || !studentGroup.equals(session.getGroupLabel())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Student does not belong to the group assigned to this session (" + session.getGroupLabel() + ")");
        }

        if (attendanceRecordRepository.findBySessionAndStudent(session, student).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Attendance already recorded for this session");
        }

        AttendanceRecord record = new AttendanceRecord();
        record.setSession(session);
        record.setStudent(student);
        record.setCheckInTime(java.time.LocalDateTime.now());
        record.setIpAddress(ipAddress);
        record.setUserAgent(userAgent);

        return attendanceRecordRepository.save(record);
    }

    // private method for checking ownership of an session
    private Session findAndVerifyOwnership(Long sessionId, Employee employee) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        if (!session.getEmployee().getId().equals(employee.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this session");
        }

        return session;
    }
}