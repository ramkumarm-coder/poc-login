package com.insp.poc_login.service;

public interface AuthService {
    String sendOtp(String email);
    void verifyRegisteredUser(String email, String userOtp);
    String forgotPasswordOtp(String email);
    void forgotPasswordVerify(String email, String userOtp);

}
