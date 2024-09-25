package com.kidari.lecture.controller;

import com.kidari.lecture.dto.LectureDTO;
import com.kidari.lecture.dto.LectureDetailDTO;
import com.kidari.lecture.dto.ReservationDTO;
import com.kidari.lecture.dto.ReservationLectureDTO;
import com.kidari.lecture.service.LectureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/front/lectures")
public class FrontController {

    @Autowired
    private LectureService lectureService;

    // 1. 신청 가능한 강연 목록 조회
    @GetMapping
    public ResponseEntity<Page<LectureDetailDTO>> getAvailableLectures(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<LectureDetailDTO> availableLectures = lectureService.getAvailableLectures(page, size);
        return ResponseEntity.ok(availableLectures);
    }

    // 2. 강연 신청
    @PostMapping("/{lectureId}/reserve")
    public ResponseEntity<String> reserveLecture(@PathVariable Long lectureId, @RequestParam String employeeNumber) {
        lectureService.reserveLecture(lectureId, employeeNumber);
        return ResponseEntity.ok("강연 신청이 완료되었습니다.");
    }

    // 3. 신청 내역 조회(사번 입력)
    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationLectureDTO>> getReservations(@RequestParam String employeeNumber) {
        List<ReservationLectureDTO> reservations = lectureService.getReservationsByEmployeeNumber(employeeNumber);
        return ResponseEntity.ok(reservations);
    }

    // 4 신청한 강연 취소
    @DeleteMapping("/reservations/{reservationId}/cancel")
    public ResponseEntity<String> cancelReservation(@PathVariable Long reservationId) {
        lectureService.cancelReservation(reservationId);
        return ResponseEntity.ok("강연 신청이 취소되었습니다.");
    }

    //5. 실시간 인기 강연
    @GetMapping("/popular")
    public ResponseEntity<List<LectureDetailDTO>> getPopularLectures() {
        List<LectureDetailDTO> popularLectures = lectureService.getPopularLectures();
        return ResponseEntity.ok(popularLectures);
    }
}
