package com.insp.poc_login.security;

import com.insp.poc_login.entity.UserLogin;
import com.insp.poc_login.exception.InvalidUserException;
import com.insp.poc_login.repository.UserLoginRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserLoginRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserLogin user = userRepository.findById(username).orElseThrow(() -> new InvalidUserException("User not found: " + username));
        if (!user.isEnabled()) {
            throw new InvalidUserException("User registration not yet completed: " + username);
        }
//        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
//        List<GrantedAuthority> authorities = user.getRoles().stream()
//                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
//                .collect(Collectors.toList());

        return new AppUser(user);
    }
}
