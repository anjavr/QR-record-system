package com.evidencija.evidencija_backend.service;

import com.evidencija.evidencija_backend.model.*;
import com.evidencija.evidencija_backend.repository.AttendanceRecordRepository;
import com.evidencija.evidencija_backend.repository.SessionRepository;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

@Service
public class ExportService {

    private final SessionRepository sessionRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy.");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final Font pdfTitleFont;
    private final Font pdfNormalFont;
    private final Font pdfBoldFont;

    public ExportService(SessionRepository sessionRepository, AttendanceRecordRepository attendanceRecordRepository) throws Exception {
        this.sessionRepository = sessionRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;

        BaseFont unicodeBaseFont = loadUnicodeBaseFont();
        this.pdfTitleFont = new Font(unicodeBaseFont, 14, Font.BOLD);
        this.pdfNormalFont = new Font(unicodeBaseFont, 10, Font.NORMAL);
        this.pdfBoldFont = new Font(unicodeBaseFont, 10, Font.BOLD);
    }

    private BaseFont loadUnicodeBaseFont() throws Exception {
        ClassPathResource resource = new ClassPathResource("fonts/NotoSans-Regular.ttf");
        try (InputStream is = resource.getInputStream()) {
            byte[] fontBytes = is.readAllBytes();
            return BaseFont.createFont("NotoSans-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, fontBytes, null);
        }
    }

    // ---------- Excel: pojedinačna sesija ----------

    public byte[] exportSessionToExcel(Session session) throws Exception {
        List<AttendanceRecord> records = attendanceRecordRepository.findBySessionId(session.getId());
        Course course = session.getCourse();

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Evidencija");

            int rowIdx = 0;
            rowIdx = writeSessionHeader(sheet, rowIdx, course, session);

            rowIdx++;
            Row headerRow = sheet.createRow(rowIdx++);
            String[] headers = {"Ime", "Prezime", "Broj indeksa", "Email", "Vrijeme prijave"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            for (AttendanceRecord record : records) {
                Row row = sheet.createRow(rowIdx++);
                Student student = record.getStudent();
                row.createCell(0).setCellValue(student.getFirstName());
                row.createCell(1).setCellValue(student.getLastName());
                row.createCell(2).setCellValue(student.getStudentNumber());
                row.createCell(3).setCellValue(student.getEmail());
                row.createCell(4).setCellValue(record.getCheckInTime().format(TIME_FORMATTER));
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private int writeSessionHeader(Sheet sheet, int rowIdx, Course course, Session session) {
        rowIdx = writeInfoRow(sheet, rowIdx, "Predmet", course.getName());
        rowIdx = writeInfoRow(sheet, rowIdx, "Šifra", course.getCode());
        rowIdx = writeInfoRow(sheet, rowIdx, "Studijski program", course.getStudyProgram());
        rowIdx = writeInfoRow(sheet, rowIdx, "Semestar", String.valueOf(course.getSemester()));
        rowIdx = writeInfoRow(sheet, rowIdx, "Godina studija", String.valueOf(course.getStudyYear()));
        rowIdx = writeInfoRow(sheet, rowIdx, "Oblik nastave", course.getTeachingForm());
        rowIdx = writeInfoRow(sheet, rowIdx, "Grupa", String.valueOf(course.getGroupNumber()));
        rowIdx = writeInfoRow(sheet, rowIdx, "Nastavnik/asistent",
                course.getEmployee().getFirstName() + " " + course.getEmployee().getLastName());
        rowIdx = writeInfoRow(sheet, rowIdx, "Datum sesije", session.getDate().format(DATE_FORMATTER));
        return rowIdx;
    }

    private int writeInfoRow(Sheet sheet, int rowIdx, String label, String value) {
        Row row = sheet.createRow(rowIdx);
        row.createCell(0).setCellValue(label + ":");
        row.createCell(1).setCellValue(value != null ? value : "");
        return rowIdx + 1;
    }

    // ---------- Excel: kumulativna evidencija po predmetu ----------

    public byte[] exportCourseToExcel(Course course) throws Exception {
        List<Session> sessions = sessionRepository.findByCourseId(course.getId());
        int totalSessions = sessions.size();

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Kumulativna evidencija");

            int rowIdx = 0;
            rowIdx = writeInfoRow(sheet, rowIdx, "Predmet", course.getName());
            rowIdx = writeInfoRow(sheet, rowIdx, "Šifra", course.getCode());
            rowIdx = writeInfoRow(sheet, rowIdx, "Studijski program", course.getStudyProgram());
            rowIdx = writeInfoRow(sheet, rowIdx, "Semestar", String.valueOf(course.getSemester()));
            rowIdx = writeInfoRow(sheet, rowIdx, "Godina studija", String.valueOf(course.getStudyYear()));
            rowIdx = writeInfoRow(sheet, rowIdx, "Oblik nastave", course.getTeachingForm());
            rowIdx = writeInfoRow(sheet, rowIdx, "Grupa", String.valueOf(course.getGroupNumber()));
            rowIdx = writeInfoRow(sheet, rowIdx, "Nastavnik/asistent",
                    course.getEmployee().getFirstName() + " " + course.getEmployee().getLastName());
            rowIdx = writeInfoRow(sheet, rowIdx, "Ukupno sesija", String.valueOf(totalSessions));

            rowIdx++;
            Row headerRow = sheet.createRow(rowIdx++);
            String[] headers = {"Ime", "Prezime", "Broj indeksa", "Email", "% prisustva", "Dolazaka", "Izostanaka"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            List<Map<String, Object>> stats = computeAttendanceStats(course, sessions, totalSessions);

            for (Map<String, Object> stat : stats) {
                Row row = sheet.createRow(rowIdx++);
                Student student = (Student) stat.get("student");
                row.createCell(0).setCellValue(student.getFirstName());
                row.createCell(1).setCellValue(student.getLastName());
                row.createCell(2).setCellValue(student.getStudentNumber());
                row.createCell(3).setCellValue(student.getEmail());
                row.createCell(4).setCellValue((Double) stat.get("percentage"));
                row.createCell(5).setCellValue((Integer) stat.get("attended"));
                row.createCell(6).setCellValue((Integer) stat.get("absent"));
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private List<Map<String, Object>> computeAttendanceStats(Course course, List<Session> sessions, int totalSessions) {
        List<Map<String, Object>> stats = new ArrayList<>();

        for (Student student : course.getStudents()) {
            int attended = 0;
            for (Session session : sessions) {
                boolean present = attendanceRecordRepository.findBySessionAndStudent(session, student).isPresent();
                if (present) attended++;
            }
            int absent = totalSessions - attended;
            double percentage = totalSessions == 0 ? 0.0 : Math.round((attended * 1000.0) / totalSessions) / 10.0;

            Map<String, Object> stat = new HashMap<>();
            stat.put("student", student);
            stat.put("attended", attended);
            stat.put("absent", absent);
            stat.put("percentage", percentage);
            stats.add(stat);
        }

        stats.sort((a, b) -> ((Integer) b.get("attended")).compareTo((Integer) a.get("attended")));
        return stats;
    }

    // ---------- PDF: pojedinačna sesija ----------

    public byte[] exportSessionToPdf(Session session) throws Exception {
        List<AttendanceRecord> records = attendanceRecordRepository.findBySessionId(session.getId());
        Course course = session.getCourse();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, out);
        document.open();

        document.add(new Paragraph(course.getName(), pdfTitleFont));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Šifra: " + course.getCode(), pdfNormalFont));
        document.add(new Paragraph("Studijski program: " + course.getStudyProgram(), pdfNormalFont));
        document.add(new Paragraph("Semestar: " + course.getSemester() + ", Godina studija: " + course.getStudyYear(), pdfNormalFont));
        document.add(new Paragraph("Oblik nastave: " + course.getTeachingForm() + ", Grupa: " + course.getGroupNumber(), pdfNormalFont));
        document.add(new Paragraph("Nastavnik/asistent: " + course.getEmployee().getFirstName() + " " + course.getEmployee().getLastName(), pdfNormalFont));
        document.add(new Paragraph("Datum sesije: " + session.getDate().format(DATE_FORMATTER), pdfNormalFont));
        document.add(new Paragraph(" "));

        com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(5);
        table.setWidthPercentage(100);
        addTableHeader(table, "Ime", "Prezime", "Broj indeksa", "Email", "Vrijeme prijave");

        for (AttendanceRecord record : records) {
            Student student = record.getStudent();
            addCell(table, student.getFirstName());
            addCell(table, student.getLastName());
            addCell(table, student.getStudentNumber());
            addCell(table, student.getEmail());
            addCell(table, record.getCheckInTime().format(TIME_FORMATTER));
        }

        document.add(table);
        document.close();

        return out.toByteArray();
    }

    // ---------- PDF: kumulativna evidencija po predmetu ----------

    public byte[] exportCourseToPdf(Course course) throws Exception {
        List<Session> sessions = sessionRepository.findByCourseId(course.getId());
        int totalSessions = sessions.size();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, out);
        document.open();

        document.add(new Paragraph(course.getName(), pdfTitleFont));
        document.add(new Paragraph("Kumulativna evidencija prisustva", pdfBoldFont));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Šifra: " + course.getCode(), pdfNormalFont));
        document.add(new Paragraph("Studijski program: " + course.getStudyProgram(), pdfNormalFont));
        document.add(new Paragraph("Semestar: " + course.getSemester() + ", Godina studija: " + course.getStudyYear(), pdfNormalFont));
        document.add(new Paragraph("Ukupno sesija: " + totalSessions, pdfNormalFont));
        document.add(new Paragraph(" "));

        com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(6);
        table.setWidthPercentage(100);
        addTableHeader(table, "Ime", "Prezime", "Broj indeksa", "% prisustva", "Dolazaka", "Izostanaka");

        List<Map<String, Object>> stats = computeAttendanceStats(course, sessions, totalSessions);

        for (Map<String, Object> stat : stats) {
            Student student = (Student) stat.get("student");
            addCell(table, student.getFirstName());
            addCell(table, student.getLastName());
            addCell(table, student.getStudentNumber());
            addCell(table, stat.get("percentage") + "%");
            addCell(table, String.valueOf(stat.get("attended")));
            addCell(table, String.valueOf(stat.get("absent")));
        }

        document.add(table);
        document.close();

        return out.toByteArray();
    }

    private void addTableHeader(com.lowagie.text.pdf.PdfPTable table, String... headers) {
        for (String header : headers) {
            com.lowagie.text.pdf.PdfPCell cell = new com.lowagie.text.pdf.PdfPCell(new Phrase(header, pdfBoldFont));
            table.addCell(cell);
        }
    }

    private void addCell(com.lowagie.text.pdf.PdfPTable table, String value) {
        table.addCell(new Phrase(value, pdfNormalFont));
    }
}