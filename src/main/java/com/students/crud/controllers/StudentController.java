package com.students.crud.controllers;

import com.students.crud.DTO.StudentSimpleDto;
import com.students.crud.Services.StudentService;
import com.students.crud.DAO.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public ResponseEntity<Page<Student>> listStudents(
            @PageableDefault(size = 20, sort = "registrationNo") Pageable pageable) {
        Page<Student> page = studentService.getStudents(pageable);
        return ResponseEntity.ok(page);
    }

    // Return a Page of DTOs so clients get pagination metadata as well
    @GetMapping("/dto")
    public ResponseEntity<Page<StudentSimpleDto>> listStudentsDto(
            @PageableDefault(size = 20, sort = "registrationNo") Pageable pageable) {

        Page<StudentSimpleDto> page = studentService.getStudentsSimple(pageable);

        return ResponseEntity.ok(page);
    }

}
