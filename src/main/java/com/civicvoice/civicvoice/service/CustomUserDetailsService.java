package com.civicvoice.civicvoice.service;

import com.civicvoice.civicvoice.model.User;
import com.civicvoice.civicvoice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User appUser = userRepository.findByEmail(email.toLowerCase().trim());

        if (appUser == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        // ✅ Just adjust role in memory (no DB write here)
        if (email.equalsIgnoreCase("madhuanbarasu19@gmail.com") ||
                email.equalsIgnoreCase("meigirishwar18@gmail.com")) {
            appUser.setRole("ADMIN");
        } else if (appUser.getRole() == null || appUser.getRole().isBlank()) {
            appUser.setRole("CITIZEN");
        }

        System.out.println("✅ Loaded user: " + appUser.getEmail() + " | Role: " + appUser.getRole());
        return new CustomUserDetails(appUser);
    }
}
