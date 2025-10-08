package com.insp.poc_login.service.impl;

import com.insp.poc_login.entity.UserLogin;
import com.insp.poc_login.exception.InvalidUserException;
import com.insp.poc_login.repository.UserLoginRepository;
import com.insp.poc_login.service.AuthService;
import com.insp.poc_login.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserLoginRepository userLoginRepository;
    private final OtpService otpService;

    @Override
    public String sendOtp(String email) {
        UserLogin userLogin = userLoginRepository.findById(email).orElseThrow(() -> new InvalidUserException("User not found: " + email));
        if(!userLogin.isEnabled()){
           return otpService.generateOtp(email);
        }
        throw new RuntimeException("User already registered successfully. Please login");
    }

    public void verifyRegisteredUser(String email, String userOtp){
        UserLogin userLogin = userLoginRepository.findById(email).orElseThrow(() -> new InvalidUserException("User not found: " + email));
        boolean isValidOtp = otpService.validateOtp(email, userOtp);
        userLogin.setEnabled(true);

        if(isValidOtp) {
            userLoginRepository.save(userLogin);
        }
        throw new RuntimeException("Invalid OTP for User registration");
    }

    public String forgotPasswordOtp(String email) {
        UserLogin userLogin = userLoginRepository.findById(email).orElseThrow(() -> new InvalidUserException("User not found: " + email));
        if(userLogin.isEnabled()){
            return otpService.generateOtp(email);
        }
        throw new RuntimeException("User not completed registration process!");
    }

    public void forgotPasswordVerify(String email, String userOtp){
        UserLogin userLogin = userLoginRepository.findById(email).orElseThrow(() -> new InvalidUserException("User not found: " + email));
        boolean isValidOtp = otpService.validateOtp(email, userOtp);
        if(!userLogin.isEnabled())
            throw new RuntimeException("User not completed registration process!");

        if(isValidOtp) {
            userLogin.setForgotPwdTimestamp(LocalDateTime.now());
            userLoginRepository.save(userLogin);
        }
        throw new RuntimeException("Invalid OTP for forgot password");
    }

    public void forgotPasswordReset(String email, String password){
        UserLogin userLogin = userLoginRepository.findById(email).orElseThrow(() -> new InvalidUserException("User not found: " + email));
        if(!userLogin.isEnabled())
            throw new RuntimeException("User not completed registration process!");


        if(userLogin.getForgotPwdTimestamp().plusMinutes(5).isBefore(LocalDateTime.now())){
            userLogin.setPassword(new BCryptPasswordEncoder().encode(password));
            userLogin.setForgotPwdTimestamp(null);
            userLoginRepository.save(userLogin);
        }
        throw new RuntimeException("Invalid OTP for forgot password");
    }
}
