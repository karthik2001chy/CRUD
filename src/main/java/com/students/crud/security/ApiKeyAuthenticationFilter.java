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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * API Key Authentication Filter
 *
 * How it works:
 * 1. Extracts API key from "X-API-Key" header or "api_key" query parameter
 * 2. Validates API key against stored keys in registry
 * 3. Retrieves user info associated with the key
 * 4. Creates authentication object if key is valid
 * 5. Allows request to proceed
 *
 * Usage:
 * curl -H "X-API-Key: key_12345_abc" http://localhost:8080/api/students
 * OR
 * curl "http://localhost:8080/api/students?api_key=key_12345_abc"
 *
 * NOTE: In production, store API keys in database with proper encryption
 */
@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    // In-memory API key registry (in production, use database)
    // Format: key -> {registrationNo, role, active}
    private static final Map<String, Map<String, Object>> API_KEY_REGISTRY = new HashMap<>();

    static {
        // Example API keys for testing
        Map<String, Object> key1 = new HashMap<>();
        key1.put("registrationNo", "S001");
        key1.put("role", "SENIOR");
        key1.put("active", true);
        API_KEY_REGISTRY.put("key_12345_abc", key1);

        Map<String, Object> key2 = new HashMap<>();
        key2.put("registrationNo", "S002");
        key2.put("role", "JUNIOR");
        key2.put("active", true);
        API_KEY_REGISTRY.put("key_67890_xyz", key2);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String apiKey = null;

        // Try to get API key from header first
        String headerApiKey = request.getHeader("X-API-Key");
        if (headerApiKey != null && !headerApiKey.isEmpty()) {
            apiKey = headerApiKey;
        }

        // If not in header, try query parameter
        if (apiKey == null) {
            apiKey = request.getParameter("api_key");
        }

        // Validate API key
        if (apiKey != null && !apiKey.isEmpty()) {
            try {
                Map<String, Object> keyData = API_KEY_REGISTRY.get(apiKey);

                if (keyData != null && (Boolean) keyData.get("active")) {
                    String registrationNo = (String) keyData.get("registrationNo");
                    String role = (String) keyData.get("role");

                    // Create authentication token
                    Collection<GrantedAuthority> authorities = new ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role));

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(registrationNo, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                // Invalid API key, continue without authentication
                logger.debug("API key authentication failed: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Add API key to registry (call this when user requests a new API key)
     */
    public static void addApiKey(String apiKey, String registrationNo, String role) {
        Map<String, Object> keyData = new HashMap<>();
        keyData.put("registrationNo", registrationNo);
        keyData.put("role", role);
        keyData.put("active", true);
        keyData.put("createdAt", System.currentTimeMillis());
        API_KEY_REGISTRY.put(apiKey, keyData);
    }

    /**
     * Revoke API key
     */
    public static void revokeApiKey(String apiKey) {
        Map<String, Object> keyData = API_KEY_REGISTRY.get(apiKey);
        if (keyData != null) {
            keyData.put("active", false);
        }
    }

    /**
     * Check if API key is valid
     */
    public static boolean isValidApiKey(String apiKey) {
        Map<String, Object> keyData = API_KEY_REGISTRY.get(apiKey);
        return keyData != null && (Boolean) keyData.get("active");
    }
}

