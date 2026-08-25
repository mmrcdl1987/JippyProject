package com.jippy.notification.configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;

@Configuration
@Slf4j
public class NFireBaseConfig {

    @PostConstruct
    public void initializeFirebase() throws IOException {

        if (!FirebaseApp.getApps().isEmpty()) {
            log.info("FIREBASE_ALREADY_INITIALIZED");
            return;
        }

        InputStream serviceAccount =
                getClass()
                        .getClassLoader()
                        .getResourceAsStream(
                                "jippy-firebase-key.json"
                        );

        if (serviceAccount == null) {
            throw new IllegalStateException(
                    "Firebase service account JSON not found"
            );
        }

        FirebaseOptions options =
                FirebaseOptions.builder()
                        .setCredentials(
                                GoogleCredentials.fromStream(
                                        serviceAccount
                                )
                        )
                        .build();

        FirebaseApp.initializeApp(options);

        log.info("FIREBASE_INITIALIZED_SUCCESSFULLY");
    }
}