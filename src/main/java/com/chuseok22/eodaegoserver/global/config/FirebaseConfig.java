package com.chuseok22.eodaegoserver.global.config;

import com.chuseok22.eodaegoserver.global.properties.FirebaseProperties;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import java.io.FileInputStream;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(FirebaseProperties.class)
public class FirebaseConfig {

  private final FirebaseProperties firebaseProperties;

  @Bean
  public FirebaseApp firebaseApp() throws IOException {
    if (!FirebaseApp.getApps().isEmpty()) {
      return FirebaseApp.getInstance();
    }
    try (FileInputStream serviceAccount = new FileInputStream(firebaseProperties.serviceAccountKeyPath())) {
      FirebaseOptions options = FirebaseOptions.builder()
          .setCredentials(GoogleCredentials.fromStream(serviceAccount))
          .build();
      return FirebaseApp.initializeApp(options);
    }
  }

  @Bean
  public FirebaseAuth firebaseAuth(FirebaseApp firebaseApp) {
    return FirebaseAuth.getInstance(firebaseApp);
  }
}
