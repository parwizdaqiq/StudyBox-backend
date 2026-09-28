package com.studybox.backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "grade_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private GradeCategory category;

    /** Optional link back to the underlying assignment (for due date / status). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id")
    private Assignment assignment;

    /**
     * Who this item belongs to. {@code null} means "shared" — every student enrolled in the
     * course sees it (used by the seed's class-wide demo grades); a Super Admin-published grade
     * always sets this, so it's visible only to that one student.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(nullable = false)
    private String name;

    private double maxScore;

    private Double scoreEarned;

    @Builder.Default
    private boolean posted = false;

    private int orderIndex;
}
