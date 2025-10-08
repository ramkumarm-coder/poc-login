package com.insp.poc_login.security;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insp.poc_login.exception.InvalidUserException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

@Component
public class CustomAuthEntryPoint implements org.springframework.security.web.AuthenticationEntryPoint {

//    @Override
//    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
//            throws IOException, ServletException {
//
//        // Determine the error message
//        String message;
//        HttpStatus status = HttpStatus.UNAUTHORIZED;
//
//        if (authException instanceof InsufficientAuthenticationException) {
//            message = "JWT is missing or authentication is insufficient.";
//        } else if (authException instanceof BadCredentialsException) {
//            message = authException.getMessage(); // e.g., "Invalid password"
//        } else if (authException instanceof UsernameNotFoundException) {
//            message = authException.getMessage(); // e.g., "User not found"
//        } else {
//            message = authException.getMessage();
//        }
//
//        writeJsonResponse(request, response, message, status);
//    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        authException.printStackTrace();
        String message = authException.getMessage();

        // Check if cause has UsernameNotFoundException
        Throwable cause = authException.getCause();
        while (cause != null) {
            if (cause instanceof InvalidUserException) {
                message = cause.getMessage();
                break;
            } else if(cause instanceof InsufficientAuthenticationException) {
                message = "JWT is missing or authentication is insufficient." + cause.getMessage();
                break;
            }
            cause = cause.getCause();
        }

        writeJsonResponse(request, response, message, HttpStatus.UNAUTHORIZED);
    }

    private void writeJsonResponse(HttpServletRequest request, HttpServletResponse response, String message, HttpStatus status)
            throws IOException {

        String basePath = getBasePath(request);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, message);
        problemDetail.setTitle("Authentication Failed");
        problemDetail.setStatus(status.value());
        problemDetail.setType(URI.create(basePath + "/errors/authentication-error"));
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(status.value());

        try (OutputStream responseStream = response.getOutputStream()) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            mapper.writeValue(responseStream, problemDetail);
            responseStream.flush();
        }
    }

    private String getBasePath(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        StringBuilder basePath = new StringBuilder();
        basePath.append(scheme).append("://").append(serverName);
        if ((scheme.equals("http") && serverPort != 80) || (scheme.equals("https") && serverPort != 443)) {
            basePath.append(":").append(serverPort);
        }
        basePath.append(contextPath);

        return basePath.toString();
    }

    public static boolean validateExceptionChain(Throwable throwable, Class<?>... exceptionClasses) {
        while (throwable != null) {
            for (Class<?> exceptionClass : exceptionClasses) {
                if (exceptionClass.isInstance(throwable)) {
                    return true;
                }
            }
            throwable = throwable.getCause(); // Move to the next cause in the chain
        }
        return false;
    }

}