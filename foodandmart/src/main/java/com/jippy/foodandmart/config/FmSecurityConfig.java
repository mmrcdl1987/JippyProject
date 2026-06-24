package com.jippy.foodandmart.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class FmSecurityConfig {

    @Autowired
    FmJwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    FmCustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                // 1. Make it stateless - Jippy doesn't need sessions with JWT
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // Use broad wildcards to ensure the documentation endpoints are universally bypassed
                        .requestMatchers("/v3/api-docs/**",  "/api/fm/v3/api-docs").permitAll()

                        // 2. ADD THIS: Allow Health Checks
                        .requestMatchers("/actuator/**").permitAll()

                        .requestMatchers("/api/fm/auth/**", "/auth/**").permitAll() // Public login/register

                        //.requestMatchers("/api/fm/**").authenticated() // Protected routes
                        // 2. READ-ONLY ROLE (Can only perform GET requests)
                        .requestMatchers(HttpMethod.GET, "/api/fm/**").hasAnyRole("OUTLET","MERCHANT","ADMIN","SUPERADMIN","DEVADMIN")

                        // 3. CREATE/UPDATE ROLE (Can perform POST/PUT/PATCH)
                        .requestMatchers(HttpMethod.POST, "/api/fm/**").hasAnyRole("ADMIN","SUPERADMIN","DEVADMIN","OUTLET","MERCHANT")
                        .requestMatchers(HttpMethod.PUT, "/api/fm/**").hasAnyRole("ADMIN","SUPERADMIN","DEVADMIN","OUTLET","MERCHANT")
                        .requestMatchers(HttpMethod.PATCH, "/api/fm/**").hasAnyRole("ADMIN","SUPERADMIN","DEVADMIN","OUTLET","MERCHANT")

                        // 4. FULL ADMIN (Can also DELETE)
                        .requestMatchers(HttpMethod.DELETE, "/api/fm/**").hasAnyRole("ADMIN","SUPERADMIN","DEVADMIN","OUTLET","MERCHANT")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        // This line links your custom message logic
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                // 2. Disable HTTP Basic to stop the "Basic realm" 401 response
                .httpBasic(AbstractHttpConfigurer::disable)
                // 3. Add your Gateway-aware filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
