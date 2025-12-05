package com.civicvoice.civicvoice.controller;

import com.civicvoice.civicvoice.model.Complaint;
import com.civicvoice.civicvoice.model.ComplaintStatus;
import com.civicvoice.civicvoice.repository.ComplaintRepository;
import com.civicvoice.civicvoice.service.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ComplaintRepository complaintRepository;

    // ✅ Admin Dashboard (active complaints only)
    @GetMapping("/dashboard")
    public String adminDashboard(@RequestParam(value = "query", required = false) String query,
                                 Model model, Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // Security check
        if (!"ADMIN".equalsIgnoreCase(userDetails.getUser().getRole())) {
            return "redirect:/access-denied";
        }

        // ✅ Load complaints (excluding completed/rejected)
        List<Complaint> complaints;
        if (query != null && !query.trim().isEmpty()) {
            complaints = complaintRepository.searchAllComplaintsByKeyword(query.trim())
                    .stream()
                    .filter(c -> c.getStatus() != ComplaintStatus.COMPLETED &&
                            c.getStatus() != ComplaintStatus.REJECTED)
                    .toList();
        } else {
            complaints = complaintRepository.findAll()
                    .stream()
                    .filter(c -> c.getStatus() != ComplaintStatus.COMPLETED &&
                            c.getStatus() != ComplaintStatus.REJECTED)
                    .toList();
        }

        // ✅ Stats
        long pending = complaintRepository.countByStatus(ComplaintStatus.PENDING);
        long inProgress = complaintRepository.countByStatus(ComplaintStatus.IN_PROGRESS);
        long completed = complaintRepository.countByStatus(ComplaintStatus.COMPLETED);
        long rejected = complaintRepository.countByStatus(ComplaintStatus.REJECTED);

        // ✅ Model attributes
        model.addAttribute("complaints", complaints);
        model.addAttribute("statuses", ComplaintStatus.values());
        model.addAttribute("pending", pending);
        model.addAttribute("inProgress", inProgress);
        model.addAttribute("completed", completed);
        model.addAttribute("rejected", rejected);
        model.addAttribute("adminName", userDetails.getUser().getFullName());
        model.addAttribute("query", query);

        return "admin/dashboard";
    }

    // ✅ (Optional) Full complaint view
    @GetMapping("/complaints/view/{id}")
    public String viewComplaint(@PathVariable Long id, Model model, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        if (!"ADMIN".equalsIgnoreCase(userDetails.getUser().getRole())) {
            return "redirect:/access-denied";
        }

        Complaint complaint = complaintRepository.findById(id).orElse(null);
        if (complaint == null) {
            return "redirect:/admin/dashboard";
        }

        model.addAttribute("complaint", complaint);
        return "admin/view-complaint";
    }

    // ✅ Update complaint status (AJAX + form POST supported)
    @PostMapping("/complaints/{id}/status")
    @ResponseBody
    public String updateComplaintStatus(@PathVariable Long id,
                                        @RequestParam(value = "action", required = false) String action,
                                        @RequestParam(value = "reason", required = false) String reason,
                                        Authentication authentication) {

        System.out.println("🚀 Received update request for ID: " + id + " | Action: " + action + " | Reason: " + reason);

        // ✅ Verify authentication
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            System.out.println("❌ No valid authentication or user details found.");
            return "ACCESS_DENIED";
        }

        // ✅ Verify admin role
        if (!"ADMIN".equalsIgnoreCase(userDetails.getUser().getRole())) {
            System.out.println("❌ Unauthorized role: " + userDetails.getUser().getRole());
            return "ACCESS_DENIED";
        }

        Complaint complaint = complaintRepository.findById(id).orElse(null);
        if (complaint == null) {
            System.out.println("❌ Complaint not found for ID: " + id);
            return "NOT_FOUND";
        }

        ComplaintStatus currentStatus = complaint.getStatus();

        try {
            if (action == null) {
                System.out.println("⚠️ No action provided.");
                return "ERROR";
            }

            switch (action.toLowerCase()) {
                case "reject" -> {
                    complaint.setStatus(ComplaintStatus.REJECTED);
                    System.out.println("✅ Complaint rejected with reason: " + reason);
                }
                case "complete" -> {
                    if (currentStatus == ComplaintStatus.IN_PROGRESS) {
                        complaint.setStatus(ComplaintStatus.COMPLETED);
                        System.out.println("✅ Complaint marked as completed");
                    } else {
                        System.out.println("⚠️ Cannot complete complaint not in progress.");
                    }
                }
                case "start" -> {
                    if (currentStatus == ComplaintStatus.PENDING) {
                        complaint.setStatus(ComplaintStatus.IN_PROGRESS);
                        System.out.println("✅ Complaint marked as in progress");
                    } else {
                        System.out.println("⚠️ Cannot start complaint already in another state.");
                    }
                }
                default -> System.out.println("⚠️ Unknown action received: " + action);
            }

            complaintRepository.save(complaint);
            System.out.println("💾 Complaint status saved successfully.");
            return "SUCCESS";

        } catch (Exception e) {
            System.out.println("💥 Error updating complaint: " + e.getMessage());
            return "ERROR";
        }
    }

    // ✅ Access Denied Page
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}
