package com.civicvoice.civicvoice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String email;
    private String password;

    // ✅ Profile photo path (optional)
    private String profilePicture;  // Example: "/avatars/avatar3.png"

    // ✅ Role for access control (CITIZEN / ADMIN)
    @Column(nullable = false)
    private String role = "CITIZEN";
}
