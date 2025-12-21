package com.students.crud.Services;

import com.students.crud.DAO.Student;
import com.students.crud.DTO.AuthRequest;
import com.students.crud.DTO.AuthResponse;
import com.students.crud.repository.StudentRepository;
import com.students.crud.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Optional;

@Service
public class AuthenticationService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthResponse authenticateStudent(AuthRequest authRequest) {
        Optional<Student> studentOpt = studentRepository.findById(authRequest.getRegistrationNo());

        if (studentOpt.isEmpty()) {
            return new AuthResponse(null, "Student not found", null);
        }

        Student student = studentOpt.get();

        // Simple password validation (in production, use BCrypt)
        if (!student.getRegistrationNo().equals(authRequest.getPassword())) {
            return new AuthResponse(null, "Invalid credentials", null);
        }

        // Determine role based on DOB
        String role = determineRole(student.getDateOfBirth());

        // Generate JWT token
        String token = jwtUtil.generateToken(student.getRegistrationNo(), role);

        return new AuthResponse(token, "Authentication successful", role);
    }

    private String determineRole(java.util.Date dateOfBirth) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateOfBirth);
        int year = cal.get(Calendar.YEAR);
        return year > 2002 ? "JUNIOR" : "SENIOR";
    }
}

