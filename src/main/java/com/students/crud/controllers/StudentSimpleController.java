package com.students.crud.controllers;

import com.students.crud.Services.StudentService;
import com.students.crud.DTO.StudentSimpleDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/students/simple")
@CrossOrigin(origins = "*")
public class StudentSimpleController {

    private final StudentService studentService;

    public StudentSimpleController(StudentService studentService) {
        this.studentService = studentService;
    }

    // Normal GET - returns all students (no pagination)
    @GetMapping
    public ResponseEntity<List<StudentSimpleDto>> getAllStudents() {
        List<StudentSimpleDto> students = studentService.getAllSimple();
        return ResponseEntity.ok(students);
    }

    // Normal GET by id
    @GetMapping("/{registrationNo}")
    public ResponseEntity<StudentSimpleDto> getStudentById(@PathVariable String registrationNo) {
        Optional<StudentSimpleDto> student = studentService.getSimpleByRegistrationNo(registrationNo);
        return student.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
