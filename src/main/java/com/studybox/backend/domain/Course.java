package com.studybox.backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String title;

    private int credits;

    private String term;

    /** Hex colour used for the course's card/icon on the client, e.g. "#5B6CF9". */
    private String colorHex;

    /** Emoji or short icon token shown next to the course. */
    private String icon;

    @Column(length = 2000)
    private String description;
}
