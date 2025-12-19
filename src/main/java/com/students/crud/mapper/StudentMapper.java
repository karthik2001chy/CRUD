package com.students.crud.mapper;

import com.students.crud.DAO.Student;
import com.students.crud.DTO.StudentSimpleDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class StudentMapper {

    public StudentSimpleDto toSimpleDto(Student s) {
        if (s == null) return null;
        return new StudentSimpleDto(s.getRegistrationNo(), s.getName(), s.getFatherName(), s.getMotherName());
    }

    public List<StudentSimpleDto> toSimpleDtoList(List<Student> students) {
        return students.stream()
                .map(this::toSimpleDto)
                .collect(Collectors.toList());
    }
}
