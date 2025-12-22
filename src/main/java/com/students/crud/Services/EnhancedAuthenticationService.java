package com.students.crud.Services;

import com.students.crud.DAO.Student;
import com.students.crud.DTO.AuthRequest;
import com.students.crud.DTO.AuthResponse;
import com.students.crud.DTO.MfaLoginRequest;
import com.students.crud.DTO.MfaOtpResponse;
import com.students.crud.repository.StudentRepository;
import com.students.crud.security.JwtUtil;
import com.students.crud.security.MfaManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Map;
import java.util.Optional;

/**
 * Enhanced Authentication Service with Multiple Authentication Methods
 *
 * Supports:
 * 1. JWT Authentication (original)
 * 2. Basic Authentication
 * 3. API Key Authentication
 * 4. Multi-Factor Authentication (MFA)
 * 5. OAuth2 (Google/GitHub) - placeholder
 */
@Service
public class EnhancedAuthenticationService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MfaManager mfaManager;

    /**
     * Standard JWT Authentication
     * Original method - authenticate with credentials and return JWT token
     */
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

    /**
     * Multi-Factor Authentication - Step 1: Validate credentials and send OTP
     *
     * Process:
     * 1. Validate student credentials
     * 2. Generate OTP and send to registered device (email/SMS)
     * 3. Return session token for OTP verification
     *
     * Example:
     * curl -X POST http://localhost:8080/api/auth/mfa/login \
     *   -H "Content-Type: application/json" \
     *   -d '{"registrationNo":"S001","password":"S001"}'
     */
    public MfaOtpResponse initiateMfaAuthentication(MfaLoginRequest mfaLoginRequest) {
        Optional<Student> studentOpt = studentRepository.findById(mfaLoginRequest.getRegistrationNo());

        if (studentOpt.isEmpty()) {
            throw new RuntimeException("Student not found");
        }

        Student student = studentOpt.get();

        // Simple password validation (in production, use BCrypt)
        if (!student.getRegistrationNo().equals(mfaLoginRequest.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // Generate OTP and get session token
        String sessionToken = mfaManager.generateOtp(student.getRegistrationNo());

        return new MfaOtpResponse(
                sessionToken,
                "OTP has been sent to your registered email/phone. Please verify to complete authentication.",
                300  // 5 minutes in seconds
        );
    }

    /**
     * Multi-Factor Authentication - Step 2: Verify OTP and issue JWT token
     *
     * Process:
     * 1. Validate session token
     * 2. Verify provided OTP
     * 3. Retrieve user details
     * 4. Generate JWT token
     * 5. Return token for future requests
     *
     * Example:
     * curl -X POST http://localhost:8080/api/auth/mfa/verify \
     *   -H "Content-Type: application/json" \
     *   -d '{"sessionToken":"mfa_session_...","otp":"123456"}'
     */
    public AuthResponse verifyMfaOtp(String sessionToken, String otp) {
        try {
            // Verify OTP and get user details
            Map<String, String> userData = mfaManager.verifyOtp(sessionToken, otp);
            String registrationNo = userData.get("registrationNo");

            // Get student from repository
            Optional<Student> studentOpt = studentRepository.findById(registrationNo);
            if (studentOpt.isEmpty()) {
                return new AuthResponse(null, "User not found", null);
            }

            Student student = studentOpt.get();
            String role = determineRole(student.getDateOfBirth());

            // Generate JWT token
            String token = jwtUtil.generateToken(registrationNo, role);

            return new AuthResponse(token, "MFA authentication successful", role);
        } catch (RuntimeException e) {
            return new AuthResponse(null, "MFA verification failed: " + e.getMessage(), null);
        }
    }

    /**
     * Determine user role based on date of birth
     *
     * Rule:
     * - Born after 2002 → JUNIOR
     * - Born in 2002 or before → SENIOR
     */
    private String determineRole(java.util.Date dateOfBirth) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateOfBirth);
        int year = cal.get(Calendar.YEAR);
        return year > 2002 ? "JUNIOR" : "SENIOR";
    }

    /**
     * Validate API Key (for API Key Authentication)
     * Can be called from ApiKeyAuthenticationFilter
     */
    public boolean validateApiKey(String apiKey) {
        // This would validate against a database of API keys
        // For now, it's a placeholder
        return apiKey != null && !apiKey.isEmpty();
    }

    /**
     * Validate Basic Auth credentials
     * Can be called from BasicAuthenticationFilter
     */
    public boolean validateBasicAuthCredentials(String registrationNo, String password) {
        Optional<Student> studentOpt = studentRepository.findById(registrationNo);
        if (studentOpt.isEmpty()) {
            return false;
        }

        Student student = studentOpt.get();
        // In production, use BCrypt.checkpw(password, student.getHashedPassword())
        return student.getRegistrationNo().equals(password);
    }
}

