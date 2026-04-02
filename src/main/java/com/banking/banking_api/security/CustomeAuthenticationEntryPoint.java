package com.banking.banking_api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.awt.*;
import java.io.IOException;
import java.util.Map;

@Component
public class CustomeAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);


        objectMapper.writeValue(response.getWriter(), Map.of(
                "status", 401,
                "error", "Unauthorized",
                "message", "JWT token missing or invalid. Login via /api/v1/login to get a token"
        ));
    }
}
