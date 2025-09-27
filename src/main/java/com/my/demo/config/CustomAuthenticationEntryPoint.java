package com.my.demo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.demo.response.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {

        // Set response properties FIRST
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // Create the API response
        ApiResponse<Void> apiResponse = ApiResponse.error(
            401,
            "Authentication required - Please provide a valid token",
            "AUTHENTICATION_REQUIRED"
        );

        // Write the response directly without calling delegate
        mapper.writeValue(response.getWriter(), apiResponse);
        response.getWriter().flush();
    }
}
