package com.Final_Project.Event_Booking.security;

import com.Final_Project.Event_Booking.model.entity.User;
import com.Final_Project.Event_Booking.model.enums.UserRole;
import com.Final_Project.Event_Booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        String username = "admin";

        if (!userRepository.existsByUsername(username)) {
            User admin = User.builder()
                    .username(username)
                    .password(passwordEncoder.encode("admin&password"))
                    .email("admin@gmail.com")
                    .role(UserRole.ADMIN)
                    .build();
            userRepository.save(admin);
        }
    }
}
