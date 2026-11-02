package com.evidencija.evidencija_backend.dto;

public class CourseSummaryDto {
    private Long id;
    private String code;
    private String name;
    private String studyProgram;
    private String studyType;
    private Integer studyYear;
    private Integer semester;
    private String teachingForm;
    private Integer groupNumber;
    private int studentCount;
    private Integer academicYear;

    public CourseSummaryDto(Long id, String code, String name, String studyProgram, String studyType,
                            Integer studyYear, Integer semester, String teachingForm, Integer groupNumber,
                            int studentCount, Integer academicYear) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.studyProgram = studyProgram;
        this.studyType = studyType;
        this.studyYear = studyYear;
        this.semester = semester;
        this.teachingForm = teachingForm;
        this.groupNumber = groupNumber;
        this.studentCount = studentCount;
        this.academicYear = academicYear;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getStudyProgram() { return studyProgram; }
    public String getStudyType() { return studyType; }
    public Integer getStudyYear() { return studyYear; }
    public Integer getSemester() { return semester; }
    public String getTeachingForm() { return teachingForm; }
    public Integer getGroupNumber() { return groupNumber; }
    public int getStudentCount() { return studentCount; }
    public Integer getAcademicYear() { return academicYear; }
}