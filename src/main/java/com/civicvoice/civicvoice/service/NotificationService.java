package com.civicvoice.civicvoice.service;

import com.civicvoice.civicvoice.model.Notification;
import com.civicvoice.civicvoice.model.User;
import com.civicvoice.civicvoice.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    // 🔹 Get notifications for a user (sorted by newest first)
    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByUserOrderByTimestampDesc(user);
    }

    // 🔹 Delete a single notification
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }

    // 🔹 Mark all notifications as read
    public void markAllAsRead(User user) {
        List<Notification> notifications = getUserNotifications(user);
        notifications.forEach(n -> {
            n.setRead(true);
        });
        notificationRepository.saveAll(notifications);
    }

    // 🔹 Get unread count
    public long getUnreadCount(User user) {
        return notificationRepository.findByUserOrderByTimestampDesc(user)
                .stream()
                .filter(n -> !n.isRead())
                .count();
    }

    // 🔹 Create a notification
    public void createNotification(User user, String message) {
        Notification notification = new Notification(user, message);
        notificationRepository.save(notification);
    }
}
