package com.evidencija.evidencija_backend.service;

import com.evidencija.evidencija_backend.dto.UploadConflictDto;
import com.evidencija.evidencija_backend.dto.UploadResultDto;
import com.evidencija.evidencija_backend.exception.UploadConflictException;
import com.evidencija.evidencija_backend.model.Course;
import com.evidencija.evidencija_backend.model.Employee;
import com.evidencija.evidencija_backend.model.Session;
import com.evidencija.evidencija_backend.model.Student;
import com.evidencija.evidencija_backend.repository.AttendanceRecordRepository;
import com.evidencija.evidencija_backend.repository.CourseRepository;
import com.evidencija.evidencija_backend.repository.SessionRepository;
import com.evidencija.evidencija_backend.repository.StudentRepository;
import com.evidencija.evidencija_backend.service.util.CyrillicTransliterator;
import org.apache.poi.ss.usermodel.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class CourseUploadService {

    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final DomainValidator domainValidator;
    private final CyrillicTransliterator transliterator;
    private final SessionRepository sessionRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;

    public CourseUploadService(CourseRepository courseRepository, StudentRepository studentRepository,
                               DomainValidator domainValidator, CyrillicTransliterator transliterator, SessionRepository sessionRepository, AttendanceRecordRepository attendanceRecordRepository) {
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
        this.domainValidator = domainValidator;
        this.transliterator = transliterator;
        this.sessionRepository = sessionRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
    }

    public UploadResultDto processXlsx(MultipartFile file, Employee employee, Integer academicYear, boolean confirmReplace) {
        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            Course course = new Course();
            course.setEmployee(employee);
            course.setAcademicYear(academicYear);

            for (Row row : sheet) {
                for (Cell cell : row) {
                    String text = formatter.formatCellValue(cell);
                    if (text == null) continue;
                    text = text.trim();
                    int col = cell.getColumnIndex();
                    int nextRow = row.getRowNum() + 1;

                    switch (text) {
                        case "Предмет" -> course.setName(valueAt(sheet, formatter, nextRow, col));
                        case "Шифра" -> course.setCode(valueAt(sheet, formatter, nextRow, col));
                        case "Студијски програм" -> course.setStudyProgram(valueAt(sheet, formatter, nextRow, col));
                        case "Тип студија" -> course.setStudyType(valueAt(sheet, formatter, nextRow, col));
                        case "Година студија" -> course.setStudyYear(parseIntSafe(valueAt(sheet, formatter, nextRow, col)));
                        case "Семестар" -> course.setSemester(parseIntSafe(valueAt(sheet, formatter, nextRow, col)));
                        case "Облик наставе" -> course.setTeachingForm(valueAt(sheet, formatter, nextRow, col));
                        case "Група" -> course.setGroupNumber(parseIntSafe(valueAt(sheet, formatter, nextRow, col)));
                    }
                }
            }

            if (course.getCode() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not find course metadata in file");
            }

            // Provjera duplikata
            var existing = courseRepository.findByEmployeeAndCodeAndAcademicYearAndGroupNumber(
                    employee, course.getCode(), academicYear, course.getGroupNumber());

            if (existing.isPresent() && !confirmReplace) {
                Course old = existing.get();
                throw new UploadConflictException(new UploadConflictDto(
                        old.getId(), old.getName(), old.getStudents().size()));
            }

            if (existing.isPresent() && confirmReplace) {
                deleteExistingCourse(existing.get());
            }

            course = courseRepository.save(course);

            String domain = domainValidator.getCurrentDomain();
            int processed = 0, created = 0, linked = 0, updated = 0;

            int firstStudentRow = findFirstStudentRow(sheet, formatter);
            if (firstStudentRow == -1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not find student list in file");
            }

            for (int r = firstStudentRow; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) break;

                String lastNameRaw = formatter.formatCellValue(row.getCell(1));
                String firstNameRaw = formatter.formatCellValue(row.getCell(2));
                String indexRaw = formatter.formatCellValue(row.getCell(3));

                String lastName = lastNameRaw != null ? lastNameRaw.trim() : "";
                String firstName = firstNameRaw != null ? firstNameRaw.trim() : "";
                String indexNumber = indexRaw != null ? indexRaw.trim() : "";

                if (lastName.isEmpty() && firstName.isEmpty()) break;
                if (indexNumber.isEmpty()) continue;

                String email = generateEmail(firstName, lastName, domain);

                Student student = studentRepository.findByEmail(email).orElse(null);
                if (student == null) {
                    student = new Student();
                    student.setFirstName(firstName);
                    student.setLastName(lastName);
                    student.setStudentNumber(indexNumber);
                    student.setEmail(email);
                    student = studentRepository.save(student);
                    created++;
                } else {
                    boolean changed = false;
                    if (!firstName.equals(student.getFirstName())) {
                        student.setFirstName(firstName);
                        changed = true;
                    }
                    if (!lastName.equals(student.getLastName())) {
                        student.setLastName(lastName);
                        changed = true;
                    }
                    if (!indexNumber.equals(student.getStudentNumber())) {
                        student.setStudentNumber(indexNumber);
                        changed = true;
                    }
                    if (changed) {
                        studentRepository.save(student);
                        updated++;
                    }
                }

                course.getStudents().add(student);
                linked++;
                processed++;
            }
            courseRepository.save(course);
            return new UploadResultDto(course.getName(), processed, created, linked, updated);

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to read uploaded file: " + e.getMessage());
        }
    }

    private String valueAt(Sheet sheet, DataFormatter formatter, int rowNum, int col) {
        Row row = sheet.getRow(rowNum);
        if (row == null) return null;
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        String value = formatter.formatCellValue(cell);
        return value != null ? value.trim() : null;
    }

    private Integer parseIntSafe(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Integer.parseInt(value.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int findFirstStudentRow(Sheet sheet, DataFormatter formatter) {
        for (int r = 0; r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;
            Cell first = row.getCell(0);
            if (first == null) continue;
            String value = formatter.formatCellValue(first);
            if (value == null) continue;
            value = value.trim();
            if (value.matches("^\\d+\\.$")) {
                return r;
            }
        }
        return -1;
    }

    private String generateEmail(String firstName, String lastName, String domain) {
        String first = transliterator.toLatinAscii(firstName);
        String last = transliterator.toLatinAscii(lastName);
        return first + "." + last + "@" + domain;
    }

    private void deleteExistingCourse(Course oldCourse) {
        List<Session> oldSessions = sessionRepository.findByCourseId(oldCourse.getId());
        for (Session session : oldSessions) {
            attendanceRecordRepository.deleteAll(attendanceRecordRepository.findBySessionId(session.getId()));
        }
        sessionRepository.deleteAll(oldSessions);

        oldCourse.getStudents().clear();
        courseRepository.save(oldCourse);

        courseRepository.delete(oldCourse);
    }
}
