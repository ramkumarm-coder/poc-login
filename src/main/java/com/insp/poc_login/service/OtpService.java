package com.insp.poc_login.service;

import com.insp.poc_login.entity.Otp;
import com.insp.poc_login.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final JavaMailSender mailSender;
    private final OtpRepository otpRepository;

    @Value("${otp.expiry}")
    private int expiryMinutes;

    // Generate OTP
    public String generateOtp(String email) {
        String otp = String.format("%06d", new Random().nextInt(999999));

        Otp otpEntity = new Otp();
        otpEntity.setEmail(email);
        otpEntity.setCode(otp);
        otpEntity.setCreatedAt(LocalDateTime.now());
        otpRepository.save(otpEntity);

        sendOtpEmail(email, otp);
        return "OTP generated successfully! Please check your mail!";
    }

    // Send OTP via email
    private void sendOtpEmail(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your OTP Code");
        message.setText("Your OTP code is: " + otp + "\nIt will expire in " + expiryMinutes + " minutes.");
        mailSender.send(message);
    }

    // Validate OTP (only latest)
    public boolean validateOtp(String email, String code) {
        return otpRepository.findTopByEmailOrderByCreatedAtDesc(email)
                .map(otp -> {
                    if (!otp.getCode().equals(code)) return false;
                    if (otp.getCreatedAt().plusMinutes(expiryMinutes).isBefore(LocalDateTime.now())) return false;
                    otp.setUsed(true); // mark as used
                    otpRepository.save(otp);
                    return true;
                }).orElse(false);
    }
}