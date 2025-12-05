package com.civicvoice.civicvoice.controller;

import com.civicvoice.civicvoice.model.Complaint;
import com.civicvoice.civicvoice.model.ComplaintStatus;
import com.civicvoice.civicvoice.model.User;
import com.civicvoice.civicvoice.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/complaints")
public class AdminComplaintController {

    @Autowired
    private ComplaintService complaintService;

    // ✅ 1. Dashboard summary (counts)
    @GetMapping("/dashboard")
    public String showAdminDashboard(Model model) {
        Map<String, Long> stats = complaintService.getComplaintStats();
        model.addAttribute("stats", stats);
        model.addAttribute("total", complaintService.getAllComplaints().size());
        return "admin/dashboard"; // → templates/admin/dashboard.html
    }

    // ✅ 2. List all complaints (with optional filter)
    @GetMapping
    public String listAllComplaints(@RequestParam(value = "status", required = false) String status, Model model) {
        List<Complaint> complaints;

        if (status != null && !status.isEmpty()) {
            try {
                ComplaintStatus parsedStatus = ComplaintStatus.valueOf(status.toUpperCase());
                complaints = complaintService.getAllComplaints().stream()
                        .filter(c -> c.getStatus() == parsedStatus)
                        .toList();
            } catch (IllegalArgumentException e) {
                complaints = complaintService.getAllComplaints();
            }
        } else {
            complaints = complaintService.getAllComplaints();
        }

        model.addAttribute("complaints", complaints);
        model.addAttribute("statuses", ComplaintStatus.values());
        model.addAttribute("selectedStatus", status);
        return "admin/complaints"; // → templates/admin/complaints.html
    }

    // ✅ 3. View single complaint (with user info)
    @GetMapping("/{id}")
    public String viewComplaintDetails(@PathVariable Long id, Model model) {
        Complaint complaint = complaintService.getComplaintById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        User user = complaint.getUser(); // ✅ include user info
        model.addAttribute("complaint", complaint);
        model.addAttribute("user", user);
        model.addAttribute("statuses", ComplaintStatus.values());
        return "admin/complaint-view"; // → templates/admin/complaint-view.html
    }

    // ✅ 4. Update complaint status (Admin)
    @PostMapping("/{id}/update-status")
    public String updateComplaintStatus(@PathVariable Long id, @RequestParam("status") String status) {
        try {
            ComplaintStatus newStatus = ComplaintStatus.valueOf(status.toUpperCase());
            complaintService.updateStatus(id, newStatus);

            // 🆕 Next step: trigger notification when status changes
            // (we’ll wire it in the next step NotificationService)
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Invalid status update attempted for complaint ID: " + id);
        }
        return "redirect:/admin/complaints";
    }
}
