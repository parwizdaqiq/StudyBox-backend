package com.studybox.backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** The full programme curriculum a student can browse and request to enroll in — broader
 *  than the courses they're currently enrolled in ({@link Course}). */
@Entity
@Table(name = "catalog_courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String title;

    private int credits;

    private String term;

    private String category;

    @Column(length = 1000)
    private String description;
}
