package com.kidari.lecture.repository;

import com.kidari.lecture.entity.Lecture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface LectureRepository extends JpaRepository<Lecture, Long> {

    @Query("SELECT l FROM Lecture l " +
            "LEFT JOIN FETCH l.lectureStats " +  // LectureStats와 페치 조인
            "WHERE l.startTime <= :oneWeekAfter " +
            "AND l.startTime >= :oneDayAgo")
    Page<Lecture> findAvailableLectures(LocalDateTime oneWeekAfter, LocalDateTime oneDayAgo, Pageable pageable);

    @Query("SELECT l FROM Lecture l JOIN FETCH l.lectureStats")
    Page<Lecture> findAllWithLectureStats(Pageable pageable);

}
