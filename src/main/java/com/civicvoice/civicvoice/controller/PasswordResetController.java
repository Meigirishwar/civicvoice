package com.civicvoice.civicvoice.controller;

import com.civicvoice.civicvoice.model.PasswordResetToken;
import com.civicvoice.civicvoice.model.User;
import com.civicvoice.civicvoice.repository.PasswordResetTokenRepository;
import com.civicvoice.civicvoice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Controller
public class PasswordResetController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // ✅ STEP 1 – Show Forgot Password page
    @GetMapping("/forgot-password")
    public String showForgotPasswordPage() {
        return "forgot-password";
    }

    // ✅ STEP 2 – Handle email submission
    @PostMapping("/forgot-password")
    public String handleForgotPassword(@RequestParam("email") String email, Model model) {

        User user = userRepository.findByEmail(email);
        if (user == null) {
            model.addAttribute("error", "No account found with that email!");
            return "forgot-password";
        }

        // Create new reset token (10-minute expiry)
        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(10);

        PasswordResetToken resetToken = new PasswordResetToken(email, token, expiry);
        tokenRepository.save(resetToken);

        // (Later you can send via Gmail)
        String resetLink = "http://localhost:8080/reset-password?token=" + token;

        model.addAttribute("message", "Password reset link generated successfully!");
        model.addAttribute("resetLink", resetLink);
        return "forgot-password";
    }

    // ✅ STEP 3 – Show Reset Password page
    @GetMapping("/reset-password")
    public String showResetPasswordPage(@RequestParam("token") String token, Model model) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token);

        if (resetToken == null || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "Invalid or expired reset token!");
            return "forgot-password";
        }

        model.addAttribute("token", token);
        return "reset-password";
    }

    // ✅ STEP 4 – Handle new password submission
    @PostMapping("/reset-password")
    public String handleResetPassword(@RequestParam("token") String token,
                                      @RequestParam("password") String password,
                                      @RequestParam("confirmPassword") String confirmPassword,
                                      Model model) {

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match!");
            model.addAttribute("token", token);
            return "reset-password";
        }

        PasswordResetToken resetToken = tokenRepository.findByToken(token);

        if (resetToken == null || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "Invalid or expired reset token!");
            return "forgot-password";
        }

        User user = userRepository.findByEmail(resetToken.getEmail());
        if (user == null) {
            model.addAttribute("error", "User not found!");
            return "forgot-password";
        }

        // Update password securely
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        // Delete token after use
        tokenRepository.delete(resetToken);

        model.addAttribute("success", "Password reset successfully! You can now login.");
        return "login";
    }
}
