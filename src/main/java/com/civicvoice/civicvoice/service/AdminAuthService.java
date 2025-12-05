package com.civicvoice.civicvoice.service;

import com.civicvoice.civicvoice.model.User;
import com.civicvoice.civicvoice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminAuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Validate given credentials and ensure the user has ROLE_ADMIN.
     * Returns Optional<User> if valid admin, otherwise Optional.empty().
     */
    public Optional<User> validateAdminCredentials(String email, String rawPassword) {
        if (email == null || rawPassword == null) return Optional.empty();
        User user = userRepository.findByEmail(email);
        if (user == null) return Optional.empty();

        boolean passwordMatches = passwordEncoder.matches(rawPassword, user.getPassword());
        boolean isAdmin = "ROLE_ADMIN".equals(user.getRole());
        if (passwordMatches && isAdmin) {
            return Optional.of(user);
        } else {
            return Optional.empty();
        }
    }
}
