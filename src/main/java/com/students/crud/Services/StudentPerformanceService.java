package com.students.crud.Services;

import com.students.crud.dto.AcademicData;
import com.students.crud.dto.ExtracurricularActivityData;
import com.students.crud.dto.SportsData;
import com.students.crud.dto.StudentPerformance;
import com.students.crud.repository.AcademicDataRepository;
import com.students.crud.repository.ExtracurricularActivityDataRepository;
import com.students.crud.repository.SportsDataRepository;
import com.students.crud.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentPerformanceService {

    @Autowired
    private AcademicDataRepository academicDataRepository;

    @Autowired
    private SportsDataRepository sportsDataRepository;

    @Autowired
    private ExtracurricularActivityDataRepository extracurricularActivityDataRepository;

    @Autowired
    private StudentRepository studentRepository;

    public StudentPerformance getStudentPerformance(String registrationNo, String year) {
        List<AcademicData> academicData = academicDataRepository.findByRegistrationNoAndYear(registrationNo, year);
        List<SportsData> sportsData = sportsDataRepository.findByRegistrationNoAndYear(registrationNo, year);
        List<ExtracurricularActivityData> extracurricularData = extracurricularActivityDataRepository.findByRegistrationNoAndYear(registrationNo, year);
        return new StudentPerformance(registrationNo, year, academicData, sportsData, extracurricularData);
    }
}
