package com.civicvoice.civicvoice.service;

import com.civicvoice.civicvoice.model.Feedback;
import com.civicvoice.civicvoice.model.User;
import com.civicvoice.civicvoice.repository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    public void saveFeedback(Feedback feedback) {
        feedbackRepository.save(feedback);
    }

    public List<Feedback> getFeedbackByUser(User user) {
        return feedbackRepository.findByUser(user);
    }

    public List<Feedback> getAllFeedback() {
        return feedbackRepository.findAll();
    }
}
