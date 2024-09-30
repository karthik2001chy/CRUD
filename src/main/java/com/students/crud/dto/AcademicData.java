package com.students.crud.dto;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "academic_data")
public class AcademicData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "registration_no")
    private String registrationNo;

    @Column(name = "student_name")
    private String studentName;

    @Column(name = "course")
    private String course;

    @Column(name = "year_of_study")
    private Integer yearOfStudy;

    @Column(name = "date_of_enrollment")
    @Temporal(TemporalType.DATE)
    private Date dateOfEnrollment;

    // Example of another field referencing the same column
    @Column(name = "registration_no", insertable = false, updatable = false)
    private String duplicateRegistrationNo;

    // Default constructor
    public AcademicData() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRegistrationNo() {
        return registrationNo;
    }

    public void setRegistrationNo(String registrationNo) {
        this.registrationNo = registrationNo;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public Integer getYearOfStudy() {
        return yearOfStudy;
    }

    public void setYearOfStudy(Integer yearOfStudy) {
        this.yearOfStudy = yearOfStudy;
    }

    public Date getDateOfEnrollment() {
        return dateOfEnrollment;
    }

    public void setDateOfEnrollment(Date dateOfEnrollment) {
        this.dateOfEnrollment = dateOfEnrollment;
    }

    public String getDuplicateRegistrationNo() {
        return duplicateRegistrationNo;
    }

    public void setDuplicateRegistrationNo(String duplicateRegistrationNo) {
        this.duplicateRegistrationNo = duplicateRegistrationNo;
    }
}
