package com.students.crud.Services;

import com.students.crud.dto.Student;
import com.students.crud.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    public Student registerStudent(Student student) {
        return studentRepository.save(student);
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Optional<Student> getStudentByRegistrationNo(String registrationNo) {
        return studentRepository.findById(registrationNo);
    }

    public void deleteStudent(String registrationNo) {
        studentRepository.deleteById(registrationNo);
    }
}
