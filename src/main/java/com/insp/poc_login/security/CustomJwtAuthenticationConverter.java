package com.insp.poc_login.security;

import com.insp.poc_login.entity.UserLogin;
import com.insp.poc_login.repository.UserLoginRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserLoginRepository userLoginRepository;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String username = jwt.getSubject();
        UserLogin userLogin = userLoginRepository.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));


        UserDetails user = new AppUser(userLogin);
        Collection<GrantedAuthority> authorities = user.getAuthorities().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getAuthority()))
                .collect(Collectors.toList());

        return new UsernamePasswordAuthenticationToken(user, jwt, authorities);
    }
}
