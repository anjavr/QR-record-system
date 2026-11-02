package com.evidencija.analytics_service.model;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class StudentView {

    @Id
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "student_number")
    private String studentNumber;

    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getStudentNumber() { return studentNumber; }
}