package com.civicvoice.civicvoice.controller;

import com.civicvoice.civicvoice.model.User;
import com.civicvoice.civicvoice.repository.UserRepository;
import com.civicvoice.civicvoice.service.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ✅ Always point to real project directory
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/profile_pictures/";

    // ✅ View Profile Page
    @GetMapping
    public String viewProfile(Authentication authentication, Model model) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getEmail());
        model.addAttribute("user", user);
        return "profile";
    }

    // ✅ Update Profile (photo, name, email)
    @PostMapping("/update")
    public String updateProfile(@RequestParam("fullName") String fullName,
                                @RequestParam("email") String email,
                                @RequestParam(value = "profilePicture", required = false) MultipartFile file,
                                Authentication authentication) throws IOException {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getEmail());

        user.setFullName(fullName);
        user.setEmail(email);

        // ✅ Handle File Upload
        if (file != null && !file.isEmpty()) {
            Path uploadPath = Paths.get(UPLOAD_DIR);

            // Create directories if not exist
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("✅ Created upload folder: " + uploadPath.toAbsolutePath());
            }

            // Delete old photo if exists and is a file upload (not avatar)
            if (user.getProfilePicture() != null && user.getProfilePicture().startsWith("/uploads/")) {
                try {
                    Path oldFile = Paths.get(System.getProperty("user.dir") + user.getProfilePicture());
                    Files.deleteIfExists(oldFile);
                } catch (IOException e) {
                    System.err.println("⚠️ Could not delete old picture: " + e.getMessage());
                }
            }

            // Save new file
            String fileName = "profile_" + user.getId() + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            file.transferTo(filePath.toFile());

            // Save relative path to DB
            user.setProfilePicture("/uploads/profile_pictures/" + fileName);
        }

        userRepository.save(user);
        return "redirect:/profile?success=true";
    }

    // ✅ Select Avatar
    @PostMapping("/avatar")
    public String selectAvatar(@RequestParam("avatarName") String avatarName,
                               Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getEmail());
        user.setProfilePicture("/avatars/" + avatarName);
        userRepository.save(user);
        return "redirect:/profile?avatarSet=true";
    }

    // ✅ Change Password
    @PostMapping("/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getEmail());

        // Validate current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return "redirect:/profile?error=wrongCurrent";
        }

        // Validate new password match
        if (!newPassword.equals(confirmPassword)) {
            return "redirect:/profile?error=notMatch";
        }

        // Encode and save
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return "redirect:/profile?passwordChanged=true";
    }
}
