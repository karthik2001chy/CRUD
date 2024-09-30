package com.students.crud.dto;

import jakarta.persistence.*;

@Entity
@Table(name = "extracurricular_activity_data")
public class ExtracurricularActivityData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String grade;
    private String section;
    private String year;
    private String activityName;
  

    @ManyToOne
    @JoinColumn(name = "registrationNo", nullable = false)
    private Student student;

    // Getters and setters
    // ... (same as before, excluding registrationNo)

    

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getPerformance() {
        return performance;
    }

    public void setPerformance(String performance) {
        this.performance = performance;
    }

    private String performance;

}
