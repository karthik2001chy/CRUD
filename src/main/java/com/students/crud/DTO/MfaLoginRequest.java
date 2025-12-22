package com.students.crud.DTO;

/**
 * MFA Login Request - First step of multi-factor authentication
 * User provides registrationNo and password
 */
public class MfaLoginRequest {
    private String registrationNo;
    private String password;

    public MfaLoginRequest() {}

    public MfaLoginRequest(String registrationNo, String password) {
        this.registrationNo = registrationNo;
        this.password = password;
    }

    public String getRegistrationNo() {
        return registrationNo;
    }

    public void setRegistrationNo(String registrationNo) {
        this.registrationNo = registrationNo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}


