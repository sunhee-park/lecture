package com.kidari.lecture.repository;

import com.kidari.lecture.entity.LectureStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LectureStatsRepository extends JpaRepository<LectureStats, Long> {

    @Query("SELECT ls FROM LectureStats ls WHERE ls.lecture.startTime >= :threeDaysAgo ORDER BY ls.popularityScore DESC")
    List<LectureStats> findTopLecturesByPopularity(LocalDateTime threeDaysAgo);

    Optional<LectureStats> findByLecture_LectureId(Long lectureId);
}

