package com.civicvoice.civicvoice.repository;

import com.civicvoice.civicvoice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

    // ✅ New method to fetch all users with the specified role (e.g., "ADMIN")
    List<User> findByRole(String role);
}
