package com.students.crud.Services;

import com.students.crud.dto.AcademicData;
import com.students.crud.repository.AcademicDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AcademicDataService {

    @Autowired
    private AcademicDataRepository academicDataRepository;

    public AcademicData saveAcademicData(AcademicData academicData) {
        return academicDataRepository.save(academicData);
    }

    public Optional<AcademicData> getAcademicDataById(Long id) {
        return academicDataRepository.findById(id);
    }

    public List<AcademicData> getAllAcademicData() {
        return academicDataRepository.findAll();
    }

    public void deleteAcademicData(Long id) {
        academicDataRepository.deleteById(id);
    }
}
