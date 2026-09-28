package com.studybox.backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** A weighted grade bucket inside a course, e.g. "Assessments" worth 70% of the final grade. */
@Entity
@Table(name = "grade_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(nullable = false)
    private String name;

    private double weightPercent;

    private int orderIndex;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<GradeItem> items = new ArrayList<>();
}
