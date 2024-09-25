package com.kidari.lecture.repository;

import com.kidari.lecture.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByLectureLectureIdAndEmployeeNumber(Long lectureId, String employeeNumber);

    // Reservation과 Lecture를 Fetch 조인하여 예약과 강의 정보를 함께 가져옴
    @Query("SELECT r FROM Reservation r " +
            "JOIN FETCH r.lecture l " +
            "WHERE r.employeeNumber = :employeeNumber")
    List<Reservation> findByEmployeeNumber(String employeeNumber);

    // 특정 강연에 신청한 사용자의 사번 목록 조회
    @Query("SELECT r.employeeNumber FROM Reservation r WHERE r.lecture.lectureId = :lectureId")
    List<String> findEmployeeNumbersByLectureId(Long lectureId);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.lecture.lectureId = :lectureId AND r.reservationTime >= :threeDaysAgo")
    Integer countReservationsInLastThreeDays(@Param("lectureId") Long lectureId, @Param("threeDaysAgo") LocalDateTime threeDaysAgo);

}
