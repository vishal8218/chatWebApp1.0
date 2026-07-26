package com.ChatApp;

import java.io.IOException;

import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Configuration
public class FirebaseConfigurations {




    @PostConstruct
    public void init() throws IOException {
        String firebaseConfig = System.getenv("FIREBASE_CONFIG");

        if (firebaseConfig == null) {
            throw new IllegalStateException("FIREBASE_CONFIG env variable not set");
        }

        InputStream serviceAccount = new ByteArrayInputStream(firebaseConfig.getBytes());

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }
    }
}
