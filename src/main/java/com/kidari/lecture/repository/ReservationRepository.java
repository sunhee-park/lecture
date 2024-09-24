package com.kidari.lecture.repository;

import com.kidari.lecture.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByLectureLectureIdAndEmployeeNumber(Long lectureId, String employeeNumber);

    List<Reservation> findByEmployeeNumber(String employeeNumber);

    // 특정 강연에 신청한 사용자의 사번 목록 조회
    @Query("SELECT r.employeeNumber FROM Reservation r WHERE r.lecture.lectureId = :lectureId")
    List<String> findEmployeeNumbersByLectureId(Long lectureId);
}
