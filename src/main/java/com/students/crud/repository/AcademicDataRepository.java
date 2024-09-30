package com.students.crud.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.students.crud.dto.AcademicData;

import java.util.List;

@Repository
public interface AcademicDataRepository extends JpaRepository<AcademicData, Long> {
    List<AcademicData> findByRegistrationNoAndYear(String registrationNo, String year);
}
