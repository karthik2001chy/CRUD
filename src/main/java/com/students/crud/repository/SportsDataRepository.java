package com.students.crud.repository;

import com.students.crud.dto.SportsData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SportsDataRepository extends JpaRepository<SportsData, Long> {
    List<SportsData> findByRegistrationNoAndYear(String registrationNo, String year);
}