package com.insp.poc_login.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;


@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private final CustomUserDetailsService userDetailsService;
    private final CustomJwtAuthenticationConverter jwtAuthConverter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**",
                                "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**")
                        .permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(
                        oauth2 -> oauth2
                                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter)));

        return http.build();
    }

    // ✅ JWK Source
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        byte[] secretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);

        OctetSequenceKey octetKey = new OctetSequenceKey.Builder(secretBytes)
                .algorithm(JWSAlgorithm.HS256)
                .keyID("hmac-key-1")
                .build();

        return new ImmutableJWKSet<>(new com.nimbusds.jose.jwk.JWKSet(octetKey));
    }

    // ✅ Jwt Encoder
//    @Bean
//    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
//        return new NimbusJwtEncoder(jwkSource);
//    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return parameters -> {
//            byte[] secretKeyBytes = Base64.getDecoder().decode(jwtSecret);
            SecretKeySpec secretKeySpec = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

            try {
                MACSigner signer = new MACSigner(secretKeySpec);

                JWTClaimsSet.Builder claimsSetBuilder = new JWTClaimsSet.Builder();
                parameters.getClaims().getClaims().forEach((key, value) ->
                        claimsSetBuilder.claim(key, value instanceof Instant ? Date.from((Instant) value) : value)
                );
                JWTClaimsSet claimsSet = claimsSetBuilder.build();

                JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

                SignedJWT signedJWT = new SignedJWT(header, claimsSet);
                signedJWT.sign(signer);

                return Jwt.withTokenValue(signedJWT.serialize())
                        .header("alg", header.getAlgorithm().getName())
                        .subject(claimsSet.getSubject())
                        .issuer(claimsSet.getIssuer())
                        .claims(claims -> claims.putAll(claimsSet.getClaims()))
                        .issuedAt(claimsSet.getIssueTime().toInstant())
                        .expiresAt(claimsSet.getExpirationTime().toInstant())
                        .build();
            } catch (Exception e) {
                throw new IllegalStateException("Error while signing the JWT", e);
            }
        };
    }


    // ✅ Jwt Decoder
    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKeySpec secretKey = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }
}