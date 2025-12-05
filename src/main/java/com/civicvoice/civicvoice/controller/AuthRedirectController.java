package com.civicvoice.civicvoice.controller;

import com.civicvoice.civicvoice.model.User;
import com.civicvoice.civicvoice.repository.UserRepository;
import com.civicvoice.civicvoice.service.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthRedirectController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/role-redirect")
    public String redirectAfterLogin(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getEmail());

        if (user.getRole().equals("ADMIN")) {
            return "redirect:/admin/dashboard"; // goes to admin dashboard
        } else {
            return "redirect:/dashboard"; // goes to citizen dashboard
        }
    }
}
