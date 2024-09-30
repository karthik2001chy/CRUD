package com.students.crud.dto;

import java.util.List;

public class StudentPerformance {

    private String registrationNo;
    private String year;
    private List<AcademicData> academicData;
    private List<SportsData> sportsData;
    private List<ExtracurricularActivityData> extracurricularActivityData;

    public StudentPerformance(String registrationNo, String year, List<AcademicData> academicData, List<SportsData> sportsData, List<ExtracurricularActivityData> extracurricularActivityData) {
        this.registrationNo = registrationNo;
        this.year = year;
        this.academicData = academicData;
        this.sportsData = sportsData;
        this.extracurricularActivityData = extracurricularActivityData;
    }

    // Getters and setters


    public String getRegistrationNo() {
        return registrationNo;
    }

    public void setRegistrationNo(String registrationNo) {
        this.registrationNo = registrationNo;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public List<SportsData> getSportsData() {
        return sportsData;
    }

    public void setSportsData(List<SportsData> sportsData) {
        this.sportsData = sportsData;
    }

    public List<AcademicData> getAcademicData() {
        return academicData;
    }

    public void setAcademicData(List<AcademicData> academicData) {
        this.academicData = academicData;
    }

}