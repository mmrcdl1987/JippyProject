package com.jippy.division.serviceImpl;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

    //     to load HTML templates for email integration settlements
    @Service // Marks this class as a Spring Bean so it can be injected using @Autowired or constructor injection
    public class TemplateService {

        // Method to load an HTML template file and return its content as a String
        public String loadTemplate(String templateName) throws IOException {

            // Builds the complete path of the template file
            // Example:
            // templateName = "merchant-settlement.html"
            // path = "email-templates/merchant-settlement.html"
            String path = "divEmail-templates/" + templateName;

            // Loads the file from src/main/resources
            // Spring searches inside the classpath (resources folder)
            ClassPathResource resource = new ClassPathResource(path);

            // Opens the file and reads all its contents into a byte array
            // HTML files are stored as bytes inside the file system
            byte[] content = resource.getInputStream().readAllBytes();

            // Converts the byte array into a Java String using UTF-8 encoding
            // UTF-8 ensures special characters like ₹, €, Telugu, etc. are displayed correctly
            return new String(content, StandardCharsets.UTF_8);
        }
    }