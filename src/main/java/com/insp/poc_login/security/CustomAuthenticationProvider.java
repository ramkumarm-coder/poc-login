package com.insp.poc_login.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

//@Component
//@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private  CustomUserDetailsService userDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        System.out.println("Step 1 *************");
        // Load user manually
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        // Will throw UsernameNotFoundException if user doesn't exist
        System.out.println("Step 2 *************");
        // Check password
        if (!new BCryptPasswordEncoder().matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid credential");
        }

        // Return authenticated token
        return new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
