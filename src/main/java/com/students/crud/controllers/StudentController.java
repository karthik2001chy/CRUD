package com.students.crud.controllers;

import com.students.crud.DTO.StudentSimpleDto;
import com.students.crud.Services.StudentService;
import com.students.crud.DAO.Student;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
@Tag(name = "Student Management", description = "APIs for managing students with pagination and caching support")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    @Operation(summary = "List all students (paginated)", description = "Retrieve a paginated list of students with all information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved student list",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Page<Student>> listStudents(
            @PageableDefault(size = 20, sort = "registrationNo") Pageable pageable) {
        Page<Student> page = studentService.getStudents(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/full")
    @Operation(summary = "List all students (complete details)", description = "Retrieve a paginated list of students with all information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved complete student list",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Page<Student>> listStudentsFull(
            @PageableDefault(size = 20, sort = "registrationNo") Pageable pageable) {
        Page<Student> page = studentService.getStudents(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/full/{registrationNo}")
    @Operation(summary = "Get student by registration number", description = "Retrieve complete details of a specific student")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Student found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Student.class))),
            @ApiResponse(responseCode = "404", description = "Student not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Student> getStudentByRegistrationNo(
            @PathVariable String registrationNo) {
        return studentService.getStudentByRegistrationNo(registrationNo)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/search/name")
    @Operation(summary = "Search students by name", description = "Search for students by their name (case-insensitive partial match)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search results retrieved successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Student>> searchStudentsByName(
            @RequestParam(name = "name") String name) {
        List<Student> students = studentService.searchStudentsByName(name);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/search/name/simple")
    @Operation(summary = "Search students by name (simplified)", description = "Search for students by name and return simplified information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search results retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<StudentSimpleDto>> searchStudentsByNameSimple(
            @RequestParam(name = "name") String name) {
        List<StudentSimpleDto> students = studentService.searchStudentsByNameSimple(name);
        return ResponseEntity.ok(students);
    }

    @PostMapping
    @Operation(summary = "Register a new student", description = "Create a new student record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Student registered successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Student.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Student> registerStudent(@RequestBody Student student) {
        Student savedStudent = studentService.registerStudent(student);
        return ResponseEntity.ok(savedStudent);
    }

    @DeleteMapping("/{registrationNo}")
    @Operation(summary = "Delete a student", description = "Delete a student by registration number")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Student deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Student not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteStudent(@PathVariable String registrationNo) {
        studentService.deleteStudent(registrationNo);
        return ResponseEntity.noContent().build();
    }

}
