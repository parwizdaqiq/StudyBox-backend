package com.studybox.backend.repository;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import com.studybox.backend.domain.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findTop20ByOrderByPostedAtDesc();
    List<Announcement> findByCourseIdInOrderByPostedAtDesc(List<Long> courseIds);

    List<Announcement> findByCourseIsNullOrderByPostedAtDesc();

    List<Announcement> findAllByOrderByPostedAtDesc();

    /** Programme-wide announcements (no course) plus those for any of the given courses. */
    @Query("select a from Announcement a where a.course is null or a.course.id in :courseIds order by a.postedAt desc")
    List<Announcement> findVisibleTo(@Param("courseIds") List<Long> courseIds);

}
