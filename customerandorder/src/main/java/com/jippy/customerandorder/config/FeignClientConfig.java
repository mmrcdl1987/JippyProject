package com.jippy.customerandorder.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignClientConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String username = request.getHeader("X-Auth-User");
                String roles = request.getHeader("X-Auth-Roles");
                if (username != null) requestTemplate.header("X-Auth-User", username);
                if (roles != null) requestTemplate.header("X-Auth-Roles", roles);
            }
            else {
                // Background Scheduler Flow (No HTTP Request context exists)
                // Pass hardcoded credentials identifying this call as the SYSTEM
                requestTemplate.header("X-Auth-User", "SYSTEM_SCHEDULER");
                requestTemplate.header("X-Auth-Roles", "ROLE_INTERNAL_SYSTEM");
            }
        };
    }
}
