package com.students.crud.controllers;

import com.students.crud.Services.StudentPerformanceService;
import com.students.crud.Services.StudentService;
import com.students.crud.dto.Student;
import com.students.crud.dto.StudentPerformance;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentPerformanceService studentPerformanceService;

    @GetMapping("/{registrationNo}/{year}")
    public ResponseEntity<Object> getStudentPerformance(@PathVariable String registrationNo, @PathVariable String year) {
        StudentPerformance performance = studentPerformanceService.getStudentPerformance(registrationNo, year);
        return new ResponseEntity<>(performance, setHeader(), HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<Student> registerStudent(@RequestBody Student student) {
        Student savedStudent = studentService.registerStudent(student);
        return ResponseEntity.ok(savedStudent);
    }

    @GetMapping("/{registrationNo}")
    public ResponseEntity<Student> getStudentByRegistrationNo(@PathVariable String registrationNo) {
        Optional<Student> student = studentService.getStudentByRegistrationNo(registrationNo);
        return student.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Object> getAllStudents() {
        List<Student> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
    }

    @DeleteMapping(value = "/{registrationNo}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> deleteStudent(@PathVariable String registrationNo) {
        studentService.deleteStudent(registrationNo);
        Map<String, String> response = new HashMap<>();
        response.put("Response", "Deleted Successfully");
        return ResponseEntity.ok(response);
    }

    private HttpHeaders setHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Disposition", "attachment; filename=response.json");
        return headers;
    }
}
