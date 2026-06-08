package com.jippy.driver.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DriverJwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        // Only log if it's NOT a health check or documentation request
        if (!path.startsWith("/actuator") && !path.contains("api-docs")) {
            log.info("path: {}",path);
        }
        // 1. Get the headers injected by the Gateway
        String username = request.getHeader("X-Auth-User");
        String rolesHeader = request.getHeader("X-Auth-Roles");

        log.info("Extracted from headers - Username: {}, Roles: {}", username, rolesHeader);

        // 2. If the Gateway has verified the user, set the context
        if (username != null && rolesHeader != null) {

            // Convert comma-separated roles from header back to Authorities
            List<SimpleGrantedAuthority> authorities = Arrays.stream(rolesHeader.split(","))
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            // Create the Authentication token (password is null because it's already verified)
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Set the security context for the rest of this request
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}
