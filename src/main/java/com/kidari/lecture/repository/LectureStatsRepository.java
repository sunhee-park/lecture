package com.kidari.lecture.repository;

import com.kidari.lecture.entity.Lecture;
import com.kidari.lecture.entity.LectureStats;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LectureStatsRepository extends JpaRepository<LectureStats, Long> {
    @Query("SELECT l FROM Lecture l " +
            "JOIN FETCH l.lectureStats ls " +
            "WHERE l.startTime <= :oneWeekAfter " +
            "AND l.startTime >= :oneDayAgo " +
            "ORDER BY ls.popularityScore DESC")
    List<Lecture> findTopLecturesByPopularity(LocalDateTime oneWeekAfter, LocalDateTime oneDayAgo);

    Optional<LectureStats> findByLecture_LectureId(Long lectureId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM LectureStats s WHERE s.lecture.lectureId = :lectureId")
    Optional<LectureStats> findLectureStatsForUpdate(@Param("lectureId") Long lectureId);

}

