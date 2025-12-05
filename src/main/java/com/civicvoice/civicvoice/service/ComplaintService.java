package com.civicvoice.civicvoice.service;

import com.civicvoice.civicvoice.model.Complaint;
import com.civicvoice.civicvoice.model.ComplaintStatus;
import com.civicvoice.civicvoice.model.User;
import com.civicvoice.civicvoice.repository.ComplaintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ComplaintService {

    @Autowired
    private ComplaintRepository complaintRepository;

    // ✅ Create a new complaint for a user
    public Complaint createComplaint(User user, String title, String description, String category) {
        Complaint complaint = new Complaint();
        complaint.setUser(user);
        complaint.setTitle(title);
        complaint.setDescription(description);
        complaint.setCategory(category);
        complaint.setStatus(ComplaintStatus.PENDING);
        return complaintRepository.save(complaint);
    }

    // ✅ Save complaint (used by controllers)
    public Complaint saveComplaint(Complaint complaint) {
        return complaintRepository.save(complaint);
    }

    // ✅ Get all complaints for a particular user
    public List<Complaint> getComplaintsByUser(User user) {
        return complaintRepository.findByUserOrderByDateCreatedDesc(user);
    }

    // ✅ Get a specific complaint (by ID)
    public Optional<Complaint> getComplaintById(Long id) {
        return complaintRepository.findById(id);
    }

    // ✅ Update complaint status
    public void updateStatus(Long complaintId, ComplaintStatus status) {
        Optional<Complaint> optionalComplaint = complaintRepository.findById(complaintId);
        if (optionalComplaint.isPresent()) {
            Complaint complaint = optionalComplaint.get();
            complaint.setStatus(status);
            complaintRepository.save(complaint);
        }
    }

    // ✅ Get overall counts (for dashboard stats)
    public long countByStatus(ComplaintStatus status) {
        return complaintRepository.countByStatus(status);
    }

    // ✅ Get all complaints (for admin dashboard)
    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAll();
    }

    // ✅ Get complaint statistics (for dashboard charts)
    public Map<String, Long> getComplaintStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("pending", complaintRepository.countByStatus(ComplaintStatus.PENDING));
        stats.put("inProgress", complaintRepository.countByStatus(ComplaintStatus.IN_PROGRESS));
        stats.put("completed", complaintRepository.countByStatus(ComplaintStatus.COMPLETED));
        stats.put("rejected", complaintRepository.countByStatus(ComplaintStatus.REJECTED));
        return stats;
    }

    // ✅ Search user complaints (title/category/status)
    public List<Complaint> searchComplaintsByUser(User user, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return complaintRepository.findByUserOrderByDateCreatedDesc(user);
        }
        return complaintRepository.searchComplaintsByUserAndKeyword(user, keyword.trim());
    }

    // ✅ Search all complaints (admin use)
    public List<Complaint> searchAllComplaints(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return complaintRepository.findAll();
        }
        return complaintRepository.searchAllComplaintsByKeyword(keyword.trim());
    }
}
