package com.parkmate.entity;

import com.parkmate.entity.enums.UserRole;
import com.parkmate.entity.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@Table(name = "user")
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone", unique = true, nullable = false, length = 12)
    private String phone;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(name = "date_of_birth", length = 10)
    private LocalDate dateOfBirth;

    @Column(name = "address", length = 100)
    private String address;

    @Column(name = "profile_picture_url", length = 500)
    private String profilePictureUrl;

    @Column(name = "id_number", length = 12)
    private String idNumber;

    @Column(name = "issue_place")
    private String issuePlace;

    private LocalDate issueDate;
    private LocalDate expiryDate;

    @Column(name = "front_photo_path", length = 500)
    private String frontPhotoPath;

    @Column(name = "back_photo_path", length = 500)
    private String backPhotoPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.PENDING;

    private boolean emailVerified = false;

    private boolean phoneVerified = false;

    private Instant lastLoginAt;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    public String getFullName() {
        if (firstName == null && lastName == null) {
            return null;
        }
        if (firstName == null) {
            return lastName.trim();
        }
        if (lastName == null) {
            return firstName.trim();
        }
        return (firstName.trim() + " " + lastName.trim()).trim();
    }
}
