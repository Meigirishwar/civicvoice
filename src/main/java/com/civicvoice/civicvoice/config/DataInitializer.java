package com.civicvoice.civicvoice.config;

import com.civicvoice.civicvoice.model.User;
import com.civicvoice.civicvoice.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initializeAdmins(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        return args -> {

            // ✅ Admin 1: Madhu Anbarasu
            if (userRepository.findByEmail("madhuanbarasu19@gmail.com") == null) {
                User admin1 = new User();
                admin1.setFullName("Madhu Anbarasu");
                admin1.setEmail("madhuanbarasu19@gmail.com");
                admin1.setPassword(passwordEncoder.encode("Admin1@123"));
                admin1.setRole("ADMIN");
                admin1.setProfilePicture("/avatars/female-avatar.png"); // optional image
                userRepository.save(admin1);
                System.out.println("✅ Created admin: Madhu Anbarasu");
            }

            // ✅ Admin 2: Meigirishwar V R
            if (userRepository.findByEmail("meigirishwar18@gmail.com") == null) {
                User admin2 = new User();
                admin2.setFullName("Meigirishwar V R");
                admin2.setEmail("meigirishwar18@gmail.com");
                admin2.setPassword(passwordEncoder.encode("Admin2@123"));
                admin2.setRole("ADMIN");
                admin2.setProfilePicture("/avatars/male-avatar.png"); // optional image
                userRepository.save(admin2);
                System.out.println("✅ Created admin: Meigirishwar V R");
            }

            System.out.println("🚀 Admin initialization complete.");
        };
    }
}
