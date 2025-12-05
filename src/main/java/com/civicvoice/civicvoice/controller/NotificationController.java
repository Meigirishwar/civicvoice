package com.civicvoice.civicvoice.controller;

import com.civicvoice.civicvoice.model.User;
import com.civicvoice.civicvoice.repository.UserRepository;
import com.civicvoice.civicvoice.service.CustomUserDetails;
import com.civicvoice.civicvoice.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    // 🟢 Show Notifications
    @GetMapping
    public String showNotifications(Authentication authentication, Model model) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getEmail());

        model.addAttribute("notifications", notificationService.getUserNotifications(user));
        return "dashboard"; // ✅ this should match your main dashboard.html
    }

    // 🟢 Delete a single notification
    @PostMapping("/delete/{id}")
    public String deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        // ✅ redirect back to the main dashboard, not a blank one
        return "redirect:/user/dashboard";
    }

    // 🟢 Mark all as read
    @PostMapping("/markAllRead")
    public String markAllAsRead(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getEmail());

        notificationService.markAllAsRead(user);
        return "redirect:/user/dashboard"; // ✅ consistent redirect
    }
}
