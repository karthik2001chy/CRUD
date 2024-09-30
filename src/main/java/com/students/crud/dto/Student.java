package com.students.crud.dto;

import jakarta.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @Column(nullable = false, unique = true)
    private String registrationNo;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private Date dateOfBirth;

    @Column(nullable = false)
    private String fatherName;

    @Column(nullable = false)
    private String fatherContactNo;

    @Column(nullable = false)
    private String motherName;

    @Column(nullable = false)
    private String motherContactNo;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AcademicData> academicData;

    // Getters and setters
    public String getRegistrationNo() {
        return registrationNo;
    }

    public void setRegistrationNo(String registrationNo) {
        this.registrationNo = registrationNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public String getFatherContactNo() {
        return fatherContactNo;
    }

    public void setFatherContactNo(String fatherContactNo) {
        this.fatherContactNo = fatherContactNo;
    }

    public String getMotherName() {
        return motherName;
    }

    public void setMotherName(String motherName) {
        this.motherName = motherName;
    }

    public String getMotherContactNo() {
        return motherContactNo;
    }

    public void setMotherContactNo(String motherContactNo) {
        this.motherContactNo = motherContactNo;
    }

    public List<AcademicData> getAcademicData() {
        return academicData;
    }

    public void setAcademicData(List<AcademicData> academicData) {
        this.academicData = academicData;
    }
}
