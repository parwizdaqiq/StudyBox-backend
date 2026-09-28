package com.studybox.backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "educators")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Educator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    /** e.g. "Professor", "Adjunct Lecturer", "Tutor" */
    private String title;

    private String email;

    private String avatarInitials;
}
