package com.students.crud.DTO;

/**
 * MFA OTP Response - Response containing session token for OTP verification
 */
public class MfaOtpResponse {
    private String sessionToken;
    private String message;
    private long expiryInSeconds;

    public MfaOtpResponse() {}

    public MfaOtpResponse(String sessionToken, String message, long expiryInSeconds) {
        this.sessionToken = sessionToken;
        this.message = message;
        this.expiryInSeconds = expiryInSeconds;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getExpiryInSeconds() {
        return expiryInSeconds;
    }

    public void setExpiryInSeconds(long expiryInSeconds) {
        this.expiryInSeconds = expiryInSeconds;
    }
}

