package com.civicvoice.civicvoice.repository;

import com.civicvoice.civicvoice.model.Notification;
import com.civicvoice.civicvoice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByTimestampDesc(User user);
}
