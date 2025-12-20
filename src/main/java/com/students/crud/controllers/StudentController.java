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

@RestController
@RequestMapping("/api/students")
@Tag(name = "Student Management", description = "APIs for managing students with pagination and caching support")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    @Operation(summary = "List all students (simplified)", description = "Retrieve a paginated list of simplified student information with caching support (TTL: 300 seconds)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved simplified student list",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Page<StudentSimpleDto>> listStudents(
            @PageableDefault(size = 20, sort = "registrationNo") Pageable pageable) {
        Page<StudentSimpleDto> page = studentService.getStudentsSimple(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/full")
    @Operation(summary = "List all students (complete details)", description = "Retrieve a paginated list of students with all information including address, date of birth, and parent contact details")
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

}
