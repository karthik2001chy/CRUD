package com.students.crud.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;

/**
 * Basic Authentication Filter
 *
 * How it works:
 * 1. Extracts Base64 encoded credentials from "Authorization: Basic" header
 * 2. Decodes credentials to get username and password
 * 3. Validates against the repository
 * 4. Creates authentication object if valid
 * 5. Allows request to proceed
 *
 * Usage:
 * curl -H "Authorization: Basic UzAwMTpTMDAx" http://localhost:8080/api/students
 * (Base64("S001:S001") = "UzAwMTpTMDAx")
 */
@Component
public class BasicAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private com.students.crud.repository.StudentRepository studentRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Basic ")) {
            try {
                // Extract and decode Base64 credentials
                String encodedCredentials = authHeader.substring(6);
                String decodedCredentials = new String(Base64.getDecoder().decode(encodedCredentials));
                String[] credentials = decodedCredentials.split(":", 2);

                if (credentials.length == 2) {
                    String registrationNo = credentials[0];
                    String password = credentials[1];

                    // Validate credentials against repository
                    var studentOpt = studentRepository.findById(registrationNo);
                    if (studentOpt.isPresent()) {
                        var student = studentOpt.get();

                        // In production, use BCrypt to compare passwords
                        // For now, simple comparison (NOT SECURE - FIX THIS)
                        if (password.equals(student.getRegistrationNo())) {
                            // Determine role based on DOB
                            String role = determineRole(student.getDateOfBirth());

                            // Create authentication token
                            Collection<GrantedAuthority> authorities = new ArrayList<>();
                            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));

                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(registrationNo, null, authorities);

                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        }
                    }
                }
            } catch (Exception e) {
                // Invalid credentials, continue without authentication
                logger.debug("Basic authentication failed: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private String determineRole(java.util.Date dateOfBirth) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(dateOfBirth);
        int year = cal.get(java.util.Calendar.YEAR);
        return year > 2002 ? "JUNIOR" : "SENIOR";
    }
}

