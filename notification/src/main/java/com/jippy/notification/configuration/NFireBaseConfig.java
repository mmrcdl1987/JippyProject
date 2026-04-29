package com.jippy.notification.configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.jippy.notification.exception.NotificationException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
@Slf4j
public class NFireBaseConfig {
//
//    @PostConstruct
//    public void initialize() {
//        try {
//            // Load the JSON file from the resources folder
//            InputStream serviceAccount = new ClassPathResource("jippy-firebase-key.json").getInputStream();
//           // InputStream serviceAccount = new ClassPathResource(".json").getInputStream();
//
//            FirebaseOptions options = FirebaseOptions.builder()
//                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
//                    .build();
//
//            if (FirebaseApp.getApps().isEmpty()) {
//                FirebaseApp.initializeApp(options);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

        private static final String FIREBASE_FILE = "jippy-firebase-key.json";

        @PostConstruct
        public void initialize() {

            if (!FirebaseApp.getApps().isEmpty()) {
                log.info("Firebase already initialized");
                return;
            }

            try (InputStream serviceAccount =
                         new ClassPathResource(FIREBASE_FILE).getInputStream()) {

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);

                log.info("Firebase initialized successfully");

            } catch (Exception ex) {
                log.error("Firebase initialization failed", ex);
                throw new NotificationException("Firebase initialization failed");
            }
        }

}
