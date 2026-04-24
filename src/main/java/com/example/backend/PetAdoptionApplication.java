package com.example.backend;

import com.example.backend.entity.User;
import com.example.backend.entity.property.UserRole;
import com.example.backend.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.net.URI;
import java.security.SecureRandom;
import java.util.Date;

@SpringBootApplication
@EnableScheduling
public class PetAdoptionApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(PetAdoptionApplication.class);
    private static final String ADMIN_USERNAME = "admin";
    private static final String PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static void main(String[] args) {
        SpringApplication.run(PetAdoptionApplication.class, args);
    }

    @Bean
    public ApplicationRunner adminAccountInitializer(
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            @Value("${host.address}") String hostAddress) {
        return args -> {
            if (userMapper.queryByUser(ADMIN_USERNAME).exists()) {
                return;
            }

            String password = generatePassword(12);
            Date now = new Date();
            User admin = new User(
                    null,
                    ADMIN_USERNAME,
                    passwordEncoder.encode(password),
                    createAdminEmail(hostAddress),
                    UserRole.ADMIN.getMask(),
                    null,
                    null,
                    now,
                    now);
            userMapper.insert(admin);
            LOGGER.warn("Created admin account. username={}, password={}", ADMIN_USERNAME, password);
        };
    }

    private static String generatePassword(int length) {
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            password.append(PASSWORD_CHARS.charAt(RANDOM.nextInt(PASSWORD_CHARS.length())));
        }
        return password.toString();
    }

    private static String createAdminEmail(String hostAddress) {
        return ADMIN_USERNAME + "@" + resolveHost(hostAddress);
    }

    private static String resolveHost(String hostAddress) {
        if (hostAddress == null || hostAddress.isBlank()) {
            return "localhost";
        }
        try {
            URI uri = URI.create(hostAddress);
            if (uri.getHost() != null && !uri.getHost().isBlank()) {
                return uri.getHost();
            }
        } catch (IllegalArgumentException ignored) {
            // Fall through to the simple string cleanup below.
        }

        String host = hostAddress
                .replaceFirst("^[a-zA-Z][a-zA-Z0-9+.-]*://", "")
                .replaceFirst("/.*$", "")
                .replaceFirst(":\\d+$", "")
                .trim();
        return host.isBlank() ? "localhost" : host;
    }
}
