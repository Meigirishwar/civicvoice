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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/complaints")
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    // 🟢 Show Complaint Form
    @GetMapping("/submit")
    public String showComplaintForm(Model model) {
        model.addAttribute("complaint", new Complaint());
        return "complaints/submit";
    }

    // 🟢 Handle Complaint Submission with Optional File Upload
    @PostMapping("/submit")
    public String submitComplaint(@ModelAttribute Complaint complaint,
                                  @RequestParam(value = "mediaFile", required = false) MultipartFile mediaFile,
                                  Authentication authentication,
                                  Model model) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User currentUser = userDetails.getUser();

        try {
            // ✅ Optional Image/Video Upload
            if (mediaFile != null && !mediaFile.isEmpty()) {
                String contentType = mediaFile.getContentType();

                // Allow only image or video files
                if (contentType == null || !(contentType.startsWith("image/") || contentType.startsWith("video/"))) {
                    model.addAttribute("error", "Only image or video files are allowed!");
                    return "complaints/submit";
                }

                // Check file size (max 10 MB)
                if (mediaFile.getSize() > 10 * 1024 * 1024) {
                    model.addAttribute("error", "File size must be below 10MB!");
                    return "complaints/submit";
                }

                // ✅ Save to uploads folder in project root
                String uploadDir = System.getProperty("user.dir") + "/uploads/";
                Path uploadPath = Paths.get(uploadDir);

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                    System.out.println("📂 Created upload directory: " + uploadPath.toAbsolutePath());
                }

                String originalFilename = mediaFile.getOriginalFilename();
                String fileExtension = "";

                if (originalFilename != null && originalFilename.contains(".")) {
                    fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }

                String newFileName = System.currentTimeMillis() + "_" + currentUser.getId() + fileExtension;
                Path filePath = uploadPath.resolve(newFileName);

                mediaFile.transferTo(filePath.toFile());

                // ✅ Save public-access path
                complaint.setMediaPath("/uploads/" + newFileName);

                System.out.println("✅ File uploaded successfully: " + filePath.toAbsolutePath());
            }

            // ✅ Save complaint in DB
            complaint.setUser(currentUser);
            complaint.setStatus(ComplaintStatus.PENDING);
            complaintService.saveComplaint(complaint);

            // ✅ Notify the user
            notificationService.createNotification(
                    currentUser,
                    "Complaint filed successfully: \"" + complaint.getTitle() + "\" ✅"
            );

            // ✅ Notify all admins
            List<User> admins = userRepository.findByRole("ADMIN");
            for (User admin : admins) {
                notificationService.createNotification(
                        admin,
                        "A new complaint has been filed by " + currentUser.getFullName() +
                                " titled \"" + complaint.getTitle() + "\"."
                );
            }

            return "redirect:/complaints/list?success=true";

        } catch (IOException e) {
            System.err.println("❌ File upload failed: " + e.getMessage());
            model.addAttribute("error", "Error uploading file. Please try again later.");
            return "complaints/submit";

        } catch (Exception e) {
            System.err.println("❌ Unexpected error: " + e.getMessage());
            model.addAttribute("error", "An unexpected error occurred. Please try again.");
            return "complaints/submit";
        }
    }

    // 🟢 Show All Complaints (with optional search)
    @GetMapping("/list")
    public String listUserComplaints(@RequestParam(value = "q", required = false) String query,
                                     Model model,
                                     Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User currentUser = userDetails.getUser();

        List<Complaint> complaints = (query != null && !query.trim().isEmpty())
                ? complaintService.searchComplaintsByUser(currentUser, query.trim())
                : complaintService.getComplaintsByUser(currentUser);

        model.addAttribute("complaints", complaints);
        model.addAttribute("query", query);
        return "complaints/complaint-list";
    }

    // 🟢 Complaint History
    @GetMapping("/history")
    public String viewComplaintHistory(Model model, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User currentUser = userDetails.getUser();

        List<Complaint> complaints = complaintService.getComplaintsByUser(currentUser);
        model.addAttribute("complaints", complaints);
        return "complaints/complaint-list";
    }

    // 🟢 View Single Complaint
    @GetMapping("/{id}")
    public String viewComplaint(@PathVariable("id") Long id, Model model, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User currentUser = userDetails.getUser();

        Complaint complaint = complaintService.getComplaintById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        if (!complaint.getUser().getId().equals(currentUser.getId())) {
            return "redirect:/complaints/list";
        }

        model.addAttribute("complaint", complaint);
        return "complaints/view";
    }

    // 🟢 Track Complaint Progress
    @GetMapping("/track/{id}")
    public String trackComplaint(@PathVariable("id") Long id, Model model, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User currentUser = userDetails.getUser();

        Complaint complaint = complaintService.getComplaintById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        if (!complaint.getUser().getId().equals(currentUser.getId())) {
            return "redirect:/complaints/list";
        }

        model.addAttribute("complaint", complaint);
        return "complaints/track";
    }

    // 🟢 Complaint Stats for Dashboard
    @GetMapping("/stats")
    @ResponseBody
    public Map<String, Long> getComplaintStats() {
        return complaintService.getComplaintStats();
    }
}
