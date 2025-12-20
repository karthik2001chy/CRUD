package com.students.crud.controllers;

import com.students.crud.DTO.StudentSimpleDto;
import com.students.crud.Services.StudentService;
import com.students.crud.DAO.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    // Slice backed by cached list to avoid serializing PageImpl
    @GetMapping("/dto/slice")
    public ResponseEntity<org.springframework.data.domain.Slice<StudentSimpleDto>> listStudentsDtoSlice(
            @PageableDefault(size = 20, sort = "registrationNo") Pageable pageable) {

        org.springframework.data.domain.Slice<StudentSimpleDto> slice = studentService.getStudentsSimpleSlice(pageable);

        return ResponseEntity.ok(slice);
    }

    // Simple non-paginated endpoint returning only the 4-field DTOs
    @GetMapping("/simple")
    public ResponseEntity<List<StudentSimpleDto>> getAllSimple() {
        List<StudentSimpleDto> list = studentService.getAllSimple();
        return ResponseEntity.ok(list);
    }

}
