package com.students.crud.repository;


import com.students.crud.dto.ExtracurricularActivityData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExtracurricularActivityDataRepository extends JpaRepository<ExtracurricularActivityData, Long> {
    List<ExtracurricularActivityData> findByRegistrationNoAndYear(String registrationNo, String year);
}