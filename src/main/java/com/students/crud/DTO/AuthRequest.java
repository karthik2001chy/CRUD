package com.students.crud.DTO;

public class AuthRequest {
    private String registrationNo;
    private String password;

    public AuthRequest() {}

    public AuthRequest(String registrationNo, String password) {
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

