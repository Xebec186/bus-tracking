package com.xebec.BusTracking.runner;

import com.xebec.BusTracking.model.User;
import com.xebec.BusTracking.model.UserRole;
import com.xebec.BusTracking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.existsByEmailIgnoreCase("admin@bustrack.com")) {
            return;
        }

        User admin = User.builder()
                .passwordHash(passwordEncoder.encode("admin123"))
                .email("admin@bustrack.com")
                .phoneNumber("0592558959")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.ADMIN)
                .build();

        userRepository.save(admin);
    }
}
