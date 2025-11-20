package com.reservation.reservation.config;

import com.reservation.reservation.model.Role;
import com.reservation.reservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.reservation.reservation.model.User;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j //logs in console
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        final String adminEmail = "admin@company.com";
        final String doctorEmail = "doctor@company.com";

        if(!userRepository.existsByEmail(adminEmail)) {
            log.info("Creating initial ADMIN user...");
            final String adminPassword = "dxxxxx0xxxxxxxxnxxxxzDxxxxxfxxxxxxd";
            User admin = new User(
                    adminEmail,
                    passwordEncoder.encode(adminPassword),
                    "Super",
                    "Admin",
                    Set.of(Role.ROLE_DOCTOR, Role.ROLE_PATIENT, Role.ROLE_ADMIN)
            );
            userRepository.save(admin);

            log.info("ADMIN user created successfully.");
        } else {
            log.info("ADMIN user already exists.");
        }

        if(!userRepository.existsByEmail(doctorEmail)) {
            log.info("Creating initial DOCTOR user...");
            final String doctorPassword = "dxxxxx0xxxxxxxxnxxxxzDxxxxxklmnoprst";
            User admin = new User(
                    adminEmail,
                    passwordEncoder.encode(doctorPassword),
                    "Andrew",
                    "Gold",
                    Set.of(Role.ROLE_DOCTOR)
            );
            userRepository.save(admin);

            log.info("DOCTOR user created successfully.");
        } else {
            log.info("DOCTOR user already exists.");
        }
    }
}

