package com.studybox.backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** A staff/Super Admin login — separate from {@link Student} because it carries no
 *  academic-record fields, only the ability to manage the programme's data. */
@Entity
@Table(name = "admin_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    /** "ADMIN" today; kept as a string so more staff roles can be added later without a migration. */
    @Builder.Default
    private String role = "ADMIN";

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
