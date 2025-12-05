package com.civicvoice.civicvoice.repository;

import com.civicvoice.civicvoice.model.Feedback;
import com.civicvoice.civicvoice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByUser(User user);
}
