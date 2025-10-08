package com.insp.poc_login.controller;

import com.insp.poc_login.dto.auth.LoginRequest;
import com.insp.poc_login.dto.auth.RegisterRequest;
import com.insp.poc_login.entity.UserLogin;
import com.insp.poc_login.repository.UserLoginRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authManager;
    private final JwtEncoder jwtEncoder;
    private final PasswordEncoder passwordEncoder;
    private final UserLoginRepository userRepo;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (userRepo.existsByUsername(req.getUsername()) || userRepo.existsByEmail(req.getEmail())) {
            return ResponseEntity.badRequest().body("Username/Email already exists");
        }
        UserLogin u = new UserLogin();
        u.setUsername(req.getUsername());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setEmail(req.getEmail());
        userRepo.save(u);
        return ResponseEntity.ok("User registered");
    }

    @PostMapping("/token")
    public ResponseEntity<?> token(@RequestBody LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .subject(auth.getName())
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        return ResponseEntity.ok(Map.of("access_token", token, "token_type", "Bearer"));
    }
}
