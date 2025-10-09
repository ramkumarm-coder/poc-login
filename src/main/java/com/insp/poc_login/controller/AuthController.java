package com.insp.poc_login.controller;

import com.insp.poc_login.dto.auth.*;
import com.insp.poc_login.dto.auth.request.*;
import com.insp.poc_login.entity.UserLogin;
import com.insp.poc_login.exception.InvalidInputException;
import com.insp.poc_login.exception.InvalidUserCreationException;
import com.insp.poc_login.repository.UserLoginRepository;
import com.insp.poc_login.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Objects;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@SecurityRequirements(value = {})
public class AuthController {
    private final AuthenticationManager authManager;
    private final JwtEncoder jwtEncoder;
    private final PasswordEncoder passwordEncoder;
    private final UserLoginRepository userRepo;
    private final AuthService service;

    @PostMapping("/register")
    @Operation(summary = "Register user")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest req) {
        if (userRepo.existsById(req.getEmail())) {
            boolean enabled = userRepo.findById(req.getEmail()).get().isEnabled();
            if(enabled)
                throw new InvalidUserCreationException();
            RegisterResponse resp = RegisterResponse.builder()
                    .email(req.getEmail())
                    .status("pending")
                    .message("Registration completed. Pending for verification.")
                    .build();
            return ResponseEntity.ok(resp);
        }

        if(!Objects.equals(req.getPassword(), req.getConfirmPassword()))
            throw new InvalidInputException("Password & confirm password does not match");

        UserLogin u = new UserLogin();
        u.setName(req.getName());
        u.setEmail(req.getEmail());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setEnabled(false);
        userRepo.save(u);
        RegisterResponse response = RegisterResponse.builder()
                .email(req.getEmail())
                .status("pending")
                .message("Registration completed. Pending for verification.")
                .build();
        return ResponseEntity.ok(response);
    }



    @PostMapping("/login")
    @Operation(
            summary = "Login endpoint (public)",
            description = "Provide username and password to get JWT token",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = {@ExampleObject(value = "{\"email\":\"rk.nic001@gmail.com\", \"password\":\"Rk@123\"}")}
                    )}
            )
    )
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        if (userRepo.existsById(req.getEmail())) {
            boolean enabled = userRepo.findById(req.getEmail()).get().isEnabled();
            if(enabled)
                throw new InvalidUserCreationException();
            RegisterResponse resp = RegisterResponse.builder()
                    .email(req.getEmail())
                    .status("pending")
                    .message("Registration completed. Pending for verification.")
                    .build();
            return ResponseEntity.ok(resp);
        }


        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .subject(auth.getName())
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        LoginResponse response = LoginResponse.builder()
                .status("success")
                .tokenType("Bearer")
                .token(token)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send-otp")
    public ResponseEntity<OtpResponse> sendOtp(@RequestBody OtpRequest req) {
        String message = service.sendOtp(req.getEmail());
        OtpResponse resp = OtpResponse.builder()
                .status("success")
                .message(message)
                .build();
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/verify-registration")
    public ResponseEntity<VerifyRegistrationResponse> verifyRegistration(@RequestBody VerifyRegistrationRequest req) {
        service.verifyRegisteredUser(req.getEmail(), req.getOtp());

        VerifyRegistrationResponse resp = VerifyRegistrationResponse.builder()
                .status("success")
                .message("User Registered successfully. Please login!")
                .build();
        return ResponseEntity.ok(resp);
    }


    @PostMapping("/forgot-pwd-otp")
    public ResponseEntity<StatusMsgResponse> forgotPasswordOtp(@RequestBody ForgotPasswordRequest req) {
        String message = service.forgotPasswordOtp(req.getEmail());
        StatusMsgResponse resp = StatusMsgResponse.builder()
                .status("success")
                .message(message)
                .build();
        return ResponseEntity.ok(resp);
    }


    @PostMapping("/forgot-pwd-verify")
    public ResponseEntity<StatusMsgResponse> forgotPasswordVerify(@RequestBody ForgotPasswordVerifyRequest req) {

        service.forgotPasswordVerify(req.getEmail(), req.getOtp());
        StatusMsgResponse resp = StatusMsgResponse.builder()
                .status("success")
                .message("OTP Verified successfully! 5 minutes to reset your password!")
                .build();
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/forgot-pwd-reset")
    public ResponseEntity<StatusMsgResponse> forgotPasswordReset(@RequestBody ForgotPasswordResetRequest req) {

        if(!Objects.equals(req.getPassword(), req.getConfirmPassword()))
            throw new InvalidInputException("Password & confirm password does not match");

        service.forgotPasswordReset(req.getEmail(), req.getPassword());
        StatusMsgResponse resp = StatusMsgResponse.builder()
                .status("success")
                .message("Password reset successfully! Please login")
                .build();
        return ResponseEntity.ok(resp);
    }
}
