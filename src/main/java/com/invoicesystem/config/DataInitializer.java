package com.invoicesystem.config;

import com.invoicesystem.model.User;
import com.invoicesystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@invoicesystem.com")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("Admin User")
                    .companyName("My Company Ltd.")
                    .companyAddress("123 Business Ave, Suite 100, New York, NY 10001")
                    .roles(Set.of("ADMIN", "USER"))
                    .enabled(true)
                    .build();

            userRepository.save(admin);
            log.info("✅ Default admin user created — username: admin, password: admin123");
        }
    }
}
