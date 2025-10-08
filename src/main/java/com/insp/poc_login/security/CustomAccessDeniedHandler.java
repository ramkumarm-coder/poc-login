package com.insp.poc_login.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insp.poc_login.exception.ApiErrorResponse;
import jakarta.servlet.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpServletResponse.SC_FORBIDDEN)
                .error("Forbidden")
                .message("You do not have permission to access this resource " + accessDeniedException.getMessage())
                .path(request.getRequestURI())
                .build();

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        mapper.writeValue(response.getOutputStream(), errorResponse);
    }
}