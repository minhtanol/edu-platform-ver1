package com.edu.app;

import com.edu.app.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EducationPlatformApplication {
  private static final String SEED_ADMIN_EMAIL = "admin@education.com";

  public static void main(String[] args) { SpringApplication.run(EducationPlatformApplication.class, args); }

  @Bean
  CommandLineRunner encodeSeedPasswords(
      UserRepository users,
      PasswordEncoder encoder,
      @Value("${app.seed.admin-password:Admin@123}") String adminPassword) {
    return args -> {
      setPassword(users, encoder, SEED_ADMIN_EMAIL, adminPassword);
    };
  }

  private static void setPassword(UserRepository users, PasswordEncoder encoder, String email, String raw) {
    users.findByEmailAndDeletedAtIsNull(email).ifPresent(u -> {
      if (!u.getPasswordHash().startsWith("$2")) {
        u.setPasswordHash(encoder.encode(raw));
        users.save(u);
      }
    });
  }
}
