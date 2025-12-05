package com.civicvoice.civicvoice.controller;

import com.civicvoice.civicvoice.model.Feedback;
import com.civicvoice.civicvoice.model.User;
import com.civicvoice.civicvoice.repository.UserRepository;
import com.civicvoice.civicvoice.service.CustomUserDetails;
import com.civicvoice.civicvoice.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/feedback/submit")
    public String submitFeedback(
            @RequestParam("rating") int rating,
            @RequestParam("feedback") String feedbackText,
            @RequestParam(value = "suggestion", required = false) String suggestion,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getEmail());

        Feedback feedback = new Feedback();
        feedback.setRating(rating);
        feedback.setFeedback(feedbackText);
        feedback.setSuggestion(suggestion);
        feedback.setUser(user);

        feedbackService.saveFeedback(feedback);

        // Redirect back to the feedback page with a thank you animation
        return "redirect:/feedback?success";
    }
}
