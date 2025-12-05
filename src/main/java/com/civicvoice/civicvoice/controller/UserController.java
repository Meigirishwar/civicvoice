package com.civicvoice.civicvoice.controller;

import org.springframework.security.core.Authentication;
import com.civicvoice.civicvoice.model.User;
import com.civicvoice.civicvoice.model.ComplaintStatus;
import com.civicvoice.civicvoice.repository.UserRepository;
import com.civicvoice.civicvoice.repository.ComplaintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ✅ Show Register Page
    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    // ✅ Handle Registration Form Submission
    @PostMapping("/register")
    public String processRegister(@ModelAttribute User user, Model model) {
        // Encrypt password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // ✅ Ensure default role
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");
        }

        // Save the user
        userRepository.save(user);

        // ✅ Redirect to login with success parameter instead of returning login directly
        return "redirect:/login?registered=true";
    }

    // ✅ Show Login Page
    @GetMapping("/login")
    public String showLoginPage(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout,
                                @RequestParam(value = "registered", required = false) String registered,
                                Model model) {

        if (error != null) {
            model.addAttribute("error", "Invalid email or password. Please try again.");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }
        if (registered != null) {
            model.addAttribute("message", "Registration successful! Please log in.");
        }

        return "login";
    }

    // ✅ Show User Dashboard after login with complaint stats
    @GetMapping("/user/dashboard")
    public String showUserDashboard(Model model, Authentication authentication) {
        String email = authentication.getName(); // logged-in user's email
        User user = userRepository.findByEmail(email);

        // ✅ Fetch live complaint stats for this user
        long pendingCount = complaintRepository.countByUserAndStatus(user, ComplaintStatus.PENDING);
        long inProgressCount = complaintRepository.countByUserAndStatus(user, ComplaintStatus.IN_PROGRESS);
        long completedCount = complaintRepository.countByUserAndStatus(user, ComplaintStatus.COMPLETED);
        long rejectedCount = complaintRepository.countByUserAndStatus(user, ComplaintStatus.REJECTED);

        // ✅ Pass everything to Thymeleaf
        model.addAttribute("user", user);
        model.addAttribute("username", user.getFullName());
        model.addAttribute("pending", pendingCount);
        model.addAttribute("inProgress", inProgressCount);
        model.addAttribute("completed", completedCount);
        model.addAttribute("rejected", rejectedCount);

        return "dashboard";
    }
}
