package com.students.crud.Services;

import com.students.crud.DAO.Student;
import com.students.crud.DTO.StudentSimpleDto;
import com.students.crud.mapper.StudentMapper;
import com.students.crud.repository.StudentRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;

    public StudentService(StudentRepository studentRepository, StudentMapper studentMapper) {
        this.studentRepository = studentRepository;
        this.studentMapper = studentMapper;
    }

    public Student registerStudent(Student student) {
        return studentRepository.save(student);
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Page<Student> getStudents(Pageable pageable) {
        return studentRepository.findAll(pageable);
    }

    public Optional<Student> getStudentByRegistrationNo(String registrationNo) {
        return studentRepository.findById(registrationNo);
    }

    // Search students by name
    public List<Student> searchStudentsByName(String name) {
        List<Student> allStudents = studentRepository.findAll();
        return allStudents.stream()
                .filter(student -> student.getName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());
    }

    // Search students by name and return DTO
    public List<StudentSimpleDto> searchStudentsByNameSimple(String name) {
        return searchStudentsByName(name).stream()
                .map(studentMapper::toSimpleDto)
                .collect(Collectors.toList());
    }

    // Return a Page of DTOs; cache per page/size/sort
    @Cacheable(value = "studentSimple", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()")
    public Page<StudentSimpleDto> getStudentsSimple(Pageable pageable) {
        return studentRepository.findAll(pageable).map(studentMapper::toSimpleDto);
    }

    public void deleteStudent(String registrationNo) {
        studentRepository.deleteById(registrationNo);
    }

    // Cache the full simple list in Redis under cache name 'studentSimpleList'
    @Cacheable(value = "studentSimpleList")
    public List<StudentSimpleDto> getAllSimple() {
        return studentMapper.toSimpleDtoList(studentRepository.findAll());
    }

    public Optional<StudentSimpleDto> getSimpleByRegistrationNo(String registrationNo) {
        return studentRepository.findById(registrationNo).map(studentMapper::toSimpleDto);
    }
}


