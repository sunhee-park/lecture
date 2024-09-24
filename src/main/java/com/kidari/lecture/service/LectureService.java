package com.kidari.lecture.service;

import com.kidari.lecture.dto.LectureDTO;
import com.kidari.lecture.dto.ReservationDTO;
import com.kidari.lecture.entity.Lecture;
import com.kidari.lecture.entity.LectureStats;
import com.kidari.lecture.entity.Reservation;
import com.kidari.lecture.exception.CustomException;
import com.kidari.lecture.repository.LectureRepository;
import com.kidari.lecture.repository.LectureStatsRepository;
import com.kidari.lecture.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LectureService {

    @Autowired
    private LectureRepository lectureRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private LectureStatsRepository lectureStatsRepository;

    // 전체 강연 목록 페이징 조회
    public Page<LectureDTO> getAllLectures(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return lectureRepository.findAll(pageRequest).map(this::convertToDTO);
    }

    // 신청 가능한 강연 목록 페이징 조회 (시작 1주일 전부터 시작 1일 후까지)
    public Page<LectureDTO> getAvailableLectures(int page, int size) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneWeekAfter = now.plusWeeks(1);
        LocalDateTime oneDayAgo = now.minusDays(1);

        PageRequest pageRequest = PageRequest.of(page, size);
        return lectureRepository.findAvailableLectures(oneWeekAfter, oneDayAgo, pageRequest).map(this::convertToDTO);
    }

    // 강연 생성
    public void createLecture(LectureDTO lectureDTO) {
        // Lecture 엔티티 생성 및 값 설정
        Lecture lecture = new Lecture();
        lecture.setTitle(lectureDTO.getTitle());
        lecture.setLecturer(lectureDTO.getLecturer());
        lecture.setVenue(lectureDTO.getVenue());
        lecture.setCapacity(lectureDTO.getCapacity());
        lecture.setStartTime(lectureDTO.getStartTime());
        lecture.setEndTime(lectureDTO.getEndTime());
        lecture.setContent(lectureDTO.getContent());

        // 강연 저장
        lectureRepository.save(lecture);

        // LectureStats 엔티티 생성 및 값 설정
        LectureStats lectureStats = new LectureStats();
        lectureStats.setLecture(lecture);
        lectureStats.setAvailableSeats(lectureDTO.getCapacity());  // 잔여 좌석을 신청 인원과 동일하게 설정
        lectureStats.setReservedSeats(0);  // 예약된 좌석은 처음에 0
        lectureStats.setPopularityScore(0);  // 처음엔 인기 점수 0

        // 강연 통계 저장
        lectureStatsRepository.save(lectureStats);
    }

    // 강연 신청
    @Transactional
    public void reserveLecture(Long lectureId, String employeeNumber) {
        // 중복 신청 방지
        if (reservationRepository.existsByLectureLectureIdAndEmployeeNumber(lectureId, employeeNumber)) {
            throw new CustomException("이미 신청한 강연입니다.");
        }

        Lecture lecture = lectureRepository.findById(lectureId).orElseThrow(() -> new CustomException("강연을 찾을 수 없습니다."));
        LectureStats stats = lectureStatsRepository.findByLecture_LectureId(lectureId).orElseThrow(() -> new CustomException("강연 통계를 찾을 수 없습니다."));

        if (stats.getAvailableSeats() <= 0) {
            throw new CustomException("잔여 좌석이 없습니다.");
        }

        // 좌석 업데이트
        stats.setAvailableSeats(stats.getAvailableSeats() - 1);
        stats.setReservedSeats(stats.getReservedSeats() + 1);
        lectureStatsRepository.save(stats);

        // 예약 생성
        Reservation reservation = new Reservation();
        reservation.setEmployeeNumber(employeeNumber);
        reservation.setLecture(lecture);
        reservation.setReservationTime(LocalDateTime.now());
        reservationRepository.save(reservation);
    }

    // 신청된 강연 취소
    @Transactional
    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new CustomException("신청 내역을 찾을 수 없습니다."));
        LectureStats stats = lectureStatsRepository.findByLecture_LectureId(reservation.getLecture().getLectureId()).orElseThrow(() -> new CustomException("강연 통계를 찾을 수 없습니다."));

        // 좌석 업데이트
        stats.setAvailableSeats(stats.getAvailableSeats() + 1);
        stats.setReservedSeats(stats.getReservedSeats() - 1);
        lectureStatsRepository.save(stats);

        // 예약 삭제
        reservationRepository.delete(reservation);
    }

    // 사번으로 신청 내역 조회
    public List<ReservationDTO> getReservationsByEmployeeNumber(String employeeNumber) {
        List<Reservation> reservations = reservationRepository.findByEmployeeNumber(employeeNumber);
        return reservations.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // 인기 강연 조회
    public List<LectureDTO> getPopularLectures() {
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        List<LectureStats> popularLectures = lectureStatsRepository.findTopLecturesByPopularity(threeDaysAgo);
        return popularLectures.stream().map(stats -> convertToDTO(stats.getLecture())).collect(Collectors.toList());
    }

    // 특정 강연의 신청자 사번 목록 조회
    public List<String> getLectureUsers(Long lectureId) {
        return reservationRepository.findEmployeeNumbersByLectureId(lectureId);
    }

    private LectureDTO convertToDTO(Lecture lecture) {
        LectureDTO lectureDTO = new LectureDTO();
        lectureDTO.setLectureId(lecture.getLectureId());
        lectureDTO.setTitle(lecture.getTitle());
        lectureDTO.setLecturer(lecture.getLecturer());
        lectureDTO.setVenue(lecture.getVenue());
        lectureDTO.setCapacity(lecture.getCapacity());
        lectureDTO.setStartTime(lecture.getStartTime());
        lectureDTO.setEndTime(lecture.getEndTime());
        lectureDTO.setContent(lecture.getContent());
        return lectureDTO;
    }


    private ReservationDTO convertToDTO(Reservation reservation) {
        ReservationDTO reservationDTO = new ReservationDTO();
        reservationDTO.setReservationId(reservation.getReservationId());
        reservationDTO.setEmployeeNumber(reservation.getEmployeeNumber());
        reservationDTO.setLectureId(reservation.getLecture().getLectureId());
        reservationDTO.setReservationTime(reservation.getReservationTime().toString());
        return reservationDTO;
    }

}
