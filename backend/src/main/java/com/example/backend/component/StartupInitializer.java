package com.example.backend.component;

import com.example.backend.PetAdoptionApplication;
import com.example.backend.entity.User;
import com.example.backend.entity.property.UserRole;
import com.example.backend.mapper.UserMapper;
import com.example.backend.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Date;

@Component("startupInitializer")
@RequiredArgsConstructor
public class StartupInitializer implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(PetAdoptionApplication.class);

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    @Value("${host.address}")
    private String hostAddress;

    @Value("${application.admin_user}")
    private String adminUsername;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        createAdminAccount();
    }

    private void createAdminAccount() {
        if (userMapper.queryByUser(adminUsername).exists()) {
            return;
        }

        String password = StringUtils.generateRandomString(12);
        String email = adminUsername + "@" + resolveHost();
        Date now = new Date();
        User admin = new User(
                null,
                adminUsername,
                passwordEncoder.encode(password),
                email,
                UserRole.rezip(UserRole.ADMIN.getMask()),
                null,
                null,
                now,
                now);
        userMapper.insert(admin);
        LOGGER.warn("Created admin account. username={}, password={}", adminUsername, password);
    }

    private String resolveHost() {
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
