package com.jippy.foodandmart.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class FmCustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        // You can create a Map or a custom Error DTO here
        String jsonResponse = "{"
                + "\"timestamp\": \"" + LocalDateTime.now() + "\","
                + "\"status\": 403,"
                + "\"error\": \"Forbidden\","
                + "\"message\": \"You don't have enough permissions to do this\","
                + "\"path\": \"" + request.getRequestURI() + "\""
                + "}";

        response.getWriter().write(jsonResponse);
    }
}
