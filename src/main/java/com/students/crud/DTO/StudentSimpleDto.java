package com.students.crud.DTO;

public class StudentSimpleDto {
    private String registrationNo;
    private String name;
    private String fatherName;
    private String motherName;

    public StudentSimpleDto() {}

    public StudentSimpleDto(String registrationNo, String name, String fatherName, String motherName) {
        this.registrationNo = registrationNo;
        this.name = name;
        this.fatherName = fatherName;
        this.motherName = motherName;
    }

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

    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public String getMotherName() {
        return motherName;
    }

    public void setMotherName(String motherName) {
        this.motherName = motherName;
    }
}
