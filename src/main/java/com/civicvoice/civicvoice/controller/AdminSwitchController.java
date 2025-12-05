package com.civicvoice.civicvoice.controller;

import com.civicvoice.civicvoice.model.User;
import com.civicvoice.civicvoice.payload.AdminLoginRequest;
import com.civicvoice.civicvoice.service.AdminAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminSwitchController {

    @Autowired
    private AdminAuthService adminAuthService;

    // --- STEP 1: Switch from user to admin ---
    @PostMapping("/switch")
    public String switchToAdmin(@ModelAttribute AdminLoginRequest request,
                                HttpServletRequest httpRequest,
                                RedirectAttributes redirectAttributes) {

        Optional<User> maybeAdmin = adminAuthService.validateAdminCredentials(request.getEmail(), request.getPassword());

        if (maybeAdmin.isEmpty()) {
            redirectAttributes.addFlashAttribute("adminError", "Invalid admin credentials!");
            return "redirect:/user/dashboard"; // back to user page
        }

        User adminUser = maybeAdmin.get();

        // end old session to prevent session fixation
        HttpSession oldSession = httpRequest.getSession(false);
        if (oldSession != null) {
            try { oldSession.invalidate(); } catch (IllegalStateException ignored) {}
        }
        httpRequest.getSession(true);

        // Create new Authentication with ROLE_ADMIN
        var authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
        var auth = new UsernamePasswordAuthenticationToken(adminUser.getEmail(), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Redirect to admin dashboard
        return "redirect:/admin/dashboard";
    }

    // --- STEP 2: Exit admin mode (back to login page) ---
    @GetMapping("/exit")
    public String exitAdmin(HttpServletRequest request) {
        // Clear admin authentication & invalidate session for safety
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();

        // Redirect back to login page or user landing page
        return "redirect:/login";
    }
}
