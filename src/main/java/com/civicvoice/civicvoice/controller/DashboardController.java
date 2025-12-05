package com.civicvoice.civicvoice.controller;

import com.civicvoice.civicvoice.model.Complaint;
import com.civicvoice.civicvoice.model.ComplaintStatus;
import com.civicvoice.civicvoice.model.User;
import com.civicvoice.civicvoice.repository.UserRepository;
import com.civicvoice.civicvoice.service.ComplaintService;
import com.civicvoice.civicvoice.service.CustomUserDetails;
import com.civicvoice.civicvoice.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ComplaintService complaintService;

    // ✅ Main User Dashboard
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getEmail());

        // 🟢 Basic User Info
        model.addAttribute("username", user.getFullName());
        model.addAttribute("greeting", "Welcome");
        model.addAttribute("user", user);

        // 🟢 Notifications
        model.addAttribute("notifications", notificationService.getUserNotifications(user));
        model.addAttribute("unreadCount", notificationService.getUserNotifications(user).size());

        // 🟢 Complaint Statistics
        List<Complaint> complaints = complaintService.getComplaintsByUser(user);
        long total = complaints.size();
        long pending = complaints.stream().filter(c -> c.getStatus() == ComplaintStatus.PENDING).count();
        long inProgress = complaints.stream().filter(c -> c.getStatus() == ComplaintStatus.IN_PROGRESS).count();
        long completed = complaints.stream().filter(c -> c.getStatus() == ComplaintStatus.COMPLETED).count();
        long rejected = complaints.stream().filter(c -> c.getStatus() == ComplaintStatus.REJECTED).count();

        model.addAttribute("total", total);
        model.addAttribute("pending", pending);
        model.addAttribute("inProgress", inProgress);
        model.addAttribute("completed", completed);
        model.addAttribute("rejected", rejected);

        return "dashboard";
    }

    // ✅ About Page
    @GetMapping("/about")
    public String aboutPage() {
        return "about"; // Renders templates/about.html
    }

    // ✅ Feedback Page
    @GetMapping("/feedback")
    public String feedbackPage() {
        return "feedback"; // Renders templates/feedback.html
    }
}
