package com.students.crud.controllers;

import com.students.crud.DTO.AuthRequest;
import com.students.crud.DTO.AuthResponse;
import com.students.crud.DTO.MfaLoginRequest;
import com.students.crud.DTO.MfaOtpResponse;
import com.students.crud.DTO.MfaOtpVerificationRequest;
import com.students.crud.Services.EnhancedAuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Enhanced Authentication Controller
 *
 * Supports all authentication methods:
 * 1. JWT Authentication: /api/auth/login (handled by AuthenticationController)
 * 2. Basic Authentication: (via Security Filter)
 * 3. API Key Authentication: (via Security Filter)
 * 4. Multi-Factor Authentication: /api/auth/mfa/*
 * 5. OAuth2: enabled via SecurityConfig
 */
@RestController
@RequestMapping("/api/auth")
public class EnhancedAuthenticationController {

    @Autowired
    private EnhancedAuthenticationService enhancedAuthenticationService;

    // ==================== MULTI-FACTOR AUTHENTICATION (MFA) ====================

    /**
     * MFA Step 1: Initiate MFA Authentication
     *
     * User provides credentials, system validates and sends OTP
     *
     * Request:
     * POST /api/auth/mfa/login
     * {
     *   "registrationNo": "S001",
     *   "password": "S001"
     * }
     *
     * Response (Success):
     * {
     *   "sessionToken": "mfa_session_1234567890_123456",
     *   "message": "OTP has been sent to your registered email/phone...",
     *   "expiryInSeconds": 300
     * }
     *
     * Next Step: Call /api/auth/mfa/verify with sessionToken and OTP
     *
     * Usage:
     * curl -X POST http://localhost:8080/api/auth/mfa/login \
     *   -H "Content-Type: application/json" \
     *   -d '{"registrationNo":"S001","password":"S001"}'
     */
    @PostMapping("/mfa/login")
    public ResponseEntity<?> initiateMfaLogin(@RequestBody MfaLoginRequest mfaLoginRequest) {
        try {
            MfaOtpResponse response = enhancedAuthenticationService.initiateMfaAuthentication(mfaLoginRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * MFA Step 2: Verify OTP and Complete Authentication
     *
     * User provides OTP received on their device, system validates and issues JWT token
     *
     * Request:
     * POST /api/auth/mfa/verify
     * {
     *   "sessionToken": "mfa_session_1234567890_123456",
     *   "otp": "123456"
     * }
     *
     * Response (Success):
     * {
     *   "token": "eyJhbGciOiJIUzI1NiJ9...",
     *   "message": "MFA authentication successful",
     *   "role": "SENIOR"
     * }
     *
     * Usage:
     * curl -X POST http://localhost:8080/api/auth/mfa/verify \
     *   -H "Content-Type: application/json" \
     *   -d '{"sessionToken":"mfa_session_1234567890_123456","otp":"123456"}'
     */
    @PostMapping("/mfa/verify")
    public ResponseEntity<AuthResponse> verifyMfaOtp(@RequestBody MfaOtpVerificationRequest request) {
        AuthResponse response = enhancedAuthenticationService.verifyMfaOtp(request.getSessionToken(), request.getOtp());

        if (response.getToken() != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ==================== BASIC AUTHENTICATION ====================

    /**
     * Basic Authentication Info Endpoint
     *
     * Returns information about Basic Authentication
     *
     * Basic Auth is handled automatically by the BasicAuthenticationFilter.
     * No dedicated endpoint needed - just include Authorization header:
     *
     * Usage:
     * curl -H "Authorization: Basic UzAwMTpTMDAx" \
     *      http://localhost:8080/api/auth/basic-info
     *
     * Base64("S001:S001") = "UzAwMTpTMDAx"
     */
    @GetMapping("/basic-info")
    public ResponseEntity<String> basicAuthInfo() {
        return ResponseEntity.ok(
                "Basic Authentication is active. " +
                "Include 'Authorization: Basic <base64(username:password)>' header in requests."
        );
    }

    // ==================== API KEY AUTHENTICATION ====================

    /**
     * API Key Authentication Info Endpoint
     *
     * Returns information about API Key Authentication
     *
     * API Key can be passed as:
     * 1. Header: X-API-Key: key_value
     * 2. Query Parameter: ?api_key=key_value
     *
     * Test Keys:
     * - key_12345_abc (S001, SENIOR)
     * - key_67890_xyz (S002, JUNIOR)
     *
     * Usage:
     * curl -H "X-API-Key: key_12345_abc" \
     *      http://localhost:8080/api/auth/apikey-info
     * OR
     * curl "http://localhost:8080/api/auth/apikey-info?api_key=key_12345_abc"
     */
    @GetMapping("/apikey-info")
    public ResponseEntity<String> apiKeyInfo() {
        return ResponseEntity.ok(
                "API Key Authentication is active. " +
                "Include 'X-API-Key: <your_api_key>' header or '?api_key=<your_api_key>' query parameter. " +
                "Test keys: key_12345_abc, key_67890_xyz"
        );
    }

    // ==================== GENERAL ====================

    /**
     * Authentication Methods Info
     * No authentication required - provides information about all available authentication methods
     */
    @GetMapping("/methods")
    public ResponseEntity<String> authenticationMethods() {
        String info = """
                Supported Authentication Methods:
                
                1. JWT Authentication
                   Endpoint: POST /api/auth/login
                   Header: Authorization: Bearer <token>
                   Use case: Mobile apps, SPAs, Microservices
                
                2. Basic Authentication
                   Header: Authorization: Basic <base64(username:password)>
                   Use case: Server-to-server, Legacy systems
                
                3. API Key Authentication
                   Header: X-API-Key: <api_key>
                   OR Query: ?api_key=<api_key>
                   Use case: Public APIs, Third-party integrations
                
                4. Multi-Factor Authentication (MFA)
                   Step 1: POST /api/auth/mfa/login
                   Step 2: POST /api/auth/mfa/verify
                   Use case: High-security operations, Sensitive data
                
                For detailed usage examples, see AUTHENTICATION_GUIDE.md
                """;
        return ResponseEntity.ok(info);
    }
}
