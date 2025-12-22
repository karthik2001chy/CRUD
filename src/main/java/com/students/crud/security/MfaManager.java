package com.students.crud.security;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Multi-Factor Authentication (MFA) Manager
 *
 * How it works:
 * 1. After user provides password, system generates OTP (One-Time Password)
 * 2. OTP is sent to registered device (email/SMS)
 * 3. User provides OTP for verification
 * 4. If OTP is valid, full authentication is granted
 * 5. Session token or JWT is created
 *
 * Workflow:
 * 1. User sends credentials → receiveOTP (generates OTP, sends to user)
 * 2. User sends OTP → verifyOTP (validates OTP, generates full auth token)
 *
 * Security features:
 * - OTP expires after 5 minutes
 * - OTP is single-use
 * - Limited attempts (3 incorrect attempts locks the session)
 * - Session token has short validity
 */
@Component
public class MfaManager {

    // Store OTP sessions: sessionToken -> {otp, registrationNo, expiryTime, attempts}
    private final Map<String, Map<String, Object>> otpSessions = new ConcurrentHashMap<>();

    // Store user devices: registrationNo -> {email, phone}
    private final Map<String, Map<String, String>> userDevices = new ConcurrentHashMap<>();

    private static final long OTP_EXPIRY_MS = 5 * 60 * 1000; // 5 minutes
    private static final int MAX_ATTEMPTS = 3;
    private static final int OTP_LENGTH = 6;

    static {
        // Example devices (in production, fetch from database)
        Map<String, String> devices = new HashMap<>();
        devices.put("email", "student001@university.edu");
        devices.put("phone", "+91-9876543210");
        // Store for registrationNo S001
    }

    /**
     * Generate OTP and send to user's registered device
     * Returns temporary session token for OTP verification
     */
    public String generateOtp(String registrationNo) {
        try {
            // Generate 6-digit OTP
            String otp = String.format("%06d", new Random().nextInt(1000000));

            // Generate temporary session token
            String sessionToken = generateSessionToken();

            // Store OTP session
            Map<String, Object> session = new HashMap<>();
            session.put("otp", otp);
            session.put("registrationNo", registrationNo);
            session.put("expiryTime", System.currentTimeMillis() + OTP_EXPIRY_MS);
            session.put("attempts", 0);
            session.put("used", false);
            otpSessions.put(sessionToken, session);

            // In production, send OTP via SMS or Email
            sendOtp(registrationNo, otp);

            return sessionToken;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate OTP: " + e.getMessage());
        }
    }

    /**
     * Verify OTP provided by user
     * Returns user details if OTP is valid
     */
    public Map<String, String> verifyOtp(String sessionToken, String providedOtp) {
        Map<String, Object> session = otpSessions.get(sessionToken);

        if (session == null) {
            throw new RuntimeException("Invalid session token");
        }

        // Check if OTP has expired
        long expiryTime = (Long) session.get("expiryTime");
        if (System.currentTimeMillis() > expiryTime) {
            otpSessions.remove(sessionToken);
            throw new RuntimeException("OTP has expired");
        }

        // Check if OTP has already been used
        if ((Boolean) session.get("used")) {
            throw new RuntimeException("OTP has already been used");
        }

        // Check attempt count
        int attempts = (Integer) session.get("attempts");
        if (attempts >= MAX_ATTEMPTS) {
            otpSessions.remove(sessionToken);
            throw new RuntimeException("Maximum OTP attempts exceeded. Please request new OTP.");
        }

        // Verify OTP
        String correctOtp = (String) session.get("otp");
        if (!correctOtp.equals(providedOtp)) {
            session.put("attempts", attempts + 1);
            throw new RuntimeException("Invalid OTP. Attempts remaining: " + (MAX_ATTEMPTS - attempts - 1));
        }

        // OTP is valid - mark as used
        session.put("used", true);

        // Return user details
        Map<String, String> userData = new HashMap<>();
        userData.put("registrationNo", (String) session.get("registrationNo"));
        return userData;
    }

    /**
     * Send OTP to user's registered device
     * In production, implement actual SMS/Email service
     */
    private void sendOtp(String registrationNo, String otp) {
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("📧 OTP SENT TO USER: " + registrationNo);
        System.out.println("OTP: " + otp);
        System.out.println("Valid for: 5 minutes");
        System.out.println("═══════════════════════════════════════════════════════");

        // In production, send via:
        // 1. Email service (Spring Mail)
        // 2. SMS service (Twilio, AWS SNS)
        // 3. Authenticator app (Google Authenticator, Microsoft Authenticator)
    }

    /**
     * Generate unique session token
     */
    private String generateSessionToken() {
        return "mfa_session_" + System.currentTimeMillis() + "_" +
               (int)(Math.random() * 1000000);
    }

    /**
     * Clean up expired OTP sessions
     */
    public void cleanupExpiredSessions() {
        otpSessions.entrySet().removeIf(entry -> {
            Map<String, Object> session = entry.getValue();
            long expiryTime = (Long) session.get("expiryTime");
            return System.currentTimeMillis() > expiryTime;
        });
    }
}

