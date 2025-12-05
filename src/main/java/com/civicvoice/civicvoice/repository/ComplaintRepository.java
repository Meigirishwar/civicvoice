package com.civicvoice.civicvoice.repository;

import com.civicvoice.civicvoice.model.Complaint;
import com.civicvoice.civicvoice.model.ComplaintStatus;
import com.civicvoice.civicvoice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    // ✅ Fetch all complaints by a specific user
    List<Complaint> findByUser(User user);

    // ✅ Fetch all complaints of user (sorted)
    List<Complaint> findByUserOrderByDateCreatedDesc(User user);

    // ✅ Count complaints by status (Admin view)
    long countByStatus(ComplaintStatus status);

    // ✅ Count complaints by status for a specific user (User dashboard)
    long countByUserAndStatus(User user, ComplaintStatus status);

    // ✅ Fetch user’s complaints by status (sorted)
    List<Complaint> findByUserAndStatusOrderByDateCreatedDesc(User user, ComplaintStatus status);

    // ✅ 🔍 USER SEARCH: search within user’s complaints
    @Query("""
           SELECT c FROM Complaint c
           WHERE c.user = :user AND (
                 LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(c.category) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(c.status) = LOWER(:keyword)
           )
           """)
    List<Complaint> searchComplaintsByUserAndKeyword(
            @Param("user") User user,
            @Param("keyword") String keyword);

    // ✅ 🔍 ADMIN SEARCH: search across all complaints
    @Query("""
           SELECT c FROM Complaint c
           WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(c.category) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(c.status) = LOWER(:keyword)
              OR LOWER(c.user.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
           """)
    List<Complaint> searchAllComplaintsByKeyword(@Param("keyword") String keyword);

    // ✅ Fetch all complaints except completed (for active dashboard view)
    List<Complaint> findByStatusNot(ComplaintStatus status);

    // ✅ Search by title but exclude completed complaints
    List<Complaint> findByTitleContainingIgnoreCaseAndStatusNot(String title, ComplaintStatus status);
}
