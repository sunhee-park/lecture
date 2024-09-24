package com.kidari.lecture.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Getter
@Setter
public class LectureDTO {
    private Long lectureId;
    @NotBlank(message = "강연 제목을 입력하세요.")
    private String title;
    @NotBlank(message = "강연자를 입력하세요.")
    private String lecturer;
    @NotBlank(message = "강연장을 입력하세요.")
    private String venue;
    @NotNull(message = "신청 인원을 입력하세요.")
    private int capacity;
    @NotNull(message = "강연 시작 시간을 입력하세요.")
    private LocalDateTime startTime;
    @NotNull(message = "강연 종료 시간을 입력하세요.")
    private LocalDateTime endTime;
    @NotBlank(message = "강연 내용을 입력하세요.")
    private String content;
}

