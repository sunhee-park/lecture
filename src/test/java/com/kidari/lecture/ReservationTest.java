package com.kidari.lecture;

import com.kidari.lecture.dto.LectureDTO;
import com.kidari.lecture.entity.LectureStats;
import com.kidari.lecture.repository.LectureStatsRepository;
import com.kidari.lecture.repository.ReservationRepository;
import com.kidari.lecture.service.LectureService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ReservationTest {

    @Autowired
    LectureService lectureService;
    @Autowired
    private LectureStatsRepository lectureStatsRepository;
    @Autowired
    private ReservationRepository reservationRepository;


    @BeforeEach
    public void insert() {
        // 사전 데이터 설정
        // 강연 등록
        LectureDTO lectureDTO = new LectureDTO();
        lectureDTO.setLecturer("Kim Sunhee");
        lectureDTO.setTitle("SpringBoot");
        lectureDTO.setVenue("1 venue");
        lectureDTO.setCapacity(100);
        lectureDTO.setContent("Web Application Development with Spring Boot");

        LocalDateTime now = LocalDateTime.now();

        // 3일 더하기
        LocalDateTime startTime = now.plusDays(3);
        LocalDateTime endTime = startTime.plusDays(10);

        lectureDTO.setStartTime(startTime);
        lectureDTO.setEndTime(endTime);
        lectureService.createLecture(lectureDTO);
    }


    // 100 명의 사용자가 강연 신청
    @Test
    public void reserve_100_user() throws InterruptedException {
        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        Long lectureId = 1L;

        for (int i = 0; i < threadCount; i++) {
            final String employeeNumber = "EMP" + i; // 각 스레드마다 고유한 employeeNumber 사용
            executorService.submit(() -> {
                try {
                    lectureService.reserveLecture(lectureId, employeeNumber);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // 강연 통계 확인 (최종적으로 잔여 좌석이 0이 되어야 함)
        LectureStats stats = lectureStatsRepository.findByLecture_LectureId(lectureId).orElseThrow();
        assertEquals(0, stats.getAvailableSeats());
        assertEquals(100, stats.getReservedSeats());

        // 예약된 총 좌석 수 확인
        List<String> reservations = reservationRepository.findEmployeeNumbersByLectureId(lectureId);
        assertEquals(100, reservations.size()); // 100명이 성공적으로 예약되었는지 확인
    }
}
