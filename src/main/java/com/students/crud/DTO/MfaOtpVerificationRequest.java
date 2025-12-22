package com.students.crud.DTO;

/**
 * MFA OTP Verification Request - Second step of multi-factor authentication
 * User provides the OTP received on their device
 */
public class MfaOtpVerificationRequest {
    private String sessionToken;
    private String otp;

    public MfaOtpVerificationRequest() {}

    public MfaOtpVerificationRequest(String sessionToken, String otp) {
        this.sessionToken = sessionToken;
        this.otp = otp;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}

